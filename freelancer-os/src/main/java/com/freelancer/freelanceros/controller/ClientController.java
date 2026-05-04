package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Project;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.ExpenseRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import com.freelancer.freelanceros.repository.ProjectRepository;
import com.freelancer.freelanceros.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;

    public ClientController(ClientRepository clientRepository,
                            ProjectRepository projectRepository,
                            CurrentUserService currentUserService,
                            InvoiceRepository invoiceRepository,
                            ExpenseRepository expenseRepository) {
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
        this.invoiceRepository = invoiceRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public List<Client> getClients() {
        User user = currentUserService.getCurrentUser();
        return clientRepository.findByWorkspaceId(user.getWorkspace().getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return clientRepository.findByIdWithWorkspace(id)
                .filter(c -> c.getWorkspace().getId().equals(user.getWorkspace().getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/projects")
    public List<Project> getProjectsForClient(@PathVariable Long id) {
        return projectRepository.findByClientId(id);
    }

    @PostMapping
    public Client createClient(@RequestBody Client client) {
        User user = currentUserService.getCurrentUser();
        client.setWorkspace(user.getWorkspace());
        return clientRepository.save(client);
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean force) {
        User user = currentUserService.getCurrentUser();

        var clientOpt = clientRepository.findByIdWithWorkspace(id)
                .filter(c -> c.getWorkspace().getId().equals(user.getWorkspace().getId()));

        if (clientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var invoices = invoiceRepository.findByClientId(id);
        var projects = projectRepository.findByClientId(id);

        if ((!invoices.isEmpty() || !projects.isEmpty()) && !force) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Cannot delete client with linked invoices or projects."));
        }

        if (force) {
            for (Project project : projects) {
                expenseRepository.deleteByProjectId(project.getId());
            }
            projectRepository.deleteAll(projects);
            invoiceRepository.deleteByClientId(id);
        }

        clientRepository.delete(clientOpt.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/portal-token")
    public ResponseEntity<Map<String, String>> getPortalToken(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return clientRepository.findByIdWithWorkspace(id)
                .filter(c -> c.getWorkspace().getId().equals(user.getWorkspace().getId()))
                .map(c -> ResponseEntity.ok(Map.of("token", c.getPortalToken())))
                .orElse(ResponseEntity.notFound().build());
    }
}