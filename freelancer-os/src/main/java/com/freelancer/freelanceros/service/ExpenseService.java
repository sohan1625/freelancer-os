package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Expense;
import com.freelancer.freelanceros.model.Project;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.repository.ExpenseRepository;
import com.freelancer.freelanceros.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ProjectRepository projectRepository,
                          CurrentUserService currentUserService) {
        this.expenseRepository = expenseRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    // ── Get all expenses for workspace ────────────────────────────────────────

    public List<Expense> getAllExpenses() {
        User user = currentUserService.getCurrentUser();
        return expenseRepository.findByWorkspaceId(user.getWorkspace().getId());
    }

    // ── Create expense ────────────────────────────────────────────────────────

    public Expense createExpense(Long projectId, Expense expense) {
        User user = currentUserService.getCurrentUser();

        expense.setWorkspace(user.getWorkspace());

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Multi-tenant check
            if (!project.getWorkspace().getId().equals(user.getWorkspace().getId())) {
                throw new RuntimeException("Unauthorized");
            }

            expense.setProject(project);
        }

        return expenseRepository.save(expense);
    }

    // ── Delete expense ────────────────────────────────────────────────────────

    public void deleteExpense(Long expenseId) {
        User user = currentUserService.getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getWorkspace().getId().equals(user.getWorkspace().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        expenseRepository.deleteById(expenseId);
    }
}