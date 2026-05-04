package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new RuntimeException("User not authenticated properly");
    }
}