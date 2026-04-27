package com.aijobtracker.app.interview;

import com.aijobtracker.app.job.Job;
import com.aijobtracker.app.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(name = "interview_title", nullable = false)
    private String interviewTitle;

    @Column(name = "interview_at", nullable = false)
    private LocalDateTime interviewAt;

    @Column(nullable = false)
    private String mode;

    @Column(name = "reminder_minutes_before", nullable = false)
    private Integer reminderMinutesBefore = 30;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
