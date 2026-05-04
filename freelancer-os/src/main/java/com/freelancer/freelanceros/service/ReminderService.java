package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Invoice;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class ReminderService {

    private static final String FRONTEND_URL = "http://192.168.1.2:8083";

    public enum ReminderType {
        THREE_DAYS_BEFORE,
        DAY_BEFORE,
        OVERDUE,
        OVERDUE_FOLLOWUP
    }

    private final JavaMailSender mailSender;

    public ReminderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReminder(Invoice invoice, ReminderType type) {
        Client client = invoice.getClient();
        if (client == null || client.getEmail() == null || client.getEmail().isBlank()) return;

        String invoiceRef = "INV-" + invoice.getId();
        String amount     = "&#8377;" + String.format("%,.0f", invoice.getAmount().doubleValue());
        String dueDate    = invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A";
        String portalLink = client.getPortalToken() != null
                ? FRONTEND_URL + "/portal/" + client.getPortalToken() : "#";

        String subject = buildSubject(type, invoiceRef);
        String html    = buildHtml(type, client.getName(), invoiceRef, amount, dueDate, portalLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(client.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("Reminder [" + type + "] sent to " + client.getEmail());
        } catch (Exception e) {
            System.out.println("Reminder email failed: " + e.getMessage());
        }
    }

    private String buildSubject(ReminderType type, String invoiceRef) {
        return switch (type) {
            case THREE_DAYS_BEFORE -> "Payment due in 3 days · " + invoiceRef;
            case DAY_BEFORE        -> "Payment due tomorrow · " + invoiceRef;
            case OVERDUE           -> "Invoice overdue · " + invoiceRef;
            case OVERDUE_FOLLOWUP  -> "Still unpaid · " + invoiceRef;
        };
    }

    private String buildHtml(ReminderType type, String clientName, String invoiceRef,
                             String amount, String dueDate, String portalLink) {

        String badge = switch (type) {
            case THREE_DAYS_BEFORE -> "<span style='background:#854d0e;color:#fef9c3;padding:4px 12px;border-radius:99px;font-size:12px;font-weight:600;'>Due in 3 days</span>";
            case DAY_BEFORE        -> "<span style='background:#7c2d12;color:#fed7aa;padding:4px 12px;border-radius:99px;font-size:12px;font-weight:600;'>Due Tomorrow</span>";
            case OVERDUE           -> "<span style='background:#7f1d1d;color:#fecaca;padding:4px 12px;border-radius:99px;font-size:12px;font-weight:600;'>Overdue</span>";
            case OVERDUE_FOLLOWUP  -> "<span style='background:#7f1d1d;color:#fecaca;padding:4px 12px;border-radius:99px;font-size:12px;font-weight:600;'>Still Unpaid</span>";
        };

        String message = switch (type) {
            case THREE_DAYS_BEFORE -> "This is a friendly reminder that your invoice is due in 3 days.";
            case DAY_BEFORE        -> "Your invoice is due tomorrow. Please arrange payment today.";
            case OVERDUE           -> "Your invoice is now overdue. Please make payment as soon as possible.";
            case OVERDUE_FOLLOWUP  -> "Your invoice is still unpaid after 7 days. Please settle this urgently.";
        };

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#0a0a0a;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="padding:48px 16px;">
                <tr><td align="center">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px;">

                    <!-- Logo -->
                    <tr>
                      <td align="center" style="padding-bottom:40px;">
                        <p style="margin:0;font-size:18px;font-weight:700;color:#fff;letter-spacing:-0.3px;">FreelancerOS</p>
                      </td>
                    </tr>

                    <!-- Card -->
                    <tr>
                      <td style="background:#141414;border-radius:20px;padding:32px;border:1px solid #222;">

                        <!-- Badge -->
                        <div style="margin-bottom:20px;">%s</div>

                        <!-- Top row -->
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td>
                              <p style="margin:0;font-size:12px;color:#555;text-transform:uppercase;letter-spacing:1.2px;">Invoice</p>
                              <p style="margin:4px 0 0;font-size:16px;font-weight:600;color:#fff;">%s</p>
                            </td>
                            <td align="right">
                              <p style="margin:0;font-size:12px;color:#555;text-transform:uppercase;letter-spacing:1.2px;">Due</p>
                              <p style="margin:4px 0 0;font-size:16px;font-weight:600;color:#fff;">%s</p>
                            </td>
                          </tr>
                        </table>

                        <!-- Divider -->
                        <div style="height:1px;background:#222;margin:24px 0;"></div>

                        <!-- Amount -->
                        <p style="margin:0;font-size:12px;color:#555;text-transform:uppercase;letter-spacing:1.2px;">Amount Due</p>
                        <p style="margin:6px 0 0;font-size:40px;font-weight:800;color:#fff;letter-spacing:-1px;">%s</p>

                        <!-- Divider -->
                        <div style="height:1px;background:#222;margin:24px 0;"></div>

                        <!-- Message -->
                        <p style="margin:0;font-size:14px;color:#666;">Hi <strong style="color:#ccc;">%s</strong>, %s</p>

                        <!-- Button -->
                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td>
                              <a href="%s" style="display:block;padding:13px 0;background:#2563eb;color:#fff;
                                 text-decoration:none;border-radius:10px;font-size:14px;font-weight:600;text-align:center;">
                                View &amp; Pay Invoice
                              </a>
                            </td>
                          </tr>
                        </table>

                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td align="center" style="padding-top:28px;">
                        <p style="margin:0;font-size:12px;color:#333;">FreelancerOS &middot; Automated Billing</p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(badge, invoiceRef, dueDate, amount, clientName, message, portalLink);
    }
}