package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByClientId(Long clientId);

    void deleteByClientId(Long clientId);

    void deleteByClientWorkspaceId(Long workspaceId);

    List<Invoice> findByStatus(String status);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.client WHERE i.id = :id")
    Optional<Invoice> findByIdWithClient(@Param("id") Long id);

        @Query("SELECT i FROM Invoice i JOIN FETCH i.client c WHERE c.workspace.id = :workspaceId")
        List<Invoice> findAllByWorkspaceIdWithClient(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT MONTH(i.dueDate), YEAR(i.dueDate), SUM(i.amount)
            FROM Invoice i
            WHERE i.status = 'PAID'
            GROUP BY YEAR(i.dueDate), MONTH(i.dueDate)
            ORDER BY YEAR(i.dueDate), MONTH(i.dueDate)
            """)
    List<Object[]> findMonthlyRevenue();

    @Query("""
            SELECT i.client.name, SUM(i.amount)
            FROM Invoice i
            WHERE i.status = 'PAID'
            GROUP BY i.client.id, i.client.name
            ORDER BY SUM(i.amount) DESC
            """)
    List<Object[]> findTopClientsByRevenue();
}