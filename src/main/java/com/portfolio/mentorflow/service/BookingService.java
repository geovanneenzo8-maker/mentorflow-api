package com.portfolio.mentorflow.service;

import com.portfolio.mentorflow.domain.entity.*;
import com.portfolio.mentorflow.domain.enums.BookingStatus;
import com.portfolio.mentorflow.domain.enums.TransactionType;
import com.portfolio.mentorflow.dto.BookingCancelRequest;
import com.portfolio.mentorflow.dto.BookingCreateRequest;
import com.portfolio.mentorflow.dto.BookingResponse;
import com.portfolio.mentorflow.exception.BusinessRuleException;
import com.portfolio.mentorflow.exception.ResourceNotFoundException;
import com.portfolio.mentorflow.exception.ScheduleConflictException;
import com.portfolio.mentorflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    public static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.15");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BookingRepository bookingRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public BookingService(BookingRepository bookingRepository,
                          MentorRepository mentorRepository,
                          MenteeRepository menteeRepository,
                          AvailabilitySlotRepository availabilitySlotRepository,
                          LedgerTransactionRepository ledgerTransactionRepository) {
        this.bookingRepository = bookingRepository;
        this.mentorRepository = mentorRepository;
        this.menteeRepository = menteeRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listAllBookings(Long mentorId) {
        List<Booking> bookings = (mentorId != null)
                ? bookingRepository.findByMentorIdWithDetails(mentorId)
                : bookingRepository.findAllWithDetailsOrderByStartTimeDesc();

        return bookings.stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        return BookingResponse.fromEntity(booking);
    }

    /**
     * Cria um agendamento aplicando:
     * 1. Idempotência via idempotencyKey
     * 2. Pessimistic Write Lock no Mentor para evitar Race Condition de Double-Booking
     * 3. Validação de janela de disponibilidade e duração (30m a 180m)
     * 4. Detecção de interseção temporal (Overlap Detection)
     * 5. Cálculo preciso de Split Financeiro (85% Mentor / 15% Plataforma) sem perda de centavos
     */
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        String normalizedKey = (request.idempotencyKey() != null && !request.idempotencyKey().isBlank())
                ? request.idempotencyKey().trim()
                : null;

        if (normalizedKey != null) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(normalizedKey);
            if (existing.isPresent()) {
                return BookingResponse.fromEntity(existing.get());
            }
        }

        validateBookingWindow(request);

        // Lock pessimista na linha do mentor para serializar agendamentos concorrentes
        Mentor mentor = mentorRepository.findByIdWithLock(request.mentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor não encontrado com ID: " + request.mentorId()));

        Mentee mentee = menteeRepository.findById(request.menteeId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentorado não encontrado com ID: " + request.menteeId()));

        validateMentorAvailability(mentor, request);

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                mentor.getId(),
                request.startTime(),
                request.endTime(),
                List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED)
        );

        if (!conflicts.isEmpty()) {
            Booking conflict = conflicts.getFirst();
            throw new ScheduleConflictException(
                    String.format(
                            "Conflito de agenda: o mentor %s já possui a mentoria #%d agendada das %s às %s.",
                            mentor.getName(),
                            conflict.getId(),
                            conflict.getStartTime().format(TIME_FMT),
                            conflict.getEndTime().format(TIME_FMT)
                    ),
                    conflict.getId()
            );
        }

        long durationMinutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        BigDecimal totalAmount = mentor.getHourlyRate()
                .multiply(hours)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal platformFee = totalAmount
                .multiply(PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Subtração garante soma exata dos centavos (mentorAmount + platformFee == totalAmount)
        BigDecimal mentorAmount = totalAmount.subtract(platformFee);

        String finalIdempotencyKey = (normalizedKey != null)
                ? normalizedKey
                : "bk-" + UUID.randomUUID().toString().substring(0, 12);

        Booking booking = new Booking(
                mentor,
                mentee,
                request.topic().trim(),
                request.notes(),
                request.startTime(),
                request.endTime(),
                totalAmount,
                mentorAmount,
                platformFee,
                finalIdempotencyKey
        );

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    /**
     * Confirma o pagamento do agendamento atomicamente:
     * - Debita a carteira do Mentorado
     * - Credita 85% na carteira do Mentor
     * - Registra 3 lançamentos contábeis imutáveis no Ledger (Escrow, Repasse Mentor, Taxa Plataforma)
     * - Gera sala exclusiva de videoconferência
     */
    @Transactional
    public BookingResponse confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException(
                    "INVALID_BOOKING_STATE_TRANSITION",
                    "Apenas agendamentos com status PENDING_PAYMENT podem ter o pagamento confirmado. Status atual: " + booking.getStatus()
            );
        }

        Mentee mentee = booking.getMentee();
        Mentor mentor = booking.getMentor();

        if (!mentee.hasSufficientBalance(booking.getTotalAmount())) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_WALLET_BALANCE",
                    String.format(
                            "Saldo insuficiente na carteira de %s. Necessário: R$ %s | Disponível: R$ %s",
                            mentee.getName(),
                            booking.getTotalAmount(),
                            mentee.getWalletBalance()
                    )
            );
        }

        mentee.debitWallet(booking.getTotalAmount());
        mentor.creditWallet(booking.getMentorAmount());

        String meetingCode = UUID.randomUUID().toString().substring(0, 8);
        booking.confirmPayment("https://meet.mentorflow.dev/sala-" + booking.getId() + "-" + meetingCode);

        String txPrefix = "TX-" + booking.getId() + "-" + System.currentTimeMillis() % 100000;

        ledgerTransactionRepository.save(new LedgerTransaction(
                booking,
                TransactionType.PAYMENT_CAPTURED,
                booking.getTotalAmount(),
                "Pagamento capturado de " + mentee.getName() + " para mentoria #" + booking.getId(),
                txPrefix + "-CAP"
        ));

        ledgerTransactionRepository.save(new LedgerTransaction(
                booking,
                TransactionType.MENTOR_SPLIT_CREDIT,
                booking.getMentorAmount(),
                "Split 85% creditado na carteira do mentor " + mentor.getName(),
                txPrefix + "-MNT"
        ));

        ledgerTransactionRepository.save(new LedgerTransaction(
                booking,
                TransactionType.PLATFORM_FEE_CREDIT,
                booking.getPlatformFee(),
                "Take-rate 15% retido pela plataforma MentorFlow",
                txPrefix + "-FEE"
        ));

        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException(
                    "INVALID_BOOKING_STATE_TRANSITION",
                    "Apenas agendamentos com status CONFIRMED podem ser concluídos. Status atual: " + booking.getStatus()
            );
        }

        booking.completeSession();
        booking.getMentor().incrementCompletedSessions();
        return BookingResponse.fromEntity(booking);
    }

    /**
     * Cancela um agendamento e, caso já estivesse pago (CONFIRMED), executa o estorno
     * reverso atômico (devolve 100% ao mentorado, estorna o repasse do mentor e grava no Ledger).
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, BookingCancelRequest request) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "INVALID_BOOKING_STATE_TRANSITION",
                    "Não é possível cancelar um agendamento que já está " + booking.getStatus()
            );
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Mentee mentee = booking.getMentee();
            Mentor mentor = booking.getMentor();

            mentee.creditWallet(booking.getTotalAmount());
            mentor.debitWallet(booking.getMentorAmount());

            String refundRef = "TX-REF-" + booking.getId() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            ledgerTransactionRepository.save(new LedgerTransaction(
                    booking,
                    TransactionType.REFUND_DEBIT,
                    booking.getTotalAmount(),
                    "Estorno integral ao mentorado " + mentee.getName() + " por cancelamento: " + request.reason(),
                    refundRef
            ));
        }

        booking.cancel(request.reason().trim());
        return BookingResponse.fromEntity(booking);
    }

    private void validateBookingWindow(BookingCreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessRuleException(
                    "INVALID_TIME_RANGE",
                    "A data/hora de início deve ser anterior à data/hora de término."
            );
        }

        if (!request.startTime().toLocalDate().equals(request.endTime().toLocalDate())) {
            throw new BusinessRuleException(
                    "CROSS_DAY_BOOKING_NOT_ALLOWED",
                    "A sessão de mentoria deve iniciar e terminar no mesmo dia."
            );
        }

        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        if (minutes < 30 || minutes > 180) {
            throw new BusinessRuleException(
                    "INVALID_SESSION_DURATION",
                    "A duração da mentoria deve ser de no mínimo 30 minutos e no máximo 180 minutos (3 horas)."
            );
        }
    }

    private void validateMentorAvailability(Mentor mentor, BookingCreateRequest request) {
        List<AvailabilitySlot> daySlots = availabilitySlotRepository
                .findByMentorIdAndDayOfWeekAndActiveTrue(mentor.getId(), request.startTime().getDayOfWeek());

        if (!daySlots.isEmpty()) {
            boolean covered = daySlots.stream()
                    .anyMatch(slot -> slot.coversInterval(
                            request.startTime().toLocalTime(),
                            request.endTime().toLocalTime()
                    ));
            if (!covered) {
                throw new BusinessRuleException(
                        "OUTSIDE_MENTOR_AVAILABILITY",
                        String.format(
                                "O horário solicitado (%s - %s) está fora da janela de atendimento do mentor para %s.",
                                request.startTime().toLocalTime(),
                                request.endTime().toLocalTime(),
                                request.startTime().getDayOfWeek()
                        )
                );
            }
        }
    }
}
