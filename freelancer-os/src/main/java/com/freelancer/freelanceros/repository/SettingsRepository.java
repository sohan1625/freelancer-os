package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
}