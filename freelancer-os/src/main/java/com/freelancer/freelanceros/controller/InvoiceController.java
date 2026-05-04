package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.service.InvoicePdfService;
import com.freelancer.freelanceros.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(InvoiceService invoiceService,
                             InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    // ── Create invoice for a client ───────────────────────────────────────────
    @PostMapping("/{clientId}/invoices")
    public Invoice createInvoice(@PathVariable Long clientId,
                                 @RequestBody Invoice invoice) {
        return invoiceService.createInvoiceForClient(clientId, invoice);
    }

    // ── Get invoices for a specific client ────────────────────────────────────
    @GetMapping("/{clientId}/invoices")
    public List<Invoice> getInvoicesForClient(@PathVariable Long clientId) {
        return invoiceService.getInvoicesByClient(clientId);
    }

    // ── Get all invoices ──────────────────────────────────────────────────────
        @GetMapping("/invoices")
        public List<InvoiceListItem> getInvoices() {
        return invoiceService.getAllInvoices().stream()
            .map(i -> new InvoiceListItem(
                i.getId(),
                i.getAmount(),
                i.getStatus(),
                i.getIssueDate(),
                i.getDueDate(),
                i.getClient() != null ? i.getClient().getId() : null,
                i.getClient() != null ? i.getClient().getName() : "Client"
            ))
            .toList();
    }

        public record InvoiceListItem(
            Long id,
            BigDecimal amount,
            String status,
            LocalDate issueDate,
            LocalDate dueDate,
            Long clientId,
            String clientName
        ) {}

    // ── Mark invoice as paid ──────────────────────────────────────────────────
    @PatchMapping("/invoices/{invoiceId}/paid")
    public Invoice markInvoicePaid(@PathVariable Long invoiceId) {
        return invoiceService.markInvoicePaid(invoiceId);
    }

    // ── Delete invoice ────────────────────────────────────────────────────────
    @DeleteMapping("/invoices/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }

    // ── Download invoice as PDF ───────────────────────────────────────────────
    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long invoiceId) {
        try {
            byte[] pdf = invoicePdfService.generateInvoicePdf(invoiceId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"invoice-" + invoiceId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}