package com.portfolio.mentorflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cancelamento e estorno financeiro de mentoria")
public record BookingCancelRequest(
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        @Size(min = 5, max = 300, message = "O motivo deve ter entre 5 e 300 caracteres")
        @Schema(description = "Justificativa para cancelamento e liberação do horário", example = "Imprevisto no trabalho, precisarei reagendar para a próxima semana.")
        String reason
) {
}
