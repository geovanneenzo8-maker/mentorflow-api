package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.Mentee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenteeRepository extends JpaRepository<Mentee, Long> {

    Optional<Mentee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Adquire lock pessimista de escrita (SELECT ... FOR UPDATE) na carteira do Mentorado
     * para impedir Lost Update e Write Skew quando múltiplas transações concorrentes do
     * mesmo aluno tentam debitar ou estornar saldo simultaneamente (inclusive para mentores diferentes).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mentee m WHERE m.id = :id")
    Optional<Mentee> findByIdWithLock(@Param("id") Long id);
}
