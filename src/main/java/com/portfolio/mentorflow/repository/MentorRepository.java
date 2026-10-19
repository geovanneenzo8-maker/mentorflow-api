package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.Mentor;
import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Mentor> findBySpecialtyOrderByAverageRatingDesc(MentorSpecialty specialty);

    List<Mentor> findAllByOrderByAverageRatingDescTotalSessionsCompletedDesc();

    /**
     * Adquire lock pessimista de escrita na linha do Mentor para serializar
     * tentativas concorrentes de agendamento para o mesmo profissional,
     * prevenindo a condição de corrida "check-then-act" (Double-Booking).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mentor m WHERE m.id = :id")
    Optional<Mentor> findByIdWithLock(@Param("id") Long id);
}
