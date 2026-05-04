package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNameIgnoreCase(String name);

    boolean existsByEmail(String email);

    boolean existsByNameIgnoreCase(String name);

    Optional<User> findByResetToken(String resetToken);

    void deleteByWorkspaceId(Long workspaceId);
}