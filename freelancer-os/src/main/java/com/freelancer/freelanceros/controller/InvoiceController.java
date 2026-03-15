package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Create invoice for a client
    @PostMapping("/{clientId}/invoices")
    public Invoice createInvoice(@PathVariable Long clientId,
                                 @RequestBody Invoice invoice) {
        return invoiceService.createInvoiceForClient(clientId, invoice);
    }

    // Get invoices for a specific client
    @GetMapping("/{clientId}/invoices")
    public List<Invoice> getInvoicesForClient(@PathVariable Long clientId) {
        return invoiceService.getInvoicesByClient(clientId);
    }

    // Get all invoices
    @GetMapping("/invoices")
    public List<Invoice> getInvoices() {
        return invoiceService.getAllInvoices();
    }

    // Mark invoice as paid
    @PatchMapping("/invoices/{invoiceId}/paid")
    public Invoice markInvoicePaid(@PathVariable Long invoiceId) {
        return invoiceService.markInvoicePaid(invoiceId);
    }

    // Filter invoices by status
    @GetMapping("/invoices/filter")
    public List<Invoice> filterInvoices(@RequestParam String status) {
        return invoiceService.getInvoicesByStatus(status);
    }

    // Delete invoice by ID
    @DeleteMapping("/invoices/{invoiceId}")
    public void deleteInvoice(@PathVariable Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
    }
}