package com.aijobtracker.app.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserId(Long userId);
    List<Job> findByUserIdAndStatus(Long userId, JobStatus status);
    long countByUserIdAndAppliedDateBetween(Long userId, LocalDate start, LocalDate end);
}
