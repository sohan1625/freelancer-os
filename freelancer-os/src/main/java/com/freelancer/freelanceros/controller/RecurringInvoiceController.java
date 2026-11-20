package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.RecurringInvoice;
import com.freelancer.freelanceros.model.RecurringInvoiceRequest;
import com.freelancer.freelanceros.service.RecurringInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-invoices")
public class RecurringInvoiceController {

    private final RecurringInvoiceService service;

    public RecurringInvoiceController(RecurringInvoiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecurringInvoice> getAll() {
        return service.getAll();
    }

    @PostMapping
    public RecurringInvoice create(@RequestBody RecurringInvoiceRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/time")
    public RecurringInvoice updateScheduledTime(@PathVariable Long id,
                                                @RequestBody ScheduleTimeRequest request) {
        if (request == null || request.scheduledTime() == null || request.scheduledTime().isBlank()) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        return service.updateScheduledTime(id, request.scheduledTime());
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

    public record ScheduleTimeRequest(String scheduledTime) {}
}