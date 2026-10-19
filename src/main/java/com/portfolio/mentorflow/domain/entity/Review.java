package com.portfolio.mentorflow.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_mentor", columnList = "mentor_id"),
    @Index(name = "idx_review_booking", columnList = "booking_id", unique = true)
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 800)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Review() {
    }

    public Review(Booking booking, Mentor mentor, Mentee mentee, Integer rating, String comment) {
        this.booking = booking;
        this.mentor = mentor;
        this.mentee = mentee;
        this.rating = rating;
        this.comment = comment;
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

    public Mentor getMentor() {
        return mentor;
    }

    public Mentee getMentee() {
        return mentee;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
