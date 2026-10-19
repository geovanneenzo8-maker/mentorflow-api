package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.Booking;
import com.portfolio.mentorflow.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    /**
     * Detecta interseção temporal de intervalos [startTime, endTime) para o mesmo mentor.
     * Dois intervalos A e B se sobrepõem se e somente se: A.start < B.end AND A.end > B.start.
     * Ignora agendamentos com status CANCELLED (que liberam o horário automaticamente).
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.mentor.id = :mentorId
          AND b.status IN :activeStatuses
          AND b.startTime < :endTime
          AND b.endTime > :startTime
        """)
    List<Booking> findConflictingBookings(
            @Param("mentorId") Long mentorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );

    /**
     * Utiliza JOIN FETCH para carregar Mentor e Mentee em uma única query SQL,
     * prevenindo o clássico problema de performance N+1 Queries do Hibernate/JPA.
     */
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.mentor
        JOIN FETCH b.mentee
        ORDER BY b.startTime DESC
        """)
    List<Booking> findAllWithDetailsOrderByStartTimeDesc();

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.mentor
        JOIN FETCH b.mentee
        WHERE b.mentor.id = :mentorId
        ORDER BY b.startTime DESC
        """)
    List<Booking> findByMentorIdWithDetails(@Param("mentorId") Long mentorId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.mentor
        JOIN FETCH b.mentee
        WHERE b.id = :id
        """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    long countByStatus(BookingStatus status);
}
