package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.*;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.RecurringInvoiceRepository;
import com.freelancer.freelanceros.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringInvoiceService {

    private final RecurringInvoiceRepository recurringRepo;
    private final ClientRepository clientRepository;
    private final WorkspaceRepository workspaceRepository;

    public RecurringInvoiceService(RecurringInvoiceRepository recurringRepo,
                                   ClientRepository clientRepository,
                                   WorkspaceRepository workspaceRepository) {
        this.recurringRepo = recurringRepo;
        this.clientRepository = clientRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public RecurringInvoice create(Long workspaceId, RecurringInvoiceRequest req) {
        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        RecurringInvoice ri = new RecurringInvoice();
        ri.setClient(client);
        ri.setWorkspace(workspace);
        ri.setDescription(req.getDescription());
        ri.setAmount(req.getAmount());
        ri.setFrequency(req.getFrequency());
        ri.setNextRunDate(req.getStartDate());
        ri.setActive(true);

        return recurringRepo.save(ri);
    }

    public List<RecurringInvoice> getAll(Long workspaceId) {
        return recurringRepo.findByWorkspaceId(workspaceId);
    }

    public void deactivate(Long id) {
        RecurringInvoice ri = recurringRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("RecurringInvoice not found"));
        ri.setActive(false);
        recurringRepo.save(ri);
    }

    public void delete(Long id) {
        recurringRepo.deleteById(id);
    }

    public LocalDate nextDate(LocalDate current, Frequency frequency) {
        return switch (frequency) {
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}