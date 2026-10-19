package com.portfolio.mentorflow.domain.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "availability_slots", indexes = {
    @Index(name = "idx_slot_mentor_day", columnList = "mentor_id, day_of_week")
})
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    protected AvailabilitySlot() {
    }

    public AvailabilitySlot(Mentor mentor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.mentor = mentor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
    }

    public boolean coversInterval(LocalTime requestedStart, LocalTime requestedEnd) {
        return active
                && !requestedStart.isBefore(this.startTime)
                && !requestedEnd.isAfter(this.endTime);
    }

    public Long getId() {
        return id;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }
}
