package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.*;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import com.freelancer.freelanceros.repository.RecurringInvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InvoiceScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceScheduler.class);
    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Asia/Kolkata");

    private final InvoiceRepository invoiceRepository;
    private final ReminderService reminderService;
    private final RecurringInvoiceRepository recurringInvoiceRepository;
    private final RecurringInvoiceService recurringInvoiceService;
    private final InvoiceService invoiceService;
    private final TransactionTemplate transactionTemplate;

    public InvoiceScheduler(InvoiceRepository invoiceRepository,
                            ReminderService reminderService,
                            RecurringInvoiceRepository recurringInvoiceRepository,
                            RecurringInvoiceService recurringInvoiceService,
                            InvoiceService invoiceService,
                            PlatformTransactionManager transactionManager) {
        this.invoiceRepository = invoiceRepository;
        this.reminderService = reminderService;
        this.recurringInvoiceRepository = recurringInvoiceRepository;
        this.recurringInvoiceService = recurringInvoiceService;
        this.invoiceService = invoiceService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")
    public void processRecurringInvoices() {
        processRecurringInvoicesAt(LocalDateTime.now(SCHEDULE_ZONE));
    }

    /**
     * Generates invoices for every due recurring schedule. Each schedule is processed in its own
     * transaction so one bad record cannot roll back (and endlessly retry) the others, and the
     * invoice email + PDF is only sent once that transaction has committed.
     */
    public void processRecurringInvoicesAt(LocalDateTime now) {
        List<Long> dueIds = recurringInvoiceRepository.findDueIds(now.toLocalDate());

        for (Long id : dueIds) {
            try {
                transactionTemplate.executeWithoutResult(status -> processRecurringInvoice(id, now));
            } catch (RuntimeException e) {
                log.error("Failed to generate recurring invoice {}: {}", id, e.getMessage(), e);
            }
        }
    }

    private void processRecurringInvoice(Long id, LocalDateTime now) {
        // Re-read under a row lock so overlapping runs cannot generate the same invoice twice
        RecurringInvoice ri = recurringInvoiceRepository.findByIdForUpdate(id).orElse(null);
        if (ri == null || !ri.isActive() || ri.getNextRunDate() == null) {
            return;
        }

        if (ri.getClient() == null || ri.getWorkspace() == null
                || ri.getClient().getWorkspace() == null
                || ri.getWorkspace().getId() == null
                || ri.getClient().getWorkspace().getId() == null
                || !ri.getWorkspace().getId().equals(ri.getClient().getWorkspace().getId())) {
            log.warn("Skipping recurring invoice {} because its tenant associations are inconsistent",
                    ri.getId());
            return;
        }

        LocalDate today = now.toLocalDate();
        LocalTime scheduledTime = ri.getScheduledTime() == null
                ? RecurringInvoiceService.DEFAULT_SCHEDULED_TIME
                : ri.getScheduledTime();
        boolean isDue = ri.getNextRunDate().isBefore(today)
                || ri.getNextRunDate().isEqual(today) && !scheduledTime.isAfter(now.toLocalTime());
        if (!isDue) {
            return;
        }

        Invoice invoice = new Invoice();
        invoice.setClient(ri.getClient());
        invoice.setDescription(ri.getDescription());
        invoice.setAmount(ri.getAmount());
        invoice.setStatus("PENDING");
        invoice.setIssueDate(today);
        invoice.setDueDate(today.plusDays(14));

        Invoice created = invoiceService.createInvoiceForWorkspace(
                ri.getClient().getId(), ri.getWorkspace().getId(), invoice);

        // Advance past today: a schedule that fell behind (server down, old start date) sends one
        // invoice now instead of one back-dated invoice per minute until it catches up.
        LocalDate next = ri.getNextRunDate();
        do {
            next = recurringInvoiceService.nextDate(next, ri.getFrequency());
        } while (!next.isAfter(today));
        ri.setNextRunDate(next);
        recurringInvoiceRepository.save(ri);

        log.info("Recurring invoice {} generated invoice #{}; next run {}", ri.getId(), created.getId(), next);
    }
}
