package com.freelancer.freelanceros.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String company;
    private String phone;

    private boolean invoicePaid = true;
    private boolean invoiceOverdue = true;
    private boolean newClient = false;
    private boolean weeklyReport = true;
    private boolean paymentReminder = true;

    private int beforeDue = 3;
    private int afterOverdue = 1;
    private int maxReminders = 3;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isInvoicePaid() { return invoicePaid; }
    public void setInvoicePaid(boolean invoicePaid) { this.invoicePaid = invoicePaid; }

    public boolean isInvoiceOverdue() { return invoiceOverdue; }
    public void setInvoiceOverdue(boolean invoiceOverdue) { this.invoiceOverdue = invoiceOverdue; }

    public boolean isNewClient() { return newClient; }
    public void setNewClient(boolean newClient) { this.newClient = newClient; }

    public boolean isWeeklyReport() { return weeklyReport; }
    public void setWeeklyReport(boolean weeklyReport) { this.weeklyReport = weeklyReport; }

    public boolean isPaymentReminder() { return paymentReminder; }
    public void setPaymentReminder(boolean paymentReminder) { this.paymentReminder = paymentReminder; }

    public int getBeforeDue() { return beforeDue; }
    public void setBeforeDue(int beforeDue) { this.beforeDue = beforeDue; }

    public int getAfterOverdue() { return afterOverdue; }
    public void setAfterOverdue(int afterOverdue) { this.afterOverdue = afterOverdue; }

    public int getMaxReminders() { return maxReminders; }
    public void setMaxReminders(int maxReminders) { this.maxReminders = maxReminders; }
}