package com.portfolio.mentorflow.service;

import com.portfolio.mentorflow.domain.entity.AvailabilitySlot;
import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import com.portfolio.mentorflow.dto.AvailabilitySlotRequest;
import com.portfolio.mentorflow.dto.AvailabilitySlotResponse;
import com.portfolio.mentorflow.dto.MentorCreateRequest;
import com.portfolio.mentorflow.dto.MentorResponse;
import com.portfolio.mentorflow.exception.BusinessRuleException;
import com.portfolio.mentorflow.exception.ResourceNotFoundException;
import com.portfolio.mentorflow.repository.AvailabilitySlotRepository;
import com.portfolio.mentorflow.repository.MentorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public MentorService(MentorRepository mentorRepository,
                         AvailabilitySlotRepository availabilitySlotRepository) {
        this.mentorRepository = mentorRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Transactional(readOnly = true)
    public List<MentorResponse> listMentors(MentorSpecialty specialty) {
        List<Mentor> mentors = (specialty != null)
                ? mentorRepository.findBySpecialtyOrderByAverageRatingDesc(specialty)
                : mentorRepository.findAllByOrderByAverageRatingDescTotalSessionsCompletedDesc();

        return mentors.stream()
                .map(this::toMentorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MentorResponse getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor não encontrado com ID: " + id));
        return toMentorResponse(mentor);
    }

    @Transactional
    public MentorResponse createMentor(MentorCreateRequest request) {
        if (mentorRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException(
                    "DUPLICATE_MENTOR_EMAIL",
                    "Já existe um mentor cadastrado com o e-mail: " + request.email()
            );
        }

        Mentor mentor = new Mentor(
                request.name(),
                request.email().trim().toLowerCase(),
                request.title(),
                request.company(),
                request.bio(),
                request.specialty(),
                request.skills(),
                request.hourlyRate()
        );

        Mentor saved = mentorRepository.save(mentor);

        // Cria janelas padrão de disponibilidade caso seja um novo mentor
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            availabilitySlotRepository.save(new AvailabilitySlot(saved, day, LocalTime.of(9, 0), LocalTime.of(22, 0)));
        }

        return toMentorResponse(saved);
    }

    @Transactional
    public AvailabilitySlotResponse addAvailabilitySlot(Long mentorId, AvailabilitySlotRequest request) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor não encontrado com ID: " + mentorId));

        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessRuleException(
                    "INVALID_SLOT_INTERVAL",
                    "O horário inicial da janela de disponibilidade deve ser anterior ao horário final."
            );
        }

        AvailabilitySlot slot = new AvailabilitySlot(
                mentor,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );
        return AvailabilitySlotResponse.fromEntity(availabilitySlotRepository.save(slot));
    }

    private MentorResponse toMentorResponse(Mentor mentor) {
        List<AvailabilitySlotResponse> slots = availabilitySlotRepository
                .findByMentorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(mentor.getId())
                .stream()
                .map(AvailabilitySlotResponse::fromEntity)
                .toList();
        return MentorResponse.fromEntity(mentor, slots);
    }
}
