package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceId(Long workspaceId);

    List<Expense> findByWorkspaceIdAndCategory(Long workspaceId, String category);

    List<Expense> findByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}