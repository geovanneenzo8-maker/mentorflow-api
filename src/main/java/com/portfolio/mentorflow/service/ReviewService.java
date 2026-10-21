package com.portfolio.mentorflow.service;

import com.portfolio.mentorflow.domain.entity.Booking;
import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.entity.Review;
import com.portfolio.mentorflow.domain.enums.BookingStatus;
import com.portfolio.mentorflow.dto.ReviewCreateRequest;
import com.portfolio.mentorflow.dto.ReviewResponse;
import com.portfolio.mentorflow.exception.BusinessRuleException;
import com.portfolio.mentorflow.exception.ResourceNotFoundException;
import com.portfolio.mentorflow.repository.BookingRepository;
import com.portfolio.mentorflow.repository.MentorRepository;
import com.portfolio.mentorflow.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MentorRepository mentorRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         MentorRepository mentorRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.mentorRepository = mentorRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorIdWithDetails(mentorId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        Booking booking = bookingRepository.findByIdWithLock(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + request.bookingId()));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "BOOKING_NOT_COMPLETED",
                    "Apenas mentorias com status COMPLETED podem receber avaliação. Status atual: " + booking.getStatus()
            );
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BusinessRuleException(
                    "DUPLICATE_REVIEW",
                    "Esta sessão de mentoria (#" + booking.getId() + ") já foi avaliada anteriormente."
            );
        }

        Mentor mentor = mentorRepository.findByIdWithLock(booking.getMentor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor não encontrado."));

        Review review = new Review(
                booking,
                mentor,
                booking.getMentee(),
                request.rating(),
                request.comment().trim()
        );

        Review saved = reviewRepository.saveAndFlush(review);

        Double newAverage = reviewRepository.calculateAverageRatingByMentorId(mentor.getId());
        long totalReviews = reviewRepository.countByMentorId(mentor.getId());
        mentor.updateRatingStats(newAverage, (int) totalReviews);

        return ReviewResponse.fromEntity(saved);
    }
}
