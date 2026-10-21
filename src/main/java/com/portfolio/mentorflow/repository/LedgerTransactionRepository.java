package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.LedgerTransaction;
import com.portfolio.mentorflow.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    @Query("""
        SELECT l FROM LedgerTransaction l
        JOIN FETCH l.booking b
        JOIN FETCH b.mentor
        JOIN FETCH b.mentee
        ORDER BY l.createdAt DESC
        """)
    List<LedgerTransaction> findAllWithBookingDetailsOrderByCreatedAtDesc();

    List<LedgerTransaction> findByBookingIdOrderByIdAsc(Long bookingId);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTransaction l WHERE l.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);
}
