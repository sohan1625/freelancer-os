package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final String frontendUrl;
    private final JavaMailSender mailSender;
    private final InvoicePdfService invoicePdfService;

    public EmailService(@Value("${frontend.url:http://localhost:8083}") String frontendUrl,
                        JavaMailSender mailSender,
                        InvoicePdfService invoicePdfService) {
        this.frontendUrl = frontendUrl;
        this.mailSender = mailSender;
        this.invoicePdfService = invoicePdfService;
    }

    @Async
    public void sendInvoiceCreatedEmail(Invoice invoice) {
        String clientEmail = invoice.getClient().getEmail();
        String clientName  = invoice.getClient().getName();

        if (clientEmail == null || clientEmail.isBlank()) {
            log.warn("Client {} has no email — skipping invoice email", clientName);
            return;
        }

        try {
            // Generate PDF bytes
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice.getId());

            MimeMessage message = mailSender.createMimeMessage();
            // multipart = true to support attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(clientEmail);
            helper.setSubject("INV-" + invoice.getId() + " · ₹" + String.format("%,.0f", invoice.getAmount().doubleValue()));
            helper.setText(buildInvoiceHtml(invoice), true);

            // Attach PDF
            helper.addAttachment(
                    "invoice-" + invoice.getId() + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
            log.info("Invoice email with PDF sent to {} for invoice #{}", clientEmail, invoice.getId());

        } catch (MessagingException e) {
            log.error("Failed to send invoice email to {}: {}", clientEmail, e.getMessage());
        } catch (IOException e) {
            log.error("Failed to generate PDF for invoice #{}: {}", invoice.getId(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Reset your FreelancerOS password");
            helper.setText(buildResetHtml(resetLink), true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildInvoiceHtml(Invoice invoice) {
        String clientName  = invoice.getClient().getName();
        String amount      = "&#8377;" + String.format("%,.0f", invoice.getAmount().doubleValue());
        String dueDate     = invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A";
        String portalToken = invoice.getClient().getPortalToken();
        String portalLink  = portalToken != null ? FRONTEND_URL + "/portal/" + portalToken : "#";
        String invoiceRef  = "INV-" + invoice.getId();

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

                        <!-- Client -->
                        <p style="margin:0;font-size:14px;color:#666;">Hi <strong style="color:#ccc;">%s</strong>, this invoice is ready for payment.</p>
                        <p style="margin:8px 0 0;font-size:13px;color:#555;">The invoice PDF is attached to this email.</p>

                        <!-- Buttons -->
                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td width="49%%">
                              <a href="%s" style="display:block;padding:13px 0;background:#2563eb;color:#fff;
                                 text-decoration:none;border-radius:10px;font-size:14px;font-weight:600;text-align:center;">
                                Pay Now
                              </a>
                            </td>
                            <td width="2%%"></td>
                            <td width="49%%">
                              <a href="%s" style="display:block;padding:13px 0;background:transparent;color:#888;
                                 text-decoration:none;border-radius:10px;font-size:14px;font-weight:600;
                                 text-align:center;border:1px solid #2a2a2a;">
                                View Portal
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
            """.formatted(invoiceRef, dueDate, amount, clientName, portalLink, portalLink);
    }

    private String buildResetHtml(String resetLink) {
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
                        <p style="margin:0;font-size:18px;font-weight:700;color:#fff;">FreelancerOS</p>
                      </td>
                    </tr>

                    <!-- Card -->
                    <tr>
                      <td style="background:#141414;border-radius:20px;padding:32px;border:1px solid #222;">
                        <p style="margin:0;font-size:22px;font-weight:700;color:#fff;">Password reset</p>
                        <p style="margin:10px 0 0;font-size:14px;color:#666;line-height:1.6;">
                          Click below to set a new password. Link expires in 1 hour.
                        </p>
                        <table cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td>
                              <a href="%s" style="display:inline-block;padding:13px 28px;background:#2563eb;
                                 color:#fff;text-decoration:none;border-radius:10px;font-size:14px;font-weight:600;">
                                Reset Password
                              </a>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:20px 0 0;font-size:12px;color:#444;">
                          Didn't request this? Ignore this email.
                        </p>
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
            """.formatted(resetLink);
    }
}