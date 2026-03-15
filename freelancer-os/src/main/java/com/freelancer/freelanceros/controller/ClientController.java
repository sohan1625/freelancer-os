package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Create a new client
    @PostMapping
    public Client addClient(@RequestBody Client client) {
        return clientService.saveClient(client);
    }

    // Get all clients
    @GetMapping
    public List<Client> getClients() {
        return clientService.getAllClients();
    }

    // Delete a client by ID
    // Also deletes all their invoices automatically
    @DeleteMapping("/{clientId}")
    public void deleteClient(@PathVariable Long clientId) {
        clientService.deleteClient(clientId);
    }
}