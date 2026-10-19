package com.portfolio.mentorflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mentorFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MentorFlow API — Marketplace & Motor de Agendamento")
                        .version("1.0.0")
                        .description("""
                                API RESTful para Marketplace de Mentorias Técnicas construída com **Java 21** e **Spring Boot 3**.
                                
                                ### Destaques de Engenharia Backend:
                                - **Prevenção de Concorrência (Double-Booking):** Lock pessimista (`PESSIMISTIC_WRITE`) + detecção de interseção temporal de intervalos `[startTime, endTime)`.
                                - **Idempotência:** Suporte a `idempotencyKey` para evitar cobranças ou reservas duplicadas em falhas de rede.
                                - **Motor Financeiro de Split (85% / 15%) & Ledger Imutável:** Cálculo preciso com `BigDecimal` (`HALF_UP`), carteiras digitais e trilha de auditoria contábil (`LedgerTransaction`).
                                - **Tratamento de Erros Padronizado:** Respostas estruturadas no padrão **RFC 7807 (Problem Details for HTTP APIs)**.
                                """)
                        .contact(new Contact()
                                .name("Portfólio Engenharia de Software Backend")
                                .url("https://github.com/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
