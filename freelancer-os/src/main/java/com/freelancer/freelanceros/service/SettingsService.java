package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(UserRepository userRepository,
                           CurrentUserService currentUserService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Update display name ───────────────────────────────────────────────────

    public User updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Name cannot be empty.");
        }
        User user = currentUserService.getCurrentUser();
        user.setName(name.trim());
        return userRepository.save(user);
    }

    // ── Change password ───────────────────────────────────────────────────────

    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new RuntimeException("Current password is required.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters.");
        }

        User user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ── Delete current account and workspace data ───────────────────────────

    @Transactional
    public void deleteCurrentAccount() {
        User user = currentUserService.getCurrentUser();
        Long userId = user.getId();
        String email = user.getEmail();

        if (userId == null) {
            throw new RuntimeException("User account not found.");
        }

        userRepository.deleteById(userId);
        userRepository.flush();

        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Failed to delete account. Please try again.");
        }
    }
}