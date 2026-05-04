package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.RecurringInvoice;
import com.freelancer.freelanceros.model.RecurringInvoiceRequest;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.service.CurrentUserService;
import com.freelancer.freelanceros.service.RecurringInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-invoices")
public class RecurringInvoiceController {

    private final RecurringInvoiceService service;
    private final CurrentUserService currentUserService;

    public RecurringInvoiceController(RecurringInvoiceService service,
                                      CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<RecurringInvoice> getAll() {
        User user = currentUserService.getCurrentUser();
        return service.getAll(user.getWorkspace().getId());
    }

    @PostMapping
    public RecurringInvoice create(@RequestBody RecurringInvoiceRequest req) {
        User user = currentUserService.getCurrentUser();
        return service.create(user.getWorkspace().getId(), req);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}