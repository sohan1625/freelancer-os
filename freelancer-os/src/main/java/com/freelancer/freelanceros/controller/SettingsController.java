package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.service.CurrentUserService;
import com.freelancer.freelanceros.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final CurrentUserService currentUserService;

    public SettingsController(SettingsService settingsService,
                              CurrentUserService currentUserService) {
        this.settingsService = settingsService;
        this.currentUserService = currentUserService;
    }

    // ── Get current user profile ──────────────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(Map.of(
                "name", user.getName() != null ? user.getName() : "",
                "email", user.getEmail()
        ));
    }

    // ── Update name ───────────────────────────────────────────────────────────
    @PatchMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        User updated = settingsService.updateName(name);
        return ResponseEntity.ok(Map.of(
                "name", updated.getName() != null ? updated.getName() : "",
                "email", updated.getEmail()
        ));
    }

    // ── Change password ───────────────────────────────────────────────────────
    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        settingsService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    // ── Delete account and workspace data ───────────────────────────────────
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        settingsService.deleteCurrentAccount();
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully."));
    }
}