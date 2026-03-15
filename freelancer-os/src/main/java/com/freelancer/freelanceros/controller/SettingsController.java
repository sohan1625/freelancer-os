package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Settings;
import com.freelancer.freelanceros.service.SettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    // Get settings
    @GetMapping
    public Settings getSettings() {
        return settingsService.getSettings();
    }

    // Save settings
    @PostMapping
    public Settings saveSettings(@RequestBody Settings settings) {
        return settingsService.saveSettings(settings);
    }
}