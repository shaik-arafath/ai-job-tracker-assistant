package com.aijobtracker.app.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserId(Long userId);
    List<Interview> findByUserIdAndInterviewAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    long countByUserIdAndInterviewAtAfter(Long userId, LocalDateTime dateTime);
}
