package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByMentorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long mentorId);

    List<AvailabilitySlot> findByMentorIdAndDayOfWeekAndActiveTrue(Long mentorId, DayOfWeek dayOfWeek);
}
