package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.AvailabilitySlot;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilitySlotResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
    public static AvailabilitySlotResponse fromEntity(AvailabilitySlot slot) {
        return new AvailabilitySlotResponse(
                slot.getId(),
                slot.getDayOfWeek(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.isActive()
        );
    }
}
