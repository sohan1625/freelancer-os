package com.freelancer.freelanceros.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_invoices")
public class RecurringInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    private String description;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private LocalDate nextRunDate;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}