package com.portfolio.mentorflow.config;

import com.portfolio.mentorflow.domain.entity.AvailabilitySlot;
import com.portfolio.mentorflow.domain.entity.Mentee;
import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import com.portfolio.mentorflow.dto.BookingCreateRequest;
import com.portfolio.mentorflow.dto.BookingResponse;
import com.portfolio.mentorflow.dto.ReviewCreateRequest;
import com.portfolio.mentorflow.repository.AvailabilitySlotRepository;
import com.portfolio.mentorflow.repository.MenteeRepository;
import com.portfolio.mentorflow.repository.MentorRepository;
import com.portfolio.mentorflow.service.BookingService;
import com.portfolio.mentorflow.service.ReviewService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner seedInitialData(
            MentorRepository mentorRepository,
            MenteeRepository menteeRepository,
            AvailabilitySlotRepository availabilitySlotRepository,
            BookingService bookingService,
            ReviewService reviewService) {
        return args -> {
            if (mentorRepository.count() > 0) {
                return;
            }

            Mentor m1 = mentorRepository.save(new Mentor(
                    "Rafael Mendes",
                    "rafael.mendes@mentorflow.dev",
                    "Staff Backend Engineer",
                    "Itaú Unibanco",
                    "Especialista em arquitetura Java/Spring Boot para sistemas bancários de missão crítica, concorrência, mensageria com Kafka e modelagem relacional.",
                    MentorSpecialty.BACKEND_JAVA,
                    "Java 21, Spring Boot 3, JPA/Hibernate, Kafka, PostgreSQL, Testes Integrados",
                    new BigDecimal("180.00")
            ));

            Mentor m2 = mentorRepository.save(new Mentor(
                    "Camila Rocha",
                    "camila.rocha@mentorflow.dev",
                    "Principal Software Architect",
                    "Nubank",
                    "Focada em sistemas distribuídos de alta escala, idempotência financeira, motores de ledger imutável e preparação para entrevistas de System Design.",
                    MentorSpecialty.SYSTEM_DESIGN,
                    "System Design, Microsserviços, Consistência Eventual, Resiliência, Arquitetura Limpa",
                    new BigDecimal("240.00")
            ));

            Mentor m3 = mentorRepository.save(new Mentor(
                    "Lucas Ferreira",
                    "lucas.ferreira@mentorflow.dev",
                    "Senior Cloud & SRE Engineer",
                    "Mercado Livre",
                    "Engenheiro de plataforma e confiabilidade. Ajudo desenvolvedores backend a dominarem Docker, Kubernetes, CI/CD e observabilidade em produção.",
                    MentorSpecialty.CLOUD_DEVOPS,
                    "Docker, Kubernetes, AWS, GitHub Actions, Prometheus, OpenTelemetry",
                    new BigDecimal("190.00")
            ));

            Mentor m4 = mentorRepository.save(new Mentor(
                    "Juliana Martins",
                    "juliana.martins@mentorflow.dev",
                    "Lead Data & AI Engineer",
                    "iFood",
                    "Lidero pipelines de dados em tempo real e integração de modelos de IA em produtos de alto tráfego. Mentoria prática em SQL avançado e arquitetura de dados.",
                    MentorSpecialty.DATA_ENGINEERING,
                    "SQL Tuning, Apache Spark, Python, PostgreSQL, Engenharia de Dados, IA Generativa",
                    new BigDecimal("210.00")
            ));

            Mentor m5 = mentorRepository.save(new Mentor(
                    "Bruno Oliveira",
                    "bruno.oliveira@mentorflow.dev",
                    "Engineering Manager",
                    "Santander Tecnologia",
                    "Mais de 120 entrevistas técnicas conduzidas para vagas de Estágio, Júnior e Pleno. Revisão de código, estruturação de portfólio e simulação de entrevista.",
                    MentorSpecialty.CAREER_INTERVIEW,
                    "Entrevistas Técnicas, Clean Code, SOLID, Portfólio GitHub, Carreira Backend",
                    new BigDecimal("150.00")
            ));

            for (Mentor mentor : List.of(m1, m2, m3, m4, m5)) {
                for (DayOfWeek day : DayOfWeek.values()) {
                    availabilitySlotRepository.save(new AvailabilitySlot(
                            mentor,
                            day,
                            LocalTime.of(8, 0),
                            LocalTime.of(23, 0)
                    ));
                }
            }

            Mentee s1 = menteeRepository.save(new Mentee(
                    "Gabriel Almeida",
                    "gabriel.almeida@dev.br",
                    "Desenvolvedor Backend Java Júnior",
                    "Júnior / Estágio",
                    new BigDecimal("1800.00")
            ));

            Mentee s2 = menteeRepository.save(new Mentee(
                    "Mariana Costa",
                    "mariana.costa@dev.br",
                    "Engenheira de Software Júnior",
                    "Estágio",
                    new BigDecimal("1400.00")
            ));

            Mentee s3 = menteeRepository.save(new Mentee(
                    "Pedro Henrique",
                    "pedro.henrique@dev.br",
                    "Desenvolvedor Full-Stack / Backend",
                    "Transição de Carreira",
                    new BigDecimal("1600.00")
            ));

            LocalDate baseDate = LocalDate.now();

            // Sessão 1: Concluída e Avaliada
            BookingResponse b1 = bookingService.createBooking(new BookingCreateRequest(
                    m1.getId(),
                    s1.getId(),
                    "Arquitetura Spring Boot, Prevenção de N+1 no JPA e Locks de Concorrência",
                    "Revisar projeto de portfólio antes de enviar para vagas de Backend Júnior.",
                    LocalDateTime.of(baseDate.minusDays(3), LocalTime.of(19, 0)),
                    LocalDateTime.of(baseDate.minusDays(3), LocalTime.of(20, 0)),
                    "seed-booking-001"
            ));
            bookingService.confirmPayment(b1.id());
            bookingService.completeBooking(b1.id());
            reviewService.createReview(new ReviewCreateRequest(
                    b1.id(),
                    5,
                    "Mentoria sensacional! O Rafael explicou na prática como usar Pessimistic Lock e RFC 7807 ProblemDetail no Spring Boot 3."
            ));

            // Sessão 2: Concluída e Avaliada
            BookingResponse b2 = bookingService.createBooking(new BookingCreateRequest(
                    m2.getId(),
                    s2.getId(),
                    "System Design: Modelando Carteira Digital, Split de Pagamento e Ledger Imutável",
                    "Entender como fintechs garantem consistência transacional sem perder centavos.",
                    LocalDateTime.of(baseDate.minusDays(2), LocalTime.of(18, 0)),
                    LocalDateTime.of(baseDate.minusDays(2), LocalTime.of(19, 30)),
                    "seed-booking-002"
            ));
            bookingService.confirmPayment(b2.id());
            bookingService.completeBooking(b2.id());
            reviewService.createReview(new ReviewCreateRequest(
                    b2.id(),
                    5,
                    "Didática incrível da Camila! Saí da sessão com o desenho completo da arquitetura para defender nas entrevistas técnicas."
            ));

            // Sessão 3: Confirmada para os próximos dias (ótima para testar conflito de horário 409!)
            BookingResponse b3 = bookingService.createBooking(new BookingCreateRequest(
                    m1.getId(),
                    s3.getId(),
                    "Simulação de Entrevista Técnica Java + Revisão de Testes JUnit/MockMvc",
                    "Praticar perguntas de transações (@Transactional), isolamento no banco e APIs REST.",
                    LocalDateTime.of(baseDate.plusDays(2), LocalTime.of(19, 0)),
                    LocalDateTime.of(baseDate.plusDays(2), LocalTime.of(20, 0)),
                    "seed-booking-003"
            ));
            bookingService.confirmPayment(b3.id());

            // Sessão 4: Aguardando Pagamento (pronta para testar confirmação de split 85%/15% em 1 clique!)
            bookingService.createBooking(new BookingCreateRequest(
                    m3.getId(),
                    s1.getId(),
                    "Containerização de APIs Spring Boot com Docker Compose e PostgreSQL",
                    "Configurar ambiente pronto para produção e healthcheck com Spring Actuator.",
                    LocalDateTime.of(baseDate.plusDays(4), LocalTime.of(20, 0)),
                    LocalDateTime.of(baseDate.plusDays(4), LocalTime.of(21, 0)),
                    "seed-booking-004"
            ));
        };
    }
}
