package com.portfolio.mentorflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para avaliação de uma mentoria concluída")
public record ReviewCreateRequest(
        @NotNull(message = "O ID do agendamento é obrigatório")
        @Schema(description = "ID da sessão de mentoria concluída", example = "1")
        Long bookingId,

        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota mínima é 1")
        @Max(value = 5, message = "A nota máxima é 5")
        @Schema(description = "Nota de 1 a 5 estrelas", example = "5")
        Integer rating,

        @NotBlank(message = "O comentário é obrigatório")
        @Size(min = 10, max = 800, message = "O comentário deve ter entre 10 e 800 caracteres")
        @Schema(description = "Depoimento sobre a mentoria", example = "Mentoria excepcional! Revisamos toda a arquitetura de microsserviços e destravei nos testes técnicos.")
        String comment
) {
}
