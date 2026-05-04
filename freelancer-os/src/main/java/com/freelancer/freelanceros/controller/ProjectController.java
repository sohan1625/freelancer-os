package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Project;
import com.freelancer.freelanceros.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ── Get all projects ──────────────────────────────────────────────────────
    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    // ── Create project for a client ───────────────────────────────────────────
    @PostMapping("/client/{clientId}")
    public Project createProject(@PathVariable Long clientId,
                                 @RequestBody Project project) {
        return projectService.createProject(clientId, project);
    }

    // ── Update project status — status passed as query param to avoid body issues
    @PatchMapping("/{projectId}/status")
    public Project updateStatus(@PathVariable Long projectId,
                                @RequestParam String status) {
        return projectService.updateStatus(projectId, status);
    }

    // ── Delete project ────────────────────────────────────────────────────────
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}