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

        String paymentUrl = "https://freelanceros.com/pay/" + invoice.getId();

        String html = buildHtml(
                client.getName(),
                invoiceNumber,
                formattedAmount,
                formattedDate,
                paymentUrl
        );

        sendHtmlEmail(client.getEmail(), subject, html);
    }

    public void sendReminder(Invoice invoice) {

        Client client = invoice.getClient();
        if (client == null) return;

        String invoiceNumber = "INV-" + invoice.getId();
        String formattedAmount = formatAmount(invoice.getAmount().doubleValue());
        String formattedDate = formatDate(invoice.getDueDate().toString());

        String subject = "Payment Reminder – Invoice " + invoiceNumber;

        String paymentUrl = "https://freelanceros.com/pay/" + invoice.getId();

        String html = buildHtml(
                client.getName(),
                invoiceNumber,
                formattedAmount,
                formattedDate,
                paymentUrl
        );

        sendHtmlEmail(client.getEmail(), subject, html);
    }

    private String buildHtml(
            String clientName,
            String invoiceNumber,
            String amount,
            String dueDate,
            String paymentUrl
    ) {

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width'>" +
                "</head>" +

                "<body style='margin:0;padding:0;background:#f4f6f8;font-family:Arial,Helvetica,sans-serif;'>"

                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f8;padding:40px 0;'>"
                + "<tr>"
                + "<td align='center'>"

                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:10px;padding:40px;'>"

                // HEADER
                + "<tr>"
                + "<td align='center' style='padding-bottom:30px;'>"
                + "<h2 style='margin:0;color:#111;'>FreelancerOS</h2>"
                + "<p style='margin:6px 0;color:#777;font-size:13px;'>Automated Billing for Freelancers</p>"
                + "</td>"
                + "</tr>"

                // GREETING
                + "<tr>"
                + "<td style='padding-bottom:10px;'>"
                + "<h2 style='margin:0;font-size:22px;color:#111;'>Hi " + clientName + ",</h2>"
                + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style='color:#555;font-size:15px;line-height:1.6;padding-bottom:25px;'>"
                + "Thanks for using FreelancerOS. This is an invoice for your recent purchase."
                + "</td>"
                + "</tr>"

                // INVOICE CARD
                + "<tr>"
                + "<td style='padding-bottom:30px;'>"

                + "<table width='100%' cellpadding='0' cellspacing='0' "
                + "style='background:#f8fafc;border-radius:8px;padding:24px;'>"

                + "<tr>"
                + "<td style='padding-bottom:14px;'>"
                + "<div style='font-size:13px;color:#6b7280;'>Invoice</div>"
                + "<div style='font-size:16px;font-weight:600;color:#111;'>" + invoiceNumber + "</div>"
                + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style='padding-bottom:14px;'>"
                + "<div style='font-size:13px;color:#6b7280;'>Amount Due</div>"
                + "<div style='font-size:20px;font-weight:700;color:#111;'>₹" + amount + "</div>"
                + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td>"
                + "<div style='font-size:13px;color:#6b7280;'>Due Date</div>"
                + "<div style='font-size:16px;font-weight:600;color:#111;'>" + dueDate + "</div>"
                + "</td>"
                + "</tr>"

                + "</table>"
                + "</td>"
                + "</tr>"

                // BUTTONS
                + "<tr>"
                + "<td align='center' style='padding-bottom:30px;'>"

                + "<a href='" + paymentUrl + "' "
                + "style='background:#2563eb;color:#ffffff;padding:14px 28px;border-radius:6px;"
                + "text-decoration:none;font-weight:600;font-size:14px;margin-right:10px;'>"
                + "Pay Invoice"
                + "</a>"

                + "<a href='" + paymentUrl + "' "
                + "style='border:1px solid #d1d5db;color:#111;padding:14px 26px;border-radius:6px;"
                + "text-decoration:none;font-size:14px;'>"
                + "View Invoice"
                + "</a>"

                + "</td>"
                + "</tr>"

                // BREAKDOWN TABLE
                + "<tr>"
                + "<td style='padding-bottom:30px;'>"

                + "<table width='100%' cellpadding='12' cellspacing='0' "
                + "style='border:1px solid #eee;border-radius:6px;font-size:14px;'>"

                + "<tr style='background:#fafafa;'>"
                + "<td>Description</td>"
                + "<td align='right'>Amount</td>"
                + "</tr>"

                + "<tr>"
                + "<td>Invoice Payment</td>"
                + "<td align='right'>₹" + amount + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style='font-weight:600;'>Total</td>"
                + "<td align='right' style='font-weight:600;'>₹" + amount + "</td>"
                + "</tr>"

                + "</table>"
                + "</td>"
                + "</tr>"

                // SUPPORT TEXT
                + "<tr>"
                + "<td style='color:#666;font-size:14px;line-height:1.6;padding-bottom:20px;'>"
                + "If you have any questions about this invoice, simply reply to this email "
                + "or contact our support team."
                + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style='padding-bottom:30px;'>"
                + "Cheers,<br>The FreelancerOS Team"
                + "</td>"
                + "</tr>"

                // DIVIDER
                + "<tr>"
                + "<td><hr style='border:none;border-top:1px solid #eee;'></td>"
                + "</tr>"

                // FALLBACK LINK
                + "<tr>"
                + "<td style='font-size:13px;color:#777;padding-top:20px;'>"
                + "If you're having trouble with the button above, copy and paste the URL below into your browser."
                + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style='font-size:13px;color:#2563eb;padding-top:10px;'>"
                + paymentUrl +
                "</td>"
                + "</tr>"

                + "</table>"

                // FOOTER
                + "<table width='600' style='margin-top:20px;text-align:center;color:#999;font-size:12px;'>"
                + "<tr><td><strong>FreelancerOS</strong></td></tr>"
                + "<tr><td>Automated Billing for Freelancers</td></tr>"
                + "<tr><td>support@freelanceros.com</td></tr>"
                + "<tr><td>© 2026 FreelancerOS</td></tr>"
                + "</table>"

                + "</td>"
                + "</tr>"
                + "</table>"

                + "</body></html>";
    }

    private void sendHtmlEmail(String to, String subject, String html) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
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