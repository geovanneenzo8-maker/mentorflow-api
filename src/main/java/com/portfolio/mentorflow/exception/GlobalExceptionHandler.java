package com.portfolio.mentorflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de exceções seguindo a especificação RFC 7807 (Problem Details for HTTP APIs),
 * nativa no Spring Boot 3 / Spring Framework 6.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso Não Encontrado");
        problem.setType(URI.create("https://mentorflow.portfolio.dev/errors/resource-not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "RESOURCE_NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ProblemDetail handleScheduleConflict(ScheduleConflictException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflito de Agendamento Detectado (Double-Booking Prevention)");
        problem.setType(URI.create("https://mentorflow.portfolio.dev/errors/schedule-conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "SCHEDULE_OVERLAP_CONFLICT");
        problem.setProperty("conflictingBookingId", ex.getConflictingBookingId());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "O registro foi modificado simultaneamente por outra transação concorrente. Tente novamente."
        );
        problem.setTitle("Conflito de Concorrência Otimista (@Version)");
        problem.setType(URI.create("https://mentorflow.portfolio.dev/errors/concurrency-conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "OPTIMISTIC_LOCK_CONFLICT");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Violação de Regra de Negócio");
        problem.setType(URI.create("https://mentorflow.portfolio.dev/errors/business-rule-violation"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisição possuem valores inválidos."
        );
        problem.setTitle("Erro de Validação de Dados (Bean Validation)");
        problem.setType(URI.create("https://mentorflow.portfolio.dev/errors/validation-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "VALIDATION_FAILED");
        problem.setProperty("fieldErrors", fieldErrors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
