package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.Booking;
import com.portfolio.mentorflow.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long mentorId,
        String mentorName,
        String mentorCompany,
        Long menteeId,
        String menteeName,
        String topic,
        String notes,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long durationMinutes,
        BookingStatus status,
        String statusLabel,
        BigDecimal totalAmount,
        BigDecimal mentorAmount,
        BigDecimal platformFee,
        String meetingLink,
        String cancellationReason,
        String idempotencyKey,
        LocalDateTime createdAt
) {
    public static BookingResponse fromEntity(Booking booking) {
        long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        return new BookingResponse(
                booking.getId(),
                booking.getMentor().getId(),
                booking.getMentor().getName(),
                booking.getMentor().getCompany(),
                booking.getMentee().getId(),
                booking.getMentee().getName(),
                booking.getTopic(),
                booking.getNotes(),
                booking.getStartTime(),
                booking.getEndTime(),
                minutes,
                booking.getStatus(),
                booking.getStatus().getLabel(),
                booking.getTotalAmount(),
                booking.getMentorAmount(),
                booking.getPlatformFee(),
                booking.getMeetingLink(),
                booking.getCancellationReason(),
                booking.getIdempotencyKey(),
                booking.getCreatedAt()
        );
    }
}
