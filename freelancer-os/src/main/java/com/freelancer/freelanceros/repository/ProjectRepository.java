package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceId(Long workspaceId);

    List<Project> findByWorkspaceIdAndStatus(Long workspaceId, String status);

    List<Project> findByClientId(Long clientId);

    @Query("SELECT p FROM Project p JOIN FETCH p.workspace WHERE p.id = :id")
    Optional<Project> findByIdWithWorkspace(@Param("id") Long id);
}