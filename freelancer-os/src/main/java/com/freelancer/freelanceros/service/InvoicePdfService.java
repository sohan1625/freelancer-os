package com.freelancer.freelanceros.service;

import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;

    public InvoicePdfService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    private static final DeviceRgb PRIMARY    = new DeviceRgb(0, 0, 0);
    private static final DeviceRgb MUTED      = new DeviceRgb(107, 114, 128);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(243, 244, 246);
    private static final DeviceRgb ACCENT     = new DeviceRgb(99, 102, 241);

    public byte[] generateInvoicePdf(Long invoiceId) throws IOException {

        // Use JOIN FETCH to load client in same query — avoids LazyInitializationException
        Invoice invoice = invoiceRepository.findByIdWithClient(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(50, 50, 50, 50);

        PdfFont bold    = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ── Header ────────────────────────────────────────────────────────────

        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell brandCell = new Cell().setBorder(Border.NO_BORDER);
        brandCell.add(new Paragraph("FreelancerOS")
                .setFont(bold).setFontSize(20).setFontColor(PRIMARY));
        brandCell.add(new Paragraph("Professional Invoice")
                .setFont(regular).setFontSize(10).setFontColor(MUTED));
        header.addCell(brandCell);

        Cell invoiceNumCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        invoiceNumCell.add(new Paragraph("INVOICE #" + invoice.getId())
                .setFont(bold).setFontSize(16).setFontColor(ACCENT));
        invoiceNumCell.add(new Paragraph("Issued: " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .setFont(regular).setFontSize(10).setFontColor(MUTED));
        header.addCell(invoiceNumCell);

        doc.add(header);

        // ── Divider ───────────────────────────────────────────────────────────

        doc.add(new Paragraph("\n"));
        SolidLine line = new SolidLine(1f);
        line.setColor(LIGHT_GRAY);
        doc.add(new LineSeparator(line));
        doc.add(new Paragraph("\n"));

        // ── Bill To ───────────────────────────────────────────────────────────

        Table billTo = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell billToCell = new Cell().setBorder(Border.NO_BORDER);
        billToCell.add(new Paragraph("BILL TO")
                .setFont(bold).setFontSize(9).setFontColor(MUTED));
        billToCell.add(new Paragraph(invoice.getClient().getName())
                .setFont(bold).setFontSize(14).setFontColor(PRIMARY).setMarginTop(4));
        if (invoice.getClient().getEmail() != null) {
            billToCell.add(new Paragraph(invoice.getClient().getEmail())
                    .setFont(regular).setFontSize(10).setFontColor(MUTED));
        }
        if (invoice.getClient().getPhone() != null) {
            billToCell.add(new Paragraph(invoice.getClient().getPhone())
                    .setFont(regular).setFontSize(10).setFontColor(MUTED));
        }
        billTo.addCell(billToCell);

        Cell dueDateCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        dueDateCell.add(new Paragraph("DUE DATE")
                .setFont(bold).setFontSize(9).setFontColor(MUTED));
        String dueDate = invoice.getDueDate() != null
                ? invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "N/A";
        dueDateCell.add(new Paragraph(dueDate)
                .setFont(bold).setFontSize(14).setFontColor(PRIMARY).setMarginTop(4));
        dueDateCell.add(new Paragraph("Status: " + invoice.getStatus())
                .setFont(regular).setFontSize(10).setFontColor(MUTED));
        billTo.addCell(dueDateCell);

        doc.add(billTo);
        doc.add(new Paragraph("\n"));

        // ── Line Items Table ──────────────────────────────────────────────────

        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"Description", "Qty", "Amount"}) {
            itemsTable.addHeaderCell(
                    new Cell().add(new Paragraph(h)
                                    .setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(PRIMARY)
                            .setPadding(10)
                            .setBorder(Border.NO_BORDER)
            );
        }

        itemsTable.addCell(
                new Cell().add(new Paragraph("Professional Services")
                                .setFont(regular).setFontSize(10))
                        .setPadding(10).setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GRAY)
        );
        itemsTable.addCell(
                new Cell().add(new Paragraph("1")
                                .setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                        .setPadding(10).setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GRAY)
        );
        itemsTable.addCell(
                new Cell().add(new Paragraph(formatAmount(invoice))
                                .setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
                        .setPadding(10).setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GRAY)
        );

        doc.add(itemsTable);
        doc.add(new Paragraph("\n"));

        // ── Total ─────────────────────────────────────────────────────────────

        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        totalTable.addCell(new Cell().setBorder(Border.NO_BORDER));
        Cell totalCell = new Cell().setBorder(new SolidBorder(ACCENT, 2))
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(12)
                .setTextAlignment(TextAlignment.RIGHT);
        totalCell.add(new Paragraph("TOTAL DUE")
                .setFont(bold).setFontSize(9).setFontColor(MUTED));
        totalCell.add(new Paragraph(formatAmount(invoice))
                .setFont(bold).setFontSize(18).setFontColor(ACCENT).setMarginTop(4));
        totalTable.addCell(totalCell);

        doc.add(totalTable);
        doc.add(new Paragraph("\n\n"));

        // ── Footer ────────────────────────────────────────────────────────────

        SolidLine footerLine = new SolidLine(1f);
        footerLine.setColor(LIGHT_GRAY);
        doc.add(new LineSeparator(footerLine));
        doc.add(new Paragraph("\nThank you for your business. Please make payment by the due date.")
                .setFont(regular).setFontSize(9).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Generated by FreelancerOS")
                .setFont(regular).setFontSize(8).setFontColor(LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    private String formatAmount(Invoice invoice) {
        if (invoice.getAmount() == null) return "₹0";
        return "₹" + String.format("%,.2f", invoice.getAmount());
    }
}