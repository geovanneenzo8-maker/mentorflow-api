package com.portfolio.mentorflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.mentorflow.domain.entity.LedgerTransaction;
import com.portfolio.mentorflow.domain.entity.Mentee;
import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import com.portfolio.mentorflow.domain.enums.TransactionType;
import com.portfolio.mentorflow.dto.BookingCancelRequest;
import com.portfolio.mentorflow.dto.BookingCreateRequest;
import com.portfolio.mentorflow.dto.ReviewCreateRequest;
import com.portfolio.mentorflow.repository.BookingRepository;
import com.portfolio.mentorflow.repository.LedgerTransactionRepository;
import com.portfolio.mentorflow.repository.MenteeRepository;
import com.portfolio.mentorflow.repository.MentorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MentorFlowApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private MenteeRepository menteeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private LedgerTransactionRepository ledgerTransactionRepository;

    @Test
    @DisplayName("1. Deve listar mentores cadastrados e filtrar por especialidade")
    void shouldListMentorsAndFilterBySpecialty() throws Exception {
        mockMvc.perform(get("/api/v1/mentors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNumber());

        mockMvc.perform(get("/api/v1/mentors").param("specialty", "BACKEND_JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("BACKEND_JAVA"));
    }

    @Test
    @DisplayName("2. Precisão Monetária: 31 minutos a R$ 1.500,00/h deve resultar exatamente em R$ 775,00 (sem erro de arredondamento intermediário)")
    void shouldCalculateExactFractionalMinutesWithoutPrematureRounding() throws Exception {
        Mentor highRateMentor = mentorRepository.save(new Mentor(
                "Dr. Carlos Albuquerque",
                "carlos.highrate@mentorflow.dev",
                "Distinguished Architect",
                "Global FinTech",
                "Arquitetura de motores financeiros de alta precisão e baixa latência.",
                MentorSpecialty.SYSTEM_DESIGN,
                "Java, FinTech, Concorrência",
                new BigDecimal("1500.00")
        ));

        LocalDate targetDate = LocalDate.now().plusDays(10);
        BookingCreateRequest request = new BookingCreateRequest(
                highRateMentor.getId(),
                1L,
                "Cálculo de minutos fracionados (31 min a R$ 1.500/h)",
                "Validação de multiplicação antes da divisão para evitar R$ 775,05.",
                LocalDateTime.of(targetDate, LocalTime.of(10, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(10, 31)),
                "test-fractional-31min-1500"
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.durationMinutes").value(31))
                .andExpect(jsonPath("$.totalAmount").value(775.00))
                .andExpect(jsonPath("$.platformFee").value(116.25))
                .andExpect(jsonPath("$.mentorAmount").value(658.75));
    }

    @Test
    @DisplayName("3. Idempotência Concorrente: 8 threads simultâneas com a mesma idempotencyKey devem receber 201 Created com a MESMA reserva (e nunca 409)")
    void shouldGuaranteeConcurrentIdempotencyWhenSimultaneousRequestsShareSameKey() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(11);
        String sharedKey = "concurrent-idem-key-999";

        BookingCreateRequest request = new BookingCreateRequest(
                2L,
                1L,
                "Mentoria System Design com Idempotência Concorrente",
                "Disparo simultâneo com CountDownLatch usando a mesma chave.",
                LocalDateTime.of(targetDate, LocalTime.of(14, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(15, 0)),
                sharedKey
        );
        String payload = objectMapper.writeValueAsString(request);

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<MvcResult>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                return mockMvc.perform(post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                        .andReturn();
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        List<Integer> statuses = new ArrayList<>();
        List<Long> bookingIds = new ArrayList<>();
        for (Future<MvcResult> f : futures) {
            MvcResult res = f.get();
            statuses.add(res.getResponse().getStatus());
            JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
            bookingIds.add(json.get("id").asLong());
        }
        executor.shutdown();

        assertThat(statuses).allMatch(s -> s == 201);
        Set<Long> uniqueIds = bookingIds.stream().collect(Collectors.toSet());
        assertThat(uniqueIds).hasSize(1);
    }

    @Test
    @DisplayName("4. Concorrência de Agendamento: 8 threads disputando o mesmo mentor e horário simultaneamente devem aprovar 1 (201) e rejeitar 7 (409 Conflict)")
    void shouldPreventConcurrentDoubleBookingUnderMultiThreadedRace() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(12);

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                BookingCreateRequest req = new BookingCreateRequest(
                        1L,
                        1L,
                        "Disputa Concorrente #" + idx,
                        "Tentativa simultânea de reserva no mesmo intervalo.",
                        LocalDateTime.of(targetDate, LocalTime.of(14, (idx % 2 == 0) ? 0 : 15)),
                        LocalDateTime.of(targetDate, LocalTime.of(15, 0)),
                        "race-booking-key-" + idx
                );
                String body = objectMapper.writeValueAsString(req);
                readyLatch.countDown();
                startLatch.await();
                return mockMvc.perform(post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statusCodes.add(future.get());
        }
        executor.shutdown();

        long createdCount = statusCodes.stream().filter(code -> code == 201).count();
        long conflictCount = statusCodes.stream().filter(code -> code == 409).count();

        assertThat(createdCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(threads - 1);
    }

    @Test
    @DisplayName("5. Consistência de Carteira (Lost Update Prevention): pagamentos concorrentes do mesmo aluno para mentores diferentes nunca sobrescrevem débitos")
    void shouldPreventLostUpdateWhenSameMenteePaysTwoDifferentMentorsConcurrently() throws Exception {
        // Cenário 5A: Aluno com R$ 250,00 tenta pagar simultaneamente duas mentorias (R$ 180 + R$ 240 = R$ 420)
        Mentee tightBudgetMentee = menteeRepository.save(new Mentee(
                "Lucas Concorrência Insuficiente",
                "lucas.tight@dev.br",
                "Backend Developer",
                "Júnior",
                new BigDecimal("250.00")
        ));

        LocalDate dateA = LocalDate.now().plusDays(16);
        long bookingA1 = createBookingHelper(1L, tightBudgetMentee.getId(), dateA, 10, 11); // R$ 180.00
        long bookingA2 = createBookingHelper(2L, tightBudgetMentee.getId(), dateA, 10, 11); // R$ 240.00

        List<Integer> resultsInsufficient = executeConcurrentPayments(List.of(bookingA1, bookingA2));
        assertThat(resultsInsufficient).containsExactlyInAnyOrder(200, 422);

        Mentee afterTight = menteeRepository.findById(tightBudgetMentee.getId()).orElseThrow();
        assertThat(afterTight.getWalletBalance()).isIn(new BigDecimal("70.00"), new BigDecimal("10.00"));

        // Cenário 5B: Aluno com R$ 500,00 paga simultaneamente duas mentorias para mentores diferentes (R$ 180 + R$ 190 = R$ 370)
        // Sem lock em Mentee ocorreria Lost Update (saldo final ficaria 320 ou 310 em vez de 130.00)
        Mentee sufficientMentee = menteeRepository.save(new Mentee(
                "Ana Concorrência Saldo Suficiente",
                "ana.sufficient@dev.br",
                "Backend Developer",
                "Júnior",
                new BigDecimal("500.00")
        ));

        BigDecimal mentor1Before = mentorRepository.findById(1L).orElseThrow().getWalletBalance();
        BigDecimal mentor3Before = mentorRepository.findById(3L).orElseThrow().getWalletBalance();

        LocalDate dateB = LocalDate.now().plusDays(17);
        long bookingB1 = createBookingHelper(1L, sufficientMentee.getId(), dateB, 14, 15); // Mentor 1: R$ 180.00 (85% = 153.00)
        long bookingB2 = createBookingHelper(3L, sufficientMentee.getId(), dateB, 14, 15); // Mentor 3: R$ 190.00 (85% = 161.50)

        List<Integer> resultsSufficient = executeConcurrentPayments(List.of(bookingB1, bookingB2));
        assertThat(resultsSufficient).containsExactly(200, 200);

        Mentee afterSufficient = menteeRepository.findById(sufficientMentee.getId()).orElseThrow();
        assertThat(afterSufficient.getWalletBalance()).isEqualByComparingTo(new BigDecimal("130.00"));

        BigDecimal mentor1After = mentorRepository.findById(1L).orElseThrow().getWalletBalance();
        BigDecimal mentor3After = mentorRepository.findById(3L).orElseThrow().getWalletBalance();
        assertThat(mentor1After).isEqualByComparingTo(mentor1Before.add(new BigDecimal("153.00")));
        assertThat(mentor3After).isEqualByComparingTo(mentor3Before.add(new BigDecimal("161.50")));

        assertThat(ledgerTransactionRepository.findByBookingIdOrderByIdAsc(bookingB1)).hasSize(3);
        assertThat(ledgerTransactionRepository.findByBookingIdOrderByIdAsc(bookingB2)).hasSize(3);
    }

    @Test
    @DisplayName("6. Máquina de Estados & Overlap: impede concluir sessão futura (422) e mantém intervalo de sessão COMPLETED bloqueado contra novas reservas (409)")
    void shouldRejectPrematureCompletionOfFutureSessionAndKeepCompletedSlotOccupied() throws Exception {
        // Parte A: Sessão futura NÃO pode ser concluída antes do início
        LocalDate futureDate = LocalDate.now().plusDays(20);
        long futureBookingId = createBookingHelper(3L, 1L, futureDate, 16, 17);

        mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", futureBookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(post("/api/v1/bookings/{id}/complete", futureBookingId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("PREMATURE_SESSION_COMPLETION"));

        // Parte B: Sessão cujo horário já iniciou pode ser concluída e avaliada
        LocalDate pastDate = LocalDate.now().minusDays(1);
        long pastBookingId = createBookingHelper(3L, 1L, pastDate, 16, 17);

        mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", pastBookingId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/bookings/{id}/complete", pastBookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        ReviewCreateRequest reviewReq = new ReviewCreateRequest(
                pastBookingId,
                5,
                "Excelente explicação prática sobre observabilidade, métricas e containers!"
        );
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        // Parte C: Sessão COMPLETED continua ocupando o intervalo e impede sobreposição (409 Conflict)
        BookingCreateRequest overlappingWithCompleted = new BookingCreateRequest(
                3L,
                2L,
                "Tentativa de agendar sobre sessão já COMPLETED",
                "Deve ser bloqueada pois COMPLETED faz parte de OCCUPIED_STATUSES.",
                LocalDateTime.of(pastDate, LocalTime.of(16, 15)),
                LocalDateTime.of(pastDate, LocalTime.of(17, 15)),
                null
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingWithCompleted)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SCHEDULE_OVERLAP_CONFLICT"))
                .andExpect(jsonPath("$.conflictingBookingId").value(pastBookingId));
    }

    @Test
    @DisplayName("7. Auditoria Contábil de Estorno: confere saldos de Mentorado/Mentor e lançamentos no Ledger após pagamento e cancelamento")
    void shouldAuditWalletBalancesAndLedgerEntriesAfterPaymentAndRefund() throws Exception {
        Long mentorId = 4L; // Juliana Martins: R$ 210,00/h (Split: 178.50 mentor / 31.50 plataforma)
        Long menteeId = 2L;

        BigDecimal menteeInitial = menteeRepository.findById(menteeId).orElseThrow().getWalletBalance();
        BigDecimal mentorInitial = mentorRepository.findById(mentorId).orElseThrow().getWalletBalance();

        LocalDate targetDate = LocalDate.now().plusDays(25);
        long bookingId = createBookingHelper(mentorId, menteeId, targetDate, 18, 19);

        // 1. Confirma o pagamento e audita saldos e Ledger
        mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        BigDecimal menteeAfterPay = menteeRepository.findById(menteeId).orElseThrow().getWalletBalance();
        BigDecimal mentorAfterPay = mentorRepository.findById(mentorId).orElseThrow().getWalletBalance();
        assertThat(menteeAfterPay).isEqualByComparingTo(menteeInitial.subtract(new BigDecimal("210.00")));
        assertThat(mentorAfterPay).isEqualByComparingTo(mentorInitial.add(new BigDecimal("178.50")));

        List<LedgerTransaction> ledgerAfterPay = ledgerTransactionRepository.findByBookingIdOrderByIdAsc(bookingId);
        assertThat(ledgerAfterPay).hasSize(3);
        assertThat(ledgerAfterPay).extracting(LedgerTransaction::getType).containsExactly(
                TransactionType.PAYMENT_CAPTURED,
                TransactionType.MENTOR_SPLIT_CREDIT,
                TransactionType.PLATFORM_FEE_CREDIT
        );

        // 2. Cancela com estorno e audita reversão integral dos saldos e lançamento REFUND_DEBIT
        BookingCancelRequest cancelReq = new BookingCancelRequest("Reagendamento solicitado com antecedência.");
        mockMvc.perform(post("/api/v1/bookings/{id}/cancel", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        BigDecimal menteeAfterRefund = menteeRepository.findById(menteeId).orElseThrow().getWalletBalance();
        BigDecimal mentorAfterRefund = mentorRepository.findById(mentorId).orElseThrow().getWalletBalance();
        assertThat(menteeAfterRefund).isEqualByComparingTo(menteeInitial);
        assertThat(mentorAfterRefund).isEqualByComparingTo(mentorInitial);

        List<LedgerTransaction> ledgerAfterRefund = ledgerTransactionRepository.findByBookingIdOrderByIdAsc(bookingId);
        assertThat(ledgerAfterRefund).hasSize(4);
        LedgerTransaction refundTx = ledgerAfterRefund.getLast();
        assertThat(refundTx.getType()).isEqualTo(TransactionType.REFUND_DEBIT);
        assertThat(refundTx.getAmount()).isEqualByComparingTo(new BigDecimal("210.00"));

        // 3. Verifica que o horário cancelado foi liberado para nova reserva
        long rebookedId = createBookingHelper(mentorId, 3L, targetDate, 18, 19);
        assertThat(rebookedId).isNotEqualTo(bookingId);
    }

    private long createBookingHelper(Long mentorId, Long menteeId, LocalDate date, int startHour, int endHour) throws Exception {
        BookingCreateRequest req = new BookingCreateRequest(
                mentorId,
                menteeId,
                "Sessão de Teste Automatizado",
                "Verificação de invariantes financeiras e temporais.",
                LocalDateTime.of(date, LocalTime.of(startHour, 0)),
                LocalDateTime.of(date, LocalTime.of(endHour, 0)),
                null
        );
        MvcResult res = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    private List<Integer> executeConcurrentPayments(List<Long> bookingIds) throws Exception {
        int threads = bookingIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (Long bookingId : bookingIds) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                int status = mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", bookingId))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                statuses.add(status);
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();
        return statuses;
    }
}
