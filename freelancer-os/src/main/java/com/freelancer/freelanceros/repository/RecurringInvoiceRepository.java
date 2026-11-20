package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.RecurringInvoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringInvoiceRepository extends JpaRepository<RecurringInvoice, Long> {

    @Query("""
            SELECT r FROM RecurringInvoice r
            JOIN FETCH r.client c
            JOIN FETCH r.workspace w
            WHERE w.id = :workspaceId AND c.workspace.id = :workspaceId
            """)
    List<RecurringInvoice> findValidByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT r FROM RecurringInvoice r
            JOIN FETCH r.client c
            JOIN FETCH r.workspace w
            WHERE r.id = :id AND w.id = :workspaceId
            """)
    Optional<RecurringInvoice> findByIdAndWorkspaceIdWithAssociations(
            @Param("id") Long id,
            @Param("workspaceId") Long workspaceId);

    void deleteByWorkspaceId(Long workspaceId);

    @Query("SELECT r.id FROM RecurringInvoice r WHERE r.active = true AND r.nextRunDate <= :date")
    List<Long> findDueIds(@Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecurringInvoice r WHERE r.id = :id")
    Optional<RecurringInvoice> findByIdForUpdate(@Param("id") Long id);
}