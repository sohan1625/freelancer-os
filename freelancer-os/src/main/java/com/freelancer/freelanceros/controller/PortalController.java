package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public PortalController(ClientRepository clientRepository,
                            InvoiceRepository invoiceRepository) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPortal(@PathVariable String token) {
        Client client = clientRepository.findByPortalToken(token)
                .orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        List<Invoice> invoices = invoiceRepository.findByClientId(client.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clientName", client.getName());
        response.put("clientEmail", client.getEmail());
        response.put("invoices", invoices);

        return ResponseEntity.ok(response);
    }
}