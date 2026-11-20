package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.*;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.RecurringInvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class RecurringInvoiceService {

    static final LocalTime DEFAULT_SCHEDULED_TIME = LocalTime.of(9, 0);

    private final RecurringInvoiceRepository recurringRepo;
    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;

    public RecurringInvoiceService(RecurringInvoiceRepository recurringRepo,
                                   ClientRepository clientRepository,
                                   CurrentUserService currentUserService) {
        this.recurringRepo = recurringRepo;
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
    }

    public RecurringInvoice create(RecurringInvoiceRequest req) {
        Long workspaceId = getCurrentWorkspaceId();
        Client client = clientRepository.findByIdWithWorkspace(req.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getWorkspace() == null || client.getWorkspace().getId() == null
                || !workspaceId.equals(client.getWorkspace().getId())) {
            throw new RuntimeException("Client not found");
        }

        RecurringInvoice ri = new RecurringInvoice();
        ri.setClient(client);
        ri.setWorkspace(client.getWorkspace());
        ri.setDescription(req.getDescription());
        ri.setAmount(req.getAmount());
        ri.setFrequency(req.getFrequency());
        ri.setNextRunDate(req.getStartDate());
        ri.setScheduledTime(toLocalTime(req.getScheduledTime(), DEFAULT_SCHEDULED_TIME));
        ri.setActive(true);

        return recurringRepo.save(ri);
    }

    public List<RecurringInvoice> getAll() {
        return recurringRepo.findValidByWorkspaceId(getCurrentWorkspaceId());
    }

    public void deactivate(Long id) {
        Long workspaceId = getCurrentWorkspaceId();
        RecurringInvoice ri = recurringRepo.findByIdAndWorkspaceIdWithAssociations(id, workspaceId)
                .orElseThrow(() -> new RuntimeException("RecurringInvoice not found"));
        validateTenantAssociations(ri, workspaceId);
        ri.setActive(false);
        recurringRepo.save(ri);
    }

    public void delete(Long id) {
        Long workspaceId = getCurrentWorkspaceId();
        RecurringInvoice ri = recurringRepo.findByIdAndWorkspaceIdWithAssociations(id, workspaceId)
                .orElseThrow(() -> new RuntimeException("RecurringInvoice not found"));
        validateTenantAssociations(ri, workspaceId);
        recurringRepo.delete(ri);
    }

    public RecurringInvoice updateScheduledTime(Long id, LocalTime scheduledTime) {
        if (scheduledTime == null) {
            throw new IllegalArgumentException("Scheduled time is required");
        }

        Long workspaceId = getCurrentWorkspaceId();
        RecurringInvoice ri = recurringRepo.findByIdAndWorkspaceIdWithAssociations(id, workspaceId)
                .orElseThrow(() -> new RuntimeException("RecurringInvoice not found"));
        validateTenantAssociations(ri, workspaceId);
        ri.setScheduledTime(scheduledTime);
        return recurringRepo.save(ri);
    }

    public RecurringInvoice updateScheduledTime(Long id, String scheduledTimeText) {
        if (scheduledTimeText == null || scheduledTimeText.isBlank()) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        return updateScheduledTime(id, parseScheduledTime(scheduledTimeText, null));
    }

    private LocalTime parseScheduledTime(String scheduledTime, LocalTime fallback) {
        if (scheduledTime == null || scheduledTime.isBlank()) {
            return fallback;
        }

        String value = scheduledTime.trim();

        if (value.contains("T")) {
            value = value.substring(value.indexOf('T') + 1);
        }
        if (value.endsWith("Z")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.contains(".")) {
            value = value.substring(0, value.indexOf('.'));
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // Handle the UI's HH:mm input used for recurring invoice creation and updates.
        }

        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Scheduled time must be in HH:mm or HH:mm:ss format", e);
        }
    }

    private LocalTime toLocalTime(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return parseScheduledTime(value, fallback);
    }

    public LocalDate nextDate(LocalDate current, Frequency frequency) {
        return switch (frequency) {
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private void validateTenantAssociations(RecurringInvoice recurringInvoice, Long workspaceId) {
        if (recurringInvoice.getClient() == null
                || recurringInvoice.getClient().getWorkspace() == null
                || recurringInvoice.getClient().getWorkspace().getId() == null
                || recurringInvoice.getWorkspace() == null
                || recurringInvoice.getWorkspace().getId() == null
                || !workspaceId.equals(recurringInvoice.getWorkspace().getId())
                || !workspaceId.equals(recurringInvoice.getClient().getWorkspace().getId())) {
            throw new RuntimeException("RecurringInvoice not found");
        }
    }

    private Long getCurrentWorkspaceId() {
        User user = currentUserService.getCurrentUser();
        if (user == null || user.getWorkspace() == null || user.getWorkspace().getId() == null) {
            throw new RuntimeException("Workspace not found");
        }
        return user.getWorkspace().getId();
    }
}