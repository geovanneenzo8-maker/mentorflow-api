package com.portfolio.mentorflow.dto;

import com.portfolio.mentorflow.domain.entity.LedgerTransaction;
import com.portfolio.mentorflow.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerTransactionResponse(
        Long id,
        Long bookingId,
        String mentorName,
        String menteeName,
        TransactionType type,
        String typeDescription,
        BigDecimal amount,
        String description,
        String referenceCode,
        LocalDateTime createdAt
) {
    public static LedgerTransactionResponse fromEntity(LedgerTransaction tx) {
        return new LedgerTransactionResponse(
                tx.getId(),
                tx.getBooking().getId(),
                tx.getBooking().getMentor().getName(),
                tx.getBooking().getMentee().getName(),
                tx.getType(),
                tx.getType().getDescription(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getReferenceCode(),
                tx.getCreatedAt()
        );
    }
}
