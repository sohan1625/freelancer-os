package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        return createInvoiceForWorkspace(clientId, getCurrentWorkspaceId(), invoice);
    }

    Invoice createInvoiceForWorkspace(Long clientId, Long workspaceId, Invoice invoice) {
        Client client = clientRepository.findByIdWithWorkspace(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (workspaceId == null || client.getWorkspace() == null
                || client.getWorkspace().getId() == null
                || !client.getWorkspace().getId().equals(workspaceId)) {
            throw new RuntimeException("Client not found");
        }

        invoice.setClient(client);

        if (invoice.getStatus() == null) {
            invoice.setStatus("PENDING");
        }

        Invoice saved = invoiceRepository.save(invoice);

        sendInvoiceEmailAfterCommit(saved);

        return saved;
    }

    /**
     * The invoice email runs on an async thread that re-reads the invoice to build the PDF.
     * If we are inside a transaction (e.g. the recurring-invoice scheduler), that thread would
     * not see the uncommitted row yet and the PDF lookup fails, so defer the send until commit.
     */
    private void sendInvoiceEmailAfterCommit(Invoice saved) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendInvoiceCreatedEmail(saved);
                }
            });
        } else {
            emailService.sendInvoiceCreatedEmail(saved);
        }
    }

    public List<Invoice> getInvoicesByClient(Long clientId) {
        Long workspaceId = getCurrentWorkspaceId();
        Client client = clientRepository.findByIdWithWorkspace(clientId)
                .filter(c -> c.getWorkspace() != null
                        && workspaceId.equals(c.getWorkspace().getId()))
                .orElseThrow(() -> new RuntimeException("Client not found"));

        return invoiceRepository.findByClientIdAndClientWorkspaceId(client.getId(), workspaceId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByWorkspaceIdWithClient(getCurrentWorkspaceId());
    }

    public Invoice markInvoicePaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndClientWorkspaceIdWithClient(
                        invoiceId, getCurrentWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setStatus("PAID");

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatusAndClientWorkspaceId(status, getCurrentWorkspaceId());
    }

    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndClientWorkspaceIdWithClient(
                        invoiceId, getCurrentWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoiceRepository.delete(invoice);
    }

    private Long getCurrentWorkspaceId() {
        User user = currentUserService.getCurrentUser();
        if (user == null || user.getWorkspace() == null || user.getWorkspace().getId() == null) {
            throw new RuntimeException("Workspace not found");
        }
        return user.getWorkspace().getId();
    }
}