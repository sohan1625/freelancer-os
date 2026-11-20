package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByClientId(Long clientId);

    List<Invoice> findByClientIdAndClientWorkspaceId(Long clientId, Long workspaceId);

    void deleteByClientId(Long clientId);

    void deleteByClientWorkspaceId(Long workspaceId);

    List<Invoice> findByStatusAndClientWorkspaceId(String status, Long workspaceId);

    @Query("""
            SELECT i FROM Invoice i
            JOIN FETCH i.client c
            JOIN c.workspace w
            WHERE i.id = :invoiceId AND w.id = :workspaceId
            """)
    Optional<Invoice> findByIdAndClientWorkspaceIdWithClient(
            @Param("invoiceId") Long invoiceId,
            @Param("workspaceId") Long workspaceId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.client c WHERE c.workspace.id = :workspaceId")
    List<Invoice> findAllByWorkspaceIdWithClient(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT MONTH(i.dueDate), YEAR(i.dueDate), SUM(i.amount)
            FROM Invoice i
            WHERE i.status = 'PAID' AND i.client.workspace.id = :workspaceId
            GROUP BY YEAR(i.dueDate), MONTH(i.dueDate)
            ORDER BY YEAR(i.dueDate), MONTH(i.dueDate)
            """)
    List<Object[]> findMonthlyRevenueByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT i.client.name, SUM(i.amount)
            FROM Invoice i
            WHERE i.status = 'PAID' AND i.client.workspace.id = :workspaceId
            GROUP BY i.client.id, i.client.name
            ORDER BY SUM(i.amount) DESC
            """)
    List<Object[]> findTopClientsByRevenueAndWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT COUNT(i) FROM Invoice i
            WHERE i.client.id = :clientId
              AND i.client.workspace.id = :workspaceId
              AND i.status = :status
            """)
    long countByClientIdAndClientWorkspaceIdAndStatus(
            @Param("clientId") Long clientId,
            @Param("workspaceId") Long workspaceId,
            @Param("status") String status);
}