package com.portfolio.mentorflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.mentorflow.dto.BookingCancelRequest;
import com.portfolio.mentorflow.dto.BookingCreateRequest;
import com.portfolio.mentorflow.dto.ReviewCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MentorflowApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Deve listar mentores cadastrados e filtrar por especialidade")
    void shouldListMentorsAndFilterBySpecialty() throws Exception {
        mockMvc.perform(get("/api/v1/mentors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/api/v1/mentors").param("specialty", "BACKEND_JAVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("BACKEND_JAVA"))
                .andExpect(jsonPath("$[0].company").value("Itaú Unibanco"));
    }

    @Test
    @DisplayName("2. Deve criar agendamento e calcular exatamente o Split 85% Mentor / 15% Plataforma")
    void shouldCreateBookingAndCalculateFinancialSplit() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(10);
        BookingCreateRequest request = new BookingCreateRequest(
                1L,
                1L,
                "Mentoria de Arquitetura Limpa e Concorrência",
                "Testando cálculo de split 85/15 para sessão de 1 hora (R$ 180,00).",
                LocalDateTime.of(targetDate, LocalTime.of(10, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(11, 0)),
                "test-split-key-001"
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(180.00))
                .andExpect(jsonPath("$.mentorAmount").value(153.00))
                .andExpect(jsonPath("$.platformFee").value(27.00));
    }

    @Test
    @DisplayName("3. Deve garantir idempotência ao receber a mesma idempotencyKey mais de uma vez")
    void shouldGuaranteeIdempotencyOnDuplicateRequest() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(11);
        BookingCreateRequest request = new BookingCreateRequest(
                2L,
                1L,
                "Mentoria System Design com Idempotência",
                "Evitar duplicidade em retentativas de rede.",
                LocalDateTime.of(targetDate, LocalTime.of(14, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(15, 0)),
                "idempotent-key-unique-999"
        );

        MvcResult firstCall = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult secondCall = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode firstJson = objectMapper.readTree(firstCall.getResponse().getContentAsString());
        JsonNode secondJson = objectMapper.readTree(secondCall.getResponse().getContentAsString());

        assertThat(secondJson.get("id").asLong()).isEqualTo(firstJson.get("id").asLong());
    }

    @Test
    @DisplayName("4. Deve bloquear Double-Booking (sobreposição de horário) retornando HTTP 409 Conflict (RFC 7807)")
    void shouldBlockOverlappingBookingsWith409Conflict() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(12);

        // Primeira reserva: 14:00 às 15:00
        BookingCreateRequest firstBooking = new BookingCreateRequest(
                1L,
                1L,
                "Sessão Original das 14h às 15h",
                "Reserva inicial válida.",
                LocalDateTime.of(targetDate, LocalTime.of(14, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(15, 0)),
                null
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstBooking)))
                .andExpect(status().isCreated());

        // Segunda reserva conflitante (parcialmente sobreposta: 14:30 às 15:30 para o mesmo mentor)
        BookingCreateRequest conflictingBooking = new BookingCreateRequest(
                1L,
                2L,
                "Sessão Conflitante das 14h30 às 15h30",
                "Deve ser barrada pelo detector de overlap.",
                LocalDateTime.of(targetDate, LocalTime.of(14, 30)),
                LocalDateTime.of(targetDate, LocalTime.of(15, 30)),
                null
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingBooking)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("SCHEDULE_OVERLAP_CONFLICT"))
                .andExpect(jsonPath("$.conflictingBookingId").isNumber());
    }

    @Test
    @DisplayName("5. Deve executar o ciclo completo: Reserva -> Confirmação de Pagamento -> Conclusão -> Avaliação")
    void shouldExecuteFullBookingLifecycleAndReview() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(14);
        BookingCreateRequest createReq = new BookingCreateRequest(
                3L,
                1L,
                "Deploy com Kubernetes e Observabilidade",
                "Ciclo completo de mentoria.",
                LocalDateTime.of(targetDate, LocalTime.of(16, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(17, 0)),
                null
        );

        MvcResult createdRes = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long bookingId = objectMapper.readTree(createdRes.getResponse().getContentAsString()).get("id").asLong();

        // Confirmar Pagamento (PENDING_PAYMENT -> CONFIRMED)
        mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.meetingLink").isNotEmpty());

        // Concluir Sessão (CONFIRMED -> COMPLETED)
        mockMvc.perform(post("/api/v1/bookings/{id}/complete", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Avaliar Sessão Concluída
        ReviewCreateRequest reviewReq = new ReviewCreateRequest(
                bookingId,
                5,
                "Excelente explicação prática sobre observabilidade, métricas e containers!"
        );

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("6. Deve estornar valores ao cancelar mentoria paga e liberar o horário imediatamente")
    void shouldCancelPaidBookingRefundAndReleaseSlot() throws Exception {
        LocalDate targetDate = LocalDate.now().plusDays(15);
        BookingCreateRequest createReq = new BookingCreateRequest(
                4L,
                2L,
                "Mentoria Engenharia de Dados e Spark",
                "Teste de cancelamento com estorno e liberação de agenda.",
                LocalDateTime.of(targetDate, LocalTime.of(18, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(19, 0)),
                null
        );

        MvcResult createdRes = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long bookingId = objectMapper.readTree(createdRes.getResponse().getContentAsString()).get("id").asLong();

        // Confirma o pagamento
        mockMvc.perform(post("/api/v1/bookings/{id}/confirm-payment", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Cancela com estorno
        BookingCancelRequest cancelReq = new BookingCancelRequest("Reagendamento solicitado pelo aluno com antecedência.");
        mockMvc.perform(post("/api/v1/bookings/{id}/cancel", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verifica que o mesmo horário agora está livre para outro agendamento
        BookingCreateRequest rebookReq = new BookingCreateRequest(
                4L,
                3L,
                "Nova Reserva no Horário Liberado",
                "Deve permitir agendar pois o anterior foi cancelado.",
                LocalDateTime.of(targetDate, LocalTime.of(18, 0)),
                LocalDateTime.of(targetDate, LocalTime.of(19, 0)),
                null
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rebookReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }
}
