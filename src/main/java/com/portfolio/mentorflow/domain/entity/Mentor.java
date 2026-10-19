package com.portfolio.mentorflow.domain.entity;

import com.portfolio.mentorflow.domain.enums.MentorSpecialty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentors", indexes = {
    @Index(name = "idx_mentor_specialty", columnList = "specialty"),
    @Index(name = "idx_mentor_email", columnList = "email", unique = true)
})
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(nullable = false, length = 1000)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MentorSpecialty specialty;

    @Column(nullable = false, length = 400)
    private String skills;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Double averageRating = 5.0;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    @Column(nullable = false)
    private Integer totalSessionsCompleted = 0;

    @Column(nullable = false)
    private Boolean verified = true;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Mentor() {
    }

    public Mentor(String name, String email, String title, String company, String bio,
                  MentorSpecialty specialty, String skills, BigDecimal hourlyRate) {
        this.name = name;
        this.email = email;
        this.title = title;
        this.company = company;
        this.bio = bio;
        this.specialty = specialty;
        this.skills = skills;
        this.hourlyRate = hourlyRate;
        this.walletBalance = BigDecimal.ZERO;
        this.averageRating = 5.0;
        this.totalReviews = 0;
        this.totalSessionsCompleted = 0;
        this.verified = true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.walletBalance == null) {
            this.walletBalance = BigDecimal.ZERO;
        }
    }

    public void creditWallet(BigDecimal amount) {
        this.walletBalance = this.walletBalance.add(amount);
    }

    public void debitWallet(BigDecimal amount) {
        this.walletBalance = this.walletBalance.subtract(amount);
    }

    public void incrementCompletedSessions() {
        this.totalSessionsCompleted = this.totalSessionsCompleted + 1;
    }

    public void updateRatingStats(double newAverage, int newTotalReviews) {
        this.averageRating = Math.round(newAverage * 100.0) / 100.0;
        this.totalReviews = newTotalReviews;
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

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getBio() {
        return bio;
    }

    public MentorSpecialty getSpecialty() {
        return specialty;
    }

    public String getSkills() {
        return skills;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public Integer getTotalSessionsCompleted() {
        return totalSessionsCompleted;
    }

    public Boolean getVerified() {
        return verified;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
