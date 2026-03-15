package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceScheduler {

    private final InvoiceRepository invoiceRepository;
    private final ReminderService reminderService;

    public InvoiceScheduler(InvoiceRepository invoiceRepository,
                            ReminderService reminderService) {
        this.invoiceRepository = invoiceRepository;
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void markOverdueAndSendReminders() {

        List<Invoice> invoices = invoiceRepository.findAll();

        for (Invoice invoice : invoices) {

            // FIXED: dueDate is now LocalDate directly
            // No need to parse from String anymore
            LocalDate due = invoice.getDueDate();

            // Skip invoices with no due date
            if (due == null) continue;

            LocalDate today = LocalDate.now();

            // If due date passed and not paid → mark OVERDUE
            if (today.isAfter(due) && !"PAID".equals(invoice.getStatus())) {
                invoice.setStatus("OVERDUE");
                invoiceRepository.save(invoice);
                reminderService.sendReminder(invoice);
                System.out.println("Invoice " + invoice.getId() + " marked OVERDUE - reminder sent");
            }

            // If due in 3 days and still pending → send early reminder
            if (today.equals(due.minusDays(3)) && "PENDING".equals(invoice.getStatus())) {
                reminderService.sendReminder(invoice);
                System.out.println("3 day reminder sent for Invoice " + invoice.getId());
            }
        }
    }
}