package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.RecurringInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringInvoiceRepository extends JpaRepository<RecurringInvoice, Long> {

    List<RecurringInvoice> findByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceId(Long workspaceId);

    List<RecurringInvoice> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}