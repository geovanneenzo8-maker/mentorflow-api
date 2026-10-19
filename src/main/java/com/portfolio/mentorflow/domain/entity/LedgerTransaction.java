package com.portfolio.mentorflow.domain.entity;

import com.portfolio.mentorflow.domain.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_transactions", indexes = {
    @Index(name = "idx_ledger_booking", columnList = "booking_id"),
    @Index(name = "idx_ledger_type", columnList = "type")
})
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, updatable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private TransactionType type;

    @Column(nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, length = 250)
    private String description;

    @Column(nullable = false, updatable = false, unique = true, length = 60)
    private String referenceCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LedgerTransaction() {
    }

    public LedgerTransaction(Booking booking, TransactionType type, BigDecimal amount,
                             String description, String referenceCode) {
        this.booking = booking;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.referenceCode = referenceCode;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
