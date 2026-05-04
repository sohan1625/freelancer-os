package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Project;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.ExpenseRepository;
import com.freelancer.freelanceros.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;
    private final ExpenseRepository expenseRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ClientRepository clientRepository,
                          CurrentUserService currentUserService,
                          ExpenseRepository expenseRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
        this.expenseRepository = expenseRepository;
    }

    public Project createProject(Long clientId, Project project) {
        User user = currentUserService.getCurrentUser();

        Client client = clientRepository.findByIdWithWorkspace(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (!client.getWorkspace().getId().equals(user.getWorkspace().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        project.setClient(client);
        project.setWorkspace(user.getWorkspace());

        if (project.getStatus() == null) {
            project.setStatus("ACTIVE");
        }

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        User user = currentUserService.getCurrentUser();
        return projectRepository.findByWorkspaceId(user.getWorkspace().getId());
    }

    public Project updateStatus(Long projectId, String status) {
        User user = currentUserService.getCurrentUser();

        Project project = projectRepository.findByIdWithWorkspace(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getWorkspace().getId().equals(user.getWorkspace().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        project.setStatus(status.toUpperCase());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User user = currentUserService.getCurrentUser();

        Project project = projectRepository.findByIdWithWorkspace(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getWorkspace().getId().equals(user.getWorkspace().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        expenseRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);
    }
}