package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.enums.MentorSpecialty;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record MentorResponse(
        Long id,
        String name,
        String email,
        String title,
        String company,
        String bio,
        MentorSpecialty specialty,
        String specialtyLabel,
        List<String> skills,
        BigDecimal hourlyRate,
        BigDecimal walletBalance,
        Double averageRating,
        Integer totalReviews,
        Integer totalSessionsCompleted,
        Boolean verified,
        List<AvailabilitySlotResponse> availabilitySlots
) {
    public static MentorResponse fromEntity(Mentor mentor, List<AvailabilitySlotResponse> slots) {
        List<String> parsedSkills = mentor.getSkills() == null
                ? List.of()
                : Arrays.stream(mentor.getSkills().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        return new MentorResponse(
                mentor.getId(),
                mentor.getName(),
                mentor.getEmail(),
                mentor.getTitle(),
                mentor.getCompany(),
                mentor.getBio(),
                mentor.getSpecialty(),
                mentor.getSpecialty().getDisplayName(),
                parsedSkills,
                mentor.getHourlyRate(),
                mentor.getWalletBalance(),
                mentor.getAverageRating(),
                mentor.getTotalReviews(),
                mentor.getTotalSessionsCompleted(),
                mentor.getVerified(),
                slots
        );
    }
}
