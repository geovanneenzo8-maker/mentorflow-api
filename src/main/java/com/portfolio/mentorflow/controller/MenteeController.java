package com.portfolio.mentorflow.controller;

import com.portfolio.mentorflow.domain.entity.Mentee;
import com.portfolio.mentorflow.dto.MenteeResponse;
import com.portfolio.mentorflow.exception.ResourceNotFoundException;
import com.portfolio.mentorflow.repository.MenteeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mentees")
@Tag(name = "2. Mentorados & Carteira", description = "Perfis de alunos/mentorados e consulta de saldo em carteira")
public class MenteeController {

    private final MenteeRepository menteeRepository;

    public MenteeController(MenteeRepository menteeRepository) {
        this.menteeRepository = menteeRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar mentorados", description = "Retorna os perfis de mentorados e seus respectivos saldos em carteira.")
    public List<MenteeResponse> listMentees() {
        return menteeRepository.findAll()
                .stream()
                .map(MenteeResponse::fromEntity)
                .toList();
    }

    @PostMapping("/{id}/topup")
    @Transactional
    @Operation(summary = "Adicionar créditos na carteira do mentorado", description = "Recarrega o saldo da carteira do mentorado com lock pessimista na linha.")
    public MenteeResponse topUpWallet(@PathVariable Long id, @RequestParam(defaultValue = "500.00") BigDecimal amount) {
        Mentee mentee = menteeRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorado não encontrado com ID: " + id));
        mentee.creditWallet(amount);
        return MenteeResponse.fromEntity(mentee);
    }
}
