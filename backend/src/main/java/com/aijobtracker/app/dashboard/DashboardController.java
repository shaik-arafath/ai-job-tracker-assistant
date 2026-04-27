package com.aijobtracker.app.dashboard;

import com.aijobtracker.app.interview.InterviewRepository;
import com.aijobtracker.app.job.JobRepository;
import com.aijobtracker.app.job.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final JobRepository jobRepository;
    private final InterviewRepository interviewRepository;

    @GetMapping("/summary")
    public Map<String, Long> summary(@RequestHeader("X-User-Id") Long userId) {
        long total = jobRepository.findByUserId(userId).size();
        long thisWeek = jobRepository.countByUserIdAndAppliedDateBetween(userId, LocalDate.now().minusDays(7), LocalDate.now());
        long interviewsUpcoming = interviewRepository.countByUserIdAndInterviewAtAfter(userId, LocalDateTime.now());
        long offers = jobRepository.findByUserIdAndStatus(userId, JobStatus.OFFER).size();
        return Map.of(
                "totalApplications", total,
                "appliedThisWeek", thisWeek,
                "interviewsUpcoming", interviewsUpcoming,
                "offers", offers
        );
    }
}
