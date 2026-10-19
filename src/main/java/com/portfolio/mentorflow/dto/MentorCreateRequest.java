package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Dados para cadastro de um novo Mentor na plataforma")
public record MentorCreateRequest(
        @NotBlank(message = "O nome do mentor é obrigatório")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        @Schema(description = "Nome completo do mentor", example = "Rafael Costa")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Schema(description = "E-mail corporativo ou profissional", example = "rafael.costa@techmentor.io")
        String email,

        @NotBlank(message = "O cargo atual é obrigatório")
        @Schema(description = "Cargo atual do mentor", example = "Staff Backend Engineer")
        String title,

        @NotBlank(message = "A empresa é obrigatória")
        @Schema(description = "Empresa onde atua", example = "Itaú Unibanco")
        String company,

        @NotBlank(message = "A biografia é obrigatória")
        @Size(min = 20, max = 1000, message = "A biografia deve ter entre 20 e 1000 caracteres")
        @Schema(description = "Resumo de experiência e foco de mentoria", example = "Especialista em Java, Spring Boot e sistemas financeiros de alta disponibilidade.")
        String bio,

        @NotNull(message = "A especialidade principal é obrigatória")
        @Schema(description = "Trilha principal de atuação", example = "BACKEND_JAVA")
        MentorSpecialty specialty,

        @NotBlank(message = "As tecnologias/skills são obrigatórias")
        @Schema(description = "Lista de habilidades separadas por vírgula", example = "Java 21, Spring Boot, Kafka, PostgreSQL, Microsserviços")
        String skills,

        @NotNull(message = "O valor da hora de mentoria é obrigatório")
        @DecimalMin(value = "30.00", message = "O valor mínimo por hora é R$ 30,00")
        @DecimalMax(value = "1500.00", message = "O valor máximo por hora é R$ 1.500,00")
        @Schema(description = "Valor cobrado por hora (BRL)", example = "180.00")
        BigDecimal hourlyRate
) {
}
