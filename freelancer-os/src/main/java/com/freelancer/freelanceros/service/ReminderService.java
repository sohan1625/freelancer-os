package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    // JavaMailSender is provided by Spring Boot automatically
    // because we added the mail dependency in pom.xml
    private final JavaMailSender mailSender;

    public ReminderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Called when a new invoice is created
    // Sends an email to the client notifying them about the invoice
    public void sendInvoiceCreatedNotification(Invoice invoice) {

        Client client = invoice.getClient();

        if (client == null) return;

        sendEmail(
                client.getEmail(),
                "New Invoice from your Freelancer",
                "Hi " + client.getName() + ",\n\n" +
                        "You have a new invoice of Rs." + invoice.getAmount() + "\n" +
                        "Due Date: " + invoice.getDueDate() + "\n\n" +
                        "Please make payment on time.\n\n" +
                        "Thanks,\n" +
                        "FreelancerOS"
        );
    }

    // Called by InvoiceScheduler every day
    // Sends reminder email when invoice is overdue or due in 3 days
    public void sendReminder(Invoice invoice) {

        Client client = invoice.getClient();

        if (client == null) return;

        sendEmail(
                client.getEmail(),
                "Payment Reminder - Invoice #" + invoice.getId(),
                "Hi " + client.getName() + ",\n\n" +
                        "This is a reminder that your invoice of Rs." + invoice.getAmount() + "\n" +
                        "was due on " + invoice.getDueDate() + " and is still unpaid.\n\n" +
                        "Please make payment as soon as possible.\n\n" +
                        "Thanks,\n" +
                        "FreelancerOS"
        );
    }

    // Reusable private method
    // All emails go through this one method
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to " + to);
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }
}