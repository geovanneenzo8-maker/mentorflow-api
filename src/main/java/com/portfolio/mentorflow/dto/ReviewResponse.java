package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long bookingId,
        Long mentorId,
        String mentorName,
        String menteeName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBooking().getId(),
                review.getMentor().getId(),
                review.getMentor().getName(),
                review.getMentee().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
