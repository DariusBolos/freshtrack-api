package com.freshtrack.api.stats;

import com.freshtrack.api.stats.dto.StatsResponse;
import com.freshtrack.api.stats.service.IStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats-service")
@RequiredArgsConstructor
public class StatsController {
    private final IStatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(statsService.getStats(token));
    }
}
