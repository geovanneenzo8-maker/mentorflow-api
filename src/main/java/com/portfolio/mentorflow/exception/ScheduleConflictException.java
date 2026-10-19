package com.portfolio.mentorflow.exception;

public class ScheduleConflictException extends RuntimeException {
    private final Long conflictingBookingId;

    public ScheduleConflictException(String message, Long conflictingBookingId) {
        super(message);
        this.conflictingBookingId = conflictingBookingId;
    }

    public Long getConflictingBookingId() {
        return conflictingBookingId;
    }
}
