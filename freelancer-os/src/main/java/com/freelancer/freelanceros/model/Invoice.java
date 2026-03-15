package com.freelancer.freelanceros.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIXED: was double — causes floating point precision loss
    // BigDecimal is the correct type for all financial/currency data
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private String status;

    // FIXED: was String (VARCHAR) — causes sorting and filtering bugs
    // LocalDate maps to DATE in PostgreSQL — proper date type
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties("invoices")
    private Client client;

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}