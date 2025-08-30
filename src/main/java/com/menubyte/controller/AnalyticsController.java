package com.menubyte.controller;

import com.menubyte.dto.AnalyticsResponseDTO;
import com.menubyte.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/business/{businessId}/sales-trend")
    public ResponseEntity<AnalyticsResponseDTO> getSalesTrend(
            @PathVariable Long businessId,
            @RequestParam(name = "period", defaultValue = "7d") String period) {

        AnalyticsResponseDTO response = analyticsService.getSalesAnalytics(businessId, period);
        return ResponseEntity.ok(response);
    }
}