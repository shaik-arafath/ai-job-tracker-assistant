package com.aijobtracker.app.job;

import com.aijobtracker.app.user.User;
import com.aijobtracker.app.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public record JobRequest(
            @NotBlank String companyName,
            @NotBlank String roleTitle,
            @NotBlank String platform,
            String jobUrl,
            JobStatus status,
            String notes,
            LocalDate appliedDate
    ) {}

    @GetMapping
    public List<Job> list(@RequestHeader("X-User-Id") Long userId, @RequestParam(required = false) JobStatus status) {
        if (status != null) return jobRepository.findByUserIdAndStatus(userId, status);
        return jobRepository.findByUserId(userId);
    }

    @PostMapping
    public Job create(@RequestHeader("X-User-Id") Long userId, @RequestBody JobRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Job job = new Job();
        job.setUser(user);
        job.setCompanyName(request.companyName());
        job.setRoleTitle(request.roleTitle());
        job.setPlatform(request.platform());
        job.setJobUrl(request.jobUrl());
        job.setStatus(request.status() == null ? JobStatus.SAVED : request.status());
        job.setNotes(request.notes());
        job.setAppliedDate(request.appliedDate());
        return jobRepository.save(job);
    }

    @PatchMapping("/{id}/status")
    public Job updateStatus(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id, @RequestParam JobStatus status) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (!job.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        job.setStatus(status);
        return jobRepository.save(job);
    }
}
