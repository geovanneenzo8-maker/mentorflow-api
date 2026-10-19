package com.portfolio.mentorflow.repository;

import com.portfolio.mentorflow.domain.entity.Mentee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenteeRepository extends JpaRepository<Mentee, Long> {

    Optional<Mentee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
