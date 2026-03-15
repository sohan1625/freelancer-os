package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;

    public AnalyticsService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Map<String, Object> getAnalytics() {

        List<Invoice> invoices = invoiceRepository.findAll();

        int total = invoices.size();
        int paid = 0;
        int pending = 0;
        int overdue = 0;

        // FIXED: was double — now BigDecimal to match Invoice.amount
        BigDecimal revenue = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {

            switch (invoice.getStatus()) {

                case "PAID":
                    paid++;
                    // FIXED: BigDecimal uses .add() not +
                    revenue = revenue.add(invoice.getAmount());
                    break;

                case "PENDING":
                    pending++;
                    break;

                case "OVERDUE":
                    overdue++;
                    break;
            }
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalInvoices", total);
        analytics.put("paid", paid);
        analytics.put("pending", pending);
        analytics.put("overdue", overdue);
        analytics.put("revenueCollected", revenue);

        return analytics;
    }

    // Checks if client has 2 or more overdue invoices
    public boolean isClientRisky(Long clientId) {

        List<Invoice> invoices = invoiceRepository.findAll();
        int overdueCount = 0;

        for (Invoice invoice : invoices) {
            if (invoice.getClient() != null &&
                    invoice.getClient().getId().equals(clientId) &&
                    "OVERDUE".equals(invoice.getStatus())) {
                overdueCount++;
            }
        }

        return overdueCount >= 2;
    }
}