package com.portfolio.mentorflow.controller;

import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import com.portfolio.mentorflow.dto.AvailabilitySlotRequest;
import com.portfolio.mentorflow.dto.AvailabilitySlotResponse;
import com.portfolio.mentorflow.dto.MentorCreateRequest;
import com.portfolio.mentorflow.dto.MentorResponse;
import com.portfolio.mentorflow.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mentors")
@Tag(name = "1. Mentores & Disponibilidade", description = "Catálogo de mentores, especialidades, saldos de carteira e janelas de agenda")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @GetMapping
    @Operation(summary = "Listar mentores", description = "Retorna todos os mentores ordenados por avaliação e sessões concluídas, com filtro opcional por especialidade.")
    public List<MentorResponse> listMentors(@RequestParam(required = false) MentorSpecialty specialty) {
        return mentorService.listMentors(specialty);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de um mentor", description = "Retorna o perfil completo do mentor e suas janelas semanais de atendimento.")
    public MentorResponse getMentorById(@PathVariable Long id) {
        return mentorService.getMentorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo mentor", description = "Registra um novo mentor na plataforma com janelas padrão de atendimento semanal.")
    public MentorResponse createMentor(@Valid @RequestBody MentorCreateRequest request) {
        return mentorService.createMentor(request);
    }

    @PostMapping("/{id}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar janela de disponibilidade", description = "Adiciona um novo intervalo recorrente de atendimento na agenda semanal do mentor.")
    public AvailabilitySlotResponse addAvailabilitySlot(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilitySlotRequest request) {
        return mentorService.addAvailabilitySlot(id, request);
    }
}
