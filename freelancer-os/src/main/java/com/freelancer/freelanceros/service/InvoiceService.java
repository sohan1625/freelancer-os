package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;
    private final EmailService emailService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          CurrentUserService currentUserService,
                          EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
        this.emailService = emailService;
    }

    public Invoice createInvoiceForClient(Long clientId, Invoice invoice) {

        User user = currentUserService.getCurrentUser();

        Client client = clientRepository.findByIdWithWorkspace(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.getWorkspace().getId();

        // 🔥 MULTI-TENANT CHECK
        if (!client.getWorkspace().getId().equals(user.getWorkspace().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        invoice.setClient(client);

        if (invoice.getStatus() == null) {
            invoice.setStatus("PENDING");
        }

        Invoice saved = invoiceRepository.save(invoice);

        // Send email async — does not block the response, does not fail the request
        emailService.sendInvoiceCreatedEmail(saved);

        return saved;
    }

    public List<Invoice> getInvoicesByClient(Long clientId) {
        return invoiceRepository.findByClientId(clientId);
    }

    public List<Invoice> getAllInvoices() {
        User user = currentUserService.getCurrentUser();
        return invoiceRepository.findAllByWorkspaceIdWithClient(user.getWorkspace().getId());
    }

    public Invoice markInvoicePaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setStatus("PAID");

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    public void deleteInvoice(Long invoiceId) {
        invoiceRepository.deleteById(invoiceId);
    }
}