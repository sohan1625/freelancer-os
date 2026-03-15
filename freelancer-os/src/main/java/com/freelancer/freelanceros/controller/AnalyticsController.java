package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public Map<String, Object> getAnalytics() {
        return analyticsService.getAnalytics();
    }

    @GetMapping("/client-risk/{clientId}")
    public boolean checkClientRisk(@PathVariable Long clientId) {
        return analyticsService.isClientRisky(clientId);
    }
}