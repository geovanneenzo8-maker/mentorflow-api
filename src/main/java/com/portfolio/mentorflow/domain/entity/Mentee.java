package com.portfolio.mentorflow.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentees", indexes = {
    @Index(name = "idx_mentee_email", columnList = "email", unique = true)
})
public class Mentee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String roleTarget;

    @Column(nullable = false, length = 60)
    private String careerLevel;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Mentee() {
    }

    public Mentee(String name, String email, String roleTarget, String careerLevel, BigDecimal walletBalance) {
        this.name = name;
        this.email = email;
        this.roleTarget = roleTarget;
        this.careerLevel = careerLevel;
        this.walletBalance = walletBalance;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.walletBalance.compareTo(amount) >= 0;
    }

    public void debitWallet(BigDecimal amount) {
        this.walletBalance = this.walletBalance.subtract(amount);
    }

    public void creditWallet(BigDecimal amount) {
        this.walletBalance = this.walletBalance.add(amount);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleTarget() {
        return roleTarget;
    }

    public String getCareerLevel() {
        return careerLevel;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
