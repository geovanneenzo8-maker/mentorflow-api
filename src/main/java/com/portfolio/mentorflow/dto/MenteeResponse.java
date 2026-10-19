package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.Mentee;
import java.math.BigDecimal;

public record MenteeResponse(
        Long id,
        String name,
        String email,
        String roleTarget,
        String careerLevel,
        BigDecimal walletBalance
) {
    public static MenteeResponse fromEntity(Mentee mentee) {
        return new MenteeResponse(
                mentee.getId(),
                mentee.getName(),
                mentee.getEmail(),
                mentee.getRoleTarget(),
                mentee.getCareerLevel(),
                mentee.getWalletBalance()
        );
    }
}
