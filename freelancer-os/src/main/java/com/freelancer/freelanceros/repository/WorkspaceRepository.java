package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}