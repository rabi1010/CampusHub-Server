package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(
        origins = {"${app.frontend.url}", "https://campushub-n6bn.onrender.com"},
        allowCredentials = "true"
)
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // GET /api/stats/admin
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StatsService.AdminStats>> getAdminStats() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stats fetched",
                        statsService.getAdminStats()
                )
        );
    }
}