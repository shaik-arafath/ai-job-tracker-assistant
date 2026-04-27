package com.aijobtracker.app.job;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobImportController {
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern OG_TITLE_PATTERN = Pattern.compile("property=[\"']og:title[\"'][^>]*content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern OG_SITE_PATTERN = Pattern.compile("property=[\"']og:site_name[\"'][^>]*content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);

    @PostMapping("/import-from-url")
    public Map<String, String> importFromUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url is required");
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only public http/https URLs are allowed");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "AIJobTracker/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String html = response.body();
            return Map.of(
                    "url", url,
                    "title", firstGroup(OG_TITLE_PATTERN, html, firstGroup(TITLE_PATTERN, html, "Unknown Role")),
                    "platform", firstGroup(OG_SITE_PATTERN, html, "Imported"),
                    "status", "SAVED",
                    "message", "Imported public page metadata. Please review and confirm fields before saving."
            );
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch public job URL");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL");
        }
    }

    private static String firstGroup(Pattern pattern, String html, String fallback) {
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) return fallback;
        return matcher.group(1).replaceAll("\\s+", " ").trim();
    }
}
