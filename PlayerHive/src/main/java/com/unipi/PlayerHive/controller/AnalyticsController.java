package com.unipi.PlayerHive.controller;

import com.unipi.PlayerHive.DTO.analytics.GenreStatsDTO;
import com.unipi.PlayerHive.DTO.analytics.OsPlatformStatsDTO;
import com.unipi.PlayerHive.DTO.analytics.ReleaseYearStatsDTO;
import com.unipi.PlayerHive.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/genre-stats")
    public ResponseEntity<List<GenreStatsDTO>> getGenreStats() {
        return ResponseEntity.ok(analyticsService.getGenreStats());
    }

    @GetMapping("/os-platform-stats")
    public ResponseEntity<List<OsPlatformStatsDTO>> getOsPlatformStats() {
        return ResponseEntity.ok(analyticsService.getOsPlatformStats());
    }

    @GetMapping("/release-year-stats")
    public ResponseEntity<List<ReleaseYearStatsDTO>> getReleaseYearStats() {
        return ResponseEntity.ok(analyticsService.getReleaseYearStats());
    }
}
