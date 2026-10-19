package com.portfolio.mentorflow.controller;

import com.portfolio.mentorflow.dto.BookingCancelRequest;
import com.portfolio.mentorflow.dto.BookingCreateRequest;
import com.portfolio.mentorflow.dto.BookingResponse;
import com.portfolio.mentorflow.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "3. Agendamentos & Motor de Split", description = "Motor de reservas com prevenção de concorrência (Double-Booking), máquina de estados e split de pagamentos")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos", description = "Lista todos os agendamentos de mentorias com dados de split financeiro e status atual.")
    public List<BookingResponse> listBookings(@RequestParam(required = false) Long mentorId) {
        return bookingService.listAllBookings(mentorId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID", description = "Retorna os detalhes completos de uma sessão agendada.")
    public BookingResponse getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar novo agendamento de mentoria",
            description = "Reserva um horário aplicando Pessimistic Write Lock no Mentor, detecção de interseção temporal (Overlap) e cálculo de split 85%/15%."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso (PENDING_PAYMENT)"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário detectado (Double-Booking bloqueado - RFC 7807)"),
            @ApiResponse(responseCode = "422", description = "Violação de regra de negócio (duração inválida ou fora da janela do mentor)")
    })
    public BookingResponse createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return bookingService.createBooking(request);
    }

    @PostMapping("/{id}/confirm-payment")
    @Operation(
            summary = "Confirmar pagamento e executar Split (85% Mentor / 15% Plataforma)",
            description = "Debita o saldo do mentorado, credita 85% na carteira do mentor, retém 15% de taxa da plataforma, registra 3 transações no Ledger imutável e gera o link da videochamada."
    )
    public BookingResponse confirmPayment(@PathVariable Long id) {
        return bookingService.confirmPayment(id);
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Concluir sessão de mentoria",
            description = "Transiciona o agendamento de CONFIRMED para COMPLETED e incrementa o contador de mentorias realizadas pelo mentor."
    )
    public BookingResponse completeBooking(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancelar mentoria com estorno atômico",
            description = "Cancela o agendamento, libera imediatamente o horário na agenda do mentor e, caso já estivesse pago, realiza o estorno financeiro automático."
    )
    public BookingResponse cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingCancelRequest request) {
        return bookingService.cancelBooking(id, request);
    }
}
