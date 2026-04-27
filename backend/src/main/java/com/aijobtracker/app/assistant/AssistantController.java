package com.aijobtracker.app.assistant;

import com.aijobtracker.app.interview.Interview;
import com.aijobtracker.app.interview.InterviewRepository;
import com.aijobtracker.app.job.Job;
import com.aijobtracker.app.job.JobRepository;
import com.aijobtracker.app.job.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class AssistantController {
    private final JobRepository jobRepository;
    private final InterviewRepository interviewRepository;

    public record ChatRequest(String message) {}
    public record ChatResponse(String answer) {}

    @PostMapping("/chat")
    public ChatResponse chat(@RequestHeader("X-User-Id") Long userId, @RequestBody ChatRequest request) {
        String prompt = request.message().toLowerCase();
        if (prompt.contains("tomorrow") && prompt.contains("interview")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Interview> interviews = interviewRepository.findByUserIdAndInterviewAtBetween(
                    userId, tomorrow.atStartOfDay(), tomorrow.plusDays(1).atStartOfDay().minusNanos(1));
            if (interviews.isEmpty()) return new ChatResponse("You have no interviews tomorrow.");
            String out = interviews.stream().map(i -> i.getInterviewTitle() + " at " + i.getInterviewAt()).collect(Collectors.joining(", "));
            return new ChatResponse("Your interviews tomorrow: " + out);
        }
        if (prompt.contains("this week") && prompt.contains("apply")) {
            long count = jobRepository.countByUserIdAndAppliedDateBetween(userId, LocalDate.now().minusDays(7), LocalDate.now());
            return new ChatResponse("You applied to " + count + " jobs this week.");
        }
        if (prompt.contains("pending")) {
            List<Job> pending = jobRepository.findByUserIdAndStatus(userId, JobStatus.SAVED);
            if (pending.isEmpty()) return new ChatResponse("No pending applications.");
            String out = pending.stream().map(j -> j.getCompanyName() + " - " + j.getRoleTitle()).collect(Collectors.joining(", "));
            return new ChatResponse("Pending applications: " + out);
        }
        return new ChatResponse("I can help with interviews tomorrow, this week's applications, and pending applications.");
    }
}
