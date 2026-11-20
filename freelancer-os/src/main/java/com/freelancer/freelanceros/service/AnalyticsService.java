package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;

    public AnalyticsService(InvoiceRepository invoiceRepository,
                            ClientRepository clientRepository,
                            CurrentUserService currentUserService) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
    }

    public Map<String, Object> getAnalytics() {
        Long workspaceId = getCurrentWorkspaceId();

        List<Invoice> invoices = invoiceRepository.findAllByWorkspaceIdWithClient(workspaceId);

        int total = invoices.size();
        int paid = 0;
        int pending = 0;
        int overdue = 0;
        BigDecimal revenue = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            switch (invoice.getStatus()) {
                case "PAID" -> {
                    paid++;
                    revenue = revenue.add(invoice.getAmount());
                }
                case "PENDING" -> pending++;
                case "OVERDUE" -> overdue++;
            }
        }

        // ── Monthly revenue ───────────────────────────────────────────────────
        List<Object[]> monthlyRaw = invoiceRepository.findMonthlyRevenueByWorkspaceId(workspaceId);
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();

        for (Object[] row : monthlyRaw) {
            int month = ((Number) row[0]).intValue();
            int year  = ((Number) row[1]).intValue();
            BigDecimal amount = (BigDecimal) row[2];

            String label = Month.of(month)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", label);
            entry.put("revenue", amount);
            monthlyRevenue.add(entry);
        }

        // ── Top clients by revenue ────────────────────────────────────────────
        List<Object[]> topRaw = invoiceRepository.findTopClientsByRevenueAndWorkspaceId(workspaceId);
        List<Map<String, Object>> topClients = new ArrayList<>();

        for (Object[] row : topRaw) {
            String clientName   = (String) row[0];
            BigDecimal amount   = (BigDecimal) row[1];

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", clientName);
            entry.put("revenue", amount);
            topClients.add(entry);
        }

        // ── Assemble response ─────────────────────────────────────────────────
        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalInvoices", total);
        analytics.put("paid", paid);
        analytics.put("pending", pending);
        analytics.put("overdue", overdue);
        analytics.put("revenueCollected", revenue);
        analytics.put("monthlyRevenue", monthlyRevenue);
        analytics.put("topClients", topClients);

        return analytics;
    }

    public boolean isClientRisky(Long clientId) {
        Long workspaceId = getCurrentWorkspaceId();
        Client client = clientRepository.findByIdWithWorkspace(clientId)
                .filter(c -> c.getWorkspace() != null
                        && workspaceId.equals(c.getWorkspace().getId()))
                .orElseThrow(() -> new RuntimeException("Client not found"));

        return invoiceRepository.countByClientIdAndClientWorkspaceIdAndStatus(
                client.getId(), workspaceId, "OVERDUE") >= 2;
    }

    private Long getCurrentWorkspaceId() {
        User user = currentUserService.getCurrentUser();
        if (user == null || user.getWorkspace() == null || user.getWorkspace().getId() == null) {
            throw new RuntimeException("Workspace not found");
        }
        return user.getWorkspace().getId();
    }
}