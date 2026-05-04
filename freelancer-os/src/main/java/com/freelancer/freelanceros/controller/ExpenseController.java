package com.freelancer.freelanceros.controller;

import com.freelancer.freelanceros.model.Expense;
import com.freelancer.freelanceros.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // ── Get all expenses ──────────────────────────────────────────────────────
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    // ── Create expense (optionally linked to a project) ───────────────────────
    @PostMapping
    public Expense createExpense(
            @RequestBody Expense expense,
            @RequestParam(required = false) Long projectId) {
        return expenseService.createExpense(projectId, expense);
    }

    // ── Delete expense ────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}