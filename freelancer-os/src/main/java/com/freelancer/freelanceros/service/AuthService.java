package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.model.Workspace;
import com.freelancer.freelanceros.repository.UserRepository;
import com.freelancer.freelanceros.repository.WorkspaceRepository;
import com.freelancer.freelanceros.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       WorkspaceRepository workspaceRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public User register(String name, String email, String password) {
        name = name == null ? "" : name.trim();
        email = email.toLowerCase();

        if (name.isBlank()) {
            throw new RuntimeException("Username is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Username already taken");
        }

        Workspace workspace = new Workspace();
        workspace.setName(name + "'s Workspace");
        workspaceRepository.save(workspace);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setWorkspace(workspace);

        return userRepository.save(user);
    }

    // 🔥 UPDATED LOGIN (ACCESS + REFRESH TOKENS)
    public Map<String, String> login(String identifier, String password) {
        String normalized = identifier == null ? "" : identifier.trim();
        if (normalized.isBlank()) {
            throw new RuntimeException("Username or email is required");
        }

        User user = normalized.contains("@")
                ? userRepository.findByEmail(normalized.toLowerCase()).orElse(null)
                : userRepository.findByNameIgnoreCase(normalized).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    // 🔥 REFRESH TOKEN LOGIC
    public String refreshAccessToken(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        return jwtService.generateAccessToken(user);
    }

    public String extractEmailFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    public void forgotPassword(String email) {
        email = email.toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    // ── Reset password ────────────────────────────────────────────────────────

    public void resetPassword(String token, String email, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required for password reset.");
        }

        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByResetToken(token)
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(normalizedEmail))
                .orElseThrow(() -> new RuntimeException("Invalid reset link or email."));

        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset link has expired.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}