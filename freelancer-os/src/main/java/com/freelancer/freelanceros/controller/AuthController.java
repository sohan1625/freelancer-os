package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register API
    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> body) {

        return authService.register(
                body.get("name"),
                body.get("email"),
                body.get("password")
        );
    }

    // Login API
    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {

        return authService.login(
                body.get("email"),
                body.get("password")
        );
    }
}