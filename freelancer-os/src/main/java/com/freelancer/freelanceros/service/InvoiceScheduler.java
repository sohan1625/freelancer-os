package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.*;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import com.freelancer.freelanceros.repository.RecurringInvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceScheduler {

    private final InvoiceRepository invoiceRepository;
    private final ReminderService reminderService;
    private final RecurringInvoiceRepository recurringInvoiceRepository;
    private final RecurringInvoiceService recurringInvoiceService;
    private final InvoiceService invoiceService;

    public InvoiceScheduler(InvoiceRepository invoiceRepository,
                            ReminderService reminderService,
                            RecurringInvoiceRepository recurringInvoiceRepository,
                            RecurringInvoiceService recurringInvoiceService,
                            InvoiceService invoiceService) {
        this.invoiceRepository = invoiceRepository;
        this.reminderService = reminderService;
        this.recurringInvoiceRepository = recurringInvoiceRepository;
        this.recurringInvoiceService = recurringInvoiceService;
        this.invoiceService = invoiceService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void markOverdueAndSendReminders() {
        List<Invoice> invoices = invoiceRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Invoice invoice : invoices) {
            LocalDate due = invoice.getDueDate();
            if (due == null) continue;

            String status = invoice.getStatus();

            // Skip paid invoices entirely — no reminders needed
            if ("PAID".equals(status)) continue;

            // T-1 day reminder — day before due, only if still PENDING
            if (today.equals(due.minusDays(1)) && "PENDING".equals(status)) {
                reminderService.sendReminder(invoice, ReminderService.ReminderType.DAY_BEFORE);
                System.out.println("T-1 reminder sent for Invoice " + invoice.getId());
            }

            // T-3 day reminder — 3 days before due, only if still PENDING
            if (today.equals(due.minusDays(3)) && "PENDING".equals(status)) {
                reminderService.sendReminder(invoice, ReminderService.ReminderType.THREE_DAYS_BEFORE);
                System.out.println("3 day reminder sent for Invoice " + invoice.getId());
            }

            // Overdue — mark and send reminder only on the exact day it becomes overdue
            // today.isAfter(due) means it just became overdue — only fires once per invoice
            // because after marking OVERDUE it won't be PENDING anymore
            if (today.isAfter(due) && "PENDING".equals(status)) {
                invoice.setStatus("OVERDUE");
                invoiceRepository.save(invoice);
                reminderService.sendReminder(invoice, ReminderService.ReminderType.OVERDUE);
                System.out.println("Invoice " + invoice.getId() + " marked OVERDUE - reminder sent");
            }

            // Follow-up overdue reminder — 7 days after due date, if still OVERDUE
            if (today.equals(due.plusDays(7)) && "OVERDUE".equals(status)) {
                reminderService.sendReminder(invoice, ReminderService.ReminderType.OVERDUE_FOLLOWUP);
                System.out.println("7 day overdue follow-up sent for Invoice " + invoice.getId());
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void processRecurringInvoices() {
        List<RecurringInvoice> dueList = recurringInvoiceRepository
                .findByActiveTrueAndNextRunDateLessThanEqual(LocalDate.now());

        for (RecurringInvoice ri : dueList) {
            Invoice invoice = new Invoice();
            invoice.setClient(ri.getClient());
            invoice.setDescription(ri.getDescription());
            invoice.setAmount(ri.getAmount());
            invoice.setStatus("PENDING");
            invoice.setIssueDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(14));

            invoiceService.createInvoiceForClient(ri.getClient().getId(), invoice);

            ri.setNextRunDate(recurringInvoiceService.nextDate(ri.getNextRunDate(), ri.getFrequency()));
            recurringInvoiceRepository.save(ri);
        }
    }
}