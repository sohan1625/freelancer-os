package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Settings;
import com.freelancer.freelanceros.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    // Get settings — always returns the first row
    // Creates default settings if none exist
    public Settings getSettings() {
        List<Settings> all = settingsRepository.findAll();
        if (all.isEmpty()) {
            Settings defaults = new Settings();
            defaults.setName("Your Name");
            defaults.setEmail("you@example.com");
            defaults.setCompany("Your Company");
            defaults.setPhone("");
            return settingsRepository.save(defaults);
        }
        return all.get(0);
    }

    // Save/update settings
    public Settings saveSettings(Settings settings) {
        List<Settings> all = settingsRepository.findAll();
        if (!all.isEmpty()) {
            settings.setId(all.get(0).getId());
        }
        return settingsRepository.save(settings);
    }
}