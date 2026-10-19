package com.portfolio.mentorflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "Janela de disponibilidade semanal do mentor")
public record AvailabilitySlotRequest(
        @NotNull(message = "O dia da semana é obrigatório")
        @Schema(description = "Dia da semana", example = "TUESDAY")
        DayOfWeek dayOfWeek,

        @NotNull(message = "O horário de início é obrigatório")
        @Schema(description = "Horário inicial disponível", example = "18:00")
        LocalTime startTime,

        @NotNull(message = "O horário de término é obrigatório")
        @Schema(description = "Horário final disponível", example = "22:00")
        LocalTime endTime
) {
}
