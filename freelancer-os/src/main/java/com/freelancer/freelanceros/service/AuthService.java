package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register new user
    public User register(String name, String email, String password) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        // encrypt password
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    // Login user
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}