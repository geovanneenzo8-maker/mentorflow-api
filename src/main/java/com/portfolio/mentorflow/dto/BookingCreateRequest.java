package com.portfolio.mentorflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Requisição para agendamento de sessão de mentoria com verificação de conflito de horário")
public record BookingCreateRequest(
        @NotNull(message = "O ID do mentor é obrigatório")
        @Schema(description = "ID do mentor selecionado", example = "1")
        Long mentorId,

        @NotNull(message = "O ID do mentorado (aluno) é obrigatório")
        @Schema(description = "ID do mentorado", example = "1")
        Long menteeId,

        @NotBlank(message = "O tema da mentoria é obrigatório")
        @Size(min = 5, max = 180, message = "O tema deve ter entre 5 e 180 caracteres")
        @Schema(description = "Tópico principal da sessão", example = "Revisão de Arquitetura Spring Boot & Preparação para Entrevista")
        String topic,

        @Size(max = 800, message = "As observações devem ter no máximo 800 caracteres")
        @Schema(description = "Contexto adicional ou dúvidas específicas", example = "Quero revisar concorrência no JPA, locks pessimistas/otimistas e design de APIs REST.")
        String notes,

        @NotNull(message = "A data/hora de início é obrigatória")
        @Schema(description = "Data e hora de início (ISO-8601)", example = "2026-10-22T19:00:00")
        LocalDateTime startTime,

        @NotNull(message = "A data/hora de término é obrigatória")
        @Schema(description = "Data e hora de término (ISO-8601)", example = "2026-10-22T20:00:00")
        LocalDateTime endTime,

        @Schema(description = "Chave de idempotência opcional para evitar duplicidade em retentativas de rede", example = "req-booking-20261022-001")
        String idempotencyKey
) {
}
