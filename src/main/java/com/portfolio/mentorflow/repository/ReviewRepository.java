package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.mentor
        JOIN FETCH r.mentee
        WHERE r.mentor.id = :mentorId
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByMentorIdWithDetails(@Param("mentorId") Long mentorId);

    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM Review r WHERE r.mentor.id = :mentorId")
    Double calculateAverageRatingByMentorId(@Param("mentorId") Long mentorId);

    long countByMentorId(Long mentorId);
}
