package com.freelancer.freelanceros.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringInvoiceRequest {

    private Long clientId;
    private String description;
    private BigDecimal amount;
    private Frequency frequency;
    private LocalDate startDate;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}