package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> body) {
        return authService.register(
                body.get("name"),
                body.get("email"),
                body.get("password")
        );
    }

    // 🔥 UPDATED LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        try {
            String identifier = body.get("identifier") != null ? body.get("identifier") : body.get("email");
            Map<String, String> tokens = authService.login(
                identifier,
                    body.get("password")
            );
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", e.getMessage() != null ? e.getMessage() : "Invalid credentials"
            ));
        }
    }

    // 🔥 REFRESH ENDPOINT
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {

        authService.forgotPassword(body.get("email"));

        return ResponseEntity.ok(Map.of(
                "message", "If an account exists, a reset link has been sent."
        ));
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> body) {

        authService.resetPassword(body.get("token"), body.get("email"), body.get("password"));

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}
