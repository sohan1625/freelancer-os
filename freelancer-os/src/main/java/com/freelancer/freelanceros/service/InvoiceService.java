package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ReminderService reminderService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          ReminderService reminderService) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.reminderService = reminderService;
    }

    // Creates invoice and immediately sends email to client
    public Invoice createInvoiceForClient(Long clientId, Invoice invoice) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        invoice.setClient(client);
        Invoice saved = invoiceRepository.save(invoice);
        reminderService.sendInvoiceCreatedNotification(saved);
        return saved;
    }

    // Returns all invoices
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // Returns invoices for a specific client
    public List<Invoice> getInvoicesByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return invoiceRepository.findByClient(client);
    }

    // Marks invoice as PAID
    public Invoice markInvoicePaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus("PAID");
        return invoiceRepository.save(invoice);
    }

    // Filter by status
    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    // Delete invoice by ID
    public void deleteInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new RuntimeException("Invoice not found");
        }
        invoiceRepository.deleteById(invoiceId);
    }
}