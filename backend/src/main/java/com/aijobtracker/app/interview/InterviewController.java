package com.aijobtracker.app.interview;

import com.aijobtracker.app.job.Job;
import com.aijobtracker.app.job.JobRepository;
import com.aijobtracker.app.user.User;
import com.aijobtracker.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public record InterviewRequest(String interviewTitle, LocalDateTime interviewAt, String mode, Integer reminderMinutesBefore, String notes, Long jobId) {}

    @GetMapping
    public List<Interview> list(@RequestHeader("X-User-Id") Long userId) {
        return interviewRepository.findByUserId(userId);
    }

    @GetMapping("/tomorrow")
    public List<Interview> tomorrow(@RequestHeader("X-User-Id") Long userId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return interviewRepository.findByUserIdAndInterviewAtBetween(
                userId,
                tomorrow.atStartOfDay(),
                tomorrow.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @PostMapping
    public Interview create(@RequestHeader("X-User-Id") Long userId, @RequestBody InterviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Interview interview = new Interview();
        interview.setUser(user);
        interview.setInterviewTitle(request.interviewTitle());
        interview.setInterviewAt(request.interviewAt());
        interview.setMode(request.mode());
        interview.setReminderMinutesBefore(request.reminderMinutesBefore() == null ? 30 : request.reminderMinutesBefore());
        interview.setNotes(request.notes());
        if (request.jobId() != null) {
            Job job = jobRepository.findById(request.jobId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
            interview.setJob(job);
        }
        return interviewRepository.save(interview);
    }
}
