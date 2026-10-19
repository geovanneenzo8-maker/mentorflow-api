package com.portfolio.mentorflow.domain.entity;

import com.portfolio.mentorflow.domain.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_mentor_time", columnList = "mentor_id, start_time, end_time"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_idempotency", columnList = "idempotency_key", unique = true)
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(length = 800)
    private String notes;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mentorAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee;

    @Column(length = 255)
    private String meetingLink;

    @Column(length = 300)
    private String cancellationReason;

    @Column(name = "idempotency_key", unique = true, length = 80)
    private String idempotencyKey;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Booking() {
    }

    public Booking(Mentor mentor, Mentee mentee, String topic, String notes,
                   LocalDateTime startTime, LocalDateTime endTime,
                   BigDecimal totalAmount, BigDecimal mentorAmount, BigDecimal platformFee,
                   String idempotencyKey) {
        this.mentor = mentor;
        this.mentee = mentee;
        this.topic = topic;
        this.notes = notes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.PENDING_PAYMENT;
        this.totalAmount = totalAmount;
        this.mentorAmount = mentorAmount;
        this.platformFee = platformFee;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmPayment(String generatedMeetingLink) {
        this.status = BookingStatus.CONFIRMED;
        this.meetingLink = generatedMeetingLink;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeSession() {
        this.status = BookingStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public Mentee getMentee() {
        return mentee;
    }

    public String getTopic() {
        return topic;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getMentorAmount() {
        return mentorAmount;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
