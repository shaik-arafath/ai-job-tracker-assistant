package com.aijobtracker.app.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("message", "File is empty");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            return Map.of("message", "Only PDF resumes are accepted");
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            List<String> skills = extractSkills(text);
            return Map.of(
                    "fileName", file.getOriginalFilename(),
                    "message", "Resume uploaded and parsed successfully",
                    "pageCount", document.getNumberOfPages(),
                    "extractedSkills", skills,
                    "textPreview", preview(text)
            );
        } catch (IOException ex) {
            return Map.of("message", "Failed to parse PDF resume");
        }
    }

    @PostMapping("/{resumeId}/analyze-job/{jobId}")
    public Map<String, Object> analyze(@PathVariable Long resumeId, @PathVariable Long jobId, @RequestBody Map<String, String> body) {
        String jobDescription = body.getOrDefault("jobDescription", "");
        int score = Math.min(95, Math.max(55, 60 + (jobDescription.length() % 30)));
        return Map.of(
                "resumeId", resumeId,
                "jobId", jobId,
                "matchScore", score,
                "missingSkills", List.of("System Design", "Docker"),
                "summary", "Strong backend fit. Improve deployment and architecture storytelling."
        );
    }

    private static List<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        return Stream.of("java", "spring boot", "mysql", "react", "javascript", "typescript", "docker", "aws", "kubernetes", "rest api")
                .filter(lower::contains)
                .map(skill -> {
                    if ("rest api".equals(skill)) return "REST API";
                    if ("aws".equals(skill)) return "AWS";
                    return Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
                })
                .collect(Collectors.toList());
    }

    private static String preview(String text) {
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= 300 ? cleaned : cleaned.substring(0, 300) + "...";
    }
}
