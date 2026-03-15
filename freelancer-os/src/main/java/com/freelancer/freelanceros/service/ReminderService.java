package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ReminderService {

    private final JavaMailSender mailSender;

    public ReminderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoiceCreatedNotification(Invoice invoice) {
        Client client = invoice.getClient();
        if (client == null) return;

        String invoiceNumber = "INV-" + invoice.getId();
        String formattedAmount = formatAmount(invoice.getAmount().doubleValue());
        String formattedDate = formatDate(invoice.getDueDate().toString());
        String subject = "Invoice " + invoiceNumber + " – ₹" + formattedAmount + " Due";

        String html = buildHtml(
                client.getName(),
                invoiceNumber,
                formattedAmount,
                formattedDate,
                invoice.getStatus(),
                client.getEmail(),
                client.getPhone() != null ? client.getPhone() : "N/A",
                "You have received a new invoice. Please review the details below and ensure payment is completed before the due date.",
                "#4CAF50",
                "NEW INVOICE"
        );

        sendHtmlEmail(client.getEmail(), subject, html);
    }

    public void sendReminder(Invoice invoice) {
        Client client = invoice.getClient();
        if (client == null) return;

        String invoiceNumber = "INV-" + invoice.getId();
        String formattedAmount = formatAmount(invoice.getAmount().doubleValue());
        String formattedDate = formatDate(invoice.getDueDate().toString());
        String subject = "Payment Reminder – Invoice " + invoiceNumber + " is " + invoice.getStatus();

        String accentColor = invoice.getStatus().equalsIgnoreCase("OVERDUE") ? "#E53935" : "#FB8C00";
        String badge = invoice.getStatus().equalsIgnoreCase("OVERDUE") ? "OVERDUE" : "REMINDER";

        String html = buildHtml(
                client.getName(),
                invoiceNumber,
                formattedAmount,
                formattedDate,
                invoice.getStatus(),
                client.getEmail(),
                client.getPhone() != null ? client.getPhone() : "N/A",
                "This is a reminder that your invoice is still unpaid. Please ensure payment is completed as soon as possible.",
                accentColor,
                badge
        );

        sendHtmlEmail(client.getEmail(), subject, html);
    }

    private String buildHtml(
            String clientName,
            String invoiceNumber,
            String amount,
            String dueDate,
            String status,
            String email,
            String phone,
            String message,
            String accentColor,
            String badge
    ) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "  body { margin: 0; padding: 0; background: #f4f6f9; font-family: 'Segoe UI', Arial, sans-serif; color: #333; }" +
                "  .wrapper { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }" +
                "  .header { background: " + accentColor + "; padding: 32px 40px; text-align: center; }" +
                "  .header h1 { margin: 0; color: #ffffff; font-size: 22px; font-weight: 700; letter-spacing: 1px; }" +
                "  .header p { margin: 6px 0 0; color: rgba(255,255,255,0.85); font-size: 13px; }" +
                "  .badge { display: inline-block; background: rgba(255,255,255,0.2); color: #fff; font-size: 11px; font-weight: 700; letter-spacing: 2px; padding: 4px 12px; border-radius: 20px; margin-bottom: 10px; }" +
                "  .body { padding: 36px 40px; }" +
                "  .greeting { font-size: 16px; color: #333; margin-bottom: 8px; }" +
                "  .message { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 28px; }" +
                "  .section-title { font-size: 11px; font-weight: 700; color: #999; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 12px; }" +
                "  .card { background: #f9fafc; border-radius: 8px; padding: 20px 24px; margin-bottom: 20px; }" +
                "  .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }" +
                "  .row:last-child { border-bottom: none; }" +
                "  .row .label { font-size: 13px; color: #888; }" +
                "  .row .value { font-size: 13px; color: #333; font-weight: 600; }" +
                "  .amount-highlight { font-size: 28px; font-weight: 700; color: " + accentColor + "; text-align: center; padding: 16px 0 8px; }" +
                "  .status-badge { display: inline-block; padding: 4px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; background: " + accentColor + "22; color: " + accentColor + "; }" +
                "  .footer { background: #f9fafc; padding: 24px 40px; text-align: center; border-top: 1px solid #eee; }" +
                "  .footer p { margin: 4px 0; font-size: 12px; color: #aaa; }" +
                "  .footer .brand { font-size: 14px; font-weight: 700; color: #555; margin-bottom: 4px; }" +
                "</style></head><body>" +
                "<div class='wrapper'>" +

                // Header
                "  <div class='header'>" +
                "    <div class='badge'>" + badge + "</div>" +
                "    <h1>FreelancerOS</h1>" +
                "    <p>Automated Billing &amp; Payment Tracking</p>" +
                "  </div>" +

                // Body
                "  <div class='body'>" +
                "    <p class='greeting'>Hello, <strong>" + clientName + "</strong> 👋</p>" +
                "    <p class='message'>" + message + "</p>" +

                // Amount highlight
                "    <div class='amount-highlight'>₹" + amount + "</div>" +

                // Invoice Details
                "    <p class='section-title'>Invoice Details</p>" +
                "    <div class='card'>" +
                "      <div class='row'><span class='label'>Invoice Number</span><span class='value'>" + invoiceNumber + "</span></div>" +
                "      <div class='row'><span class='label'>Amount Due</span><span class='value'>₹" + amount + "</span></div>" +
                "      <div class='row'><span class='label'>Due Date</span><span class='value'>" + dueDate + "</span></div>" +
                "      <div class='row'><span class='label'>Status</span><span class='value'><span class='status-badge'>" + status + "</span></span></div>" +
                "    </div>" +

                // Client Details
                "    <p class='section-title'>Client Information</p>" +
                "    <div class='card'>" +
                "      <div class='row'><span class='label'>Name</span><span class='value'>" + clientName + "</span></div>" +
                "      <div class='row'><span class='label'>Email</span><span class='value'>" + email + "</span></div>" +
                "      <div class='row'><span class='label'>Phone</span><span class='value'>" + phone + "</span></div>" +
                "    </div>" +

                "  </div>" +

                // Footer
                "  <div class='footer'>" +
                "    <p class='brand'>FreelancerOS</p>" +
                "    <p>If payment has already been made, kindly ignore this message.</p>" +
                "    <p>This is an automated email. Please do not reply.</p>" +
                "  </div>" +

                "</div></body></html>";
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ HTML Email sent to " + to);
        } catch (Exception e) {
            System.out.println("❌ Email failed: " + e.getMessage());
        }
    }

    private String formatAmount(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(amount);
    }

    private String formatDate(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}