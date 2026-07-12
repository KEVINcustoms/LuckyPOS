package com.nexatek.invoice;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceMediaIntegrationTest {

    @TempDir Path temporary;

    @Test
    void extractsNativeTextPdfWithoutCallingAzure() throws Exception {
        Path pdf = temporary.resolve("invoice.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.setLeading(18);
                content.newLineAtOffset(40, 740);
                for (String line : List.of(
                        "Code Description Warehouse Ship quantity Unit price Gross amount",
                        "50360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000 229,584.00",
                        "50361-001 WEATHERGUARD TINTING BASE WO 1LTR FG 16 15,905.000 254,480.00")) {
                    content.showText(line);
                    content.newLine();
                }
                content.endText();
            }
            document.save(pdf.toFile());
        }
        InvoiceParseResult result = new InvoiceImportService().extract(pdf);
        assertEquals(2, result.getLines().size());
        assertTrue(result.getExtractionMethod().contains("PDF embedded text"));
    }

    @Test
    void installedTesseractReadsPreprocessedInvoicePhoto() throws Exception {
        Assumptions.assumeTrue(Files.isRegularFile(Path.of("C:/Program Files/Tesseract-OCR/tesseract.exe")),
                "Tesseract is not installed on this test machine");
        BufferedImage image = new BufferedImage(2200, 900, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(248, 244, 232));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, 34));
        int y = 180;
        for (String line : List.of(
                "Code       Description                              Ship quantity    Unit price    Gross amount",
                "50360-001  VINYL SILK TINTING BASE WO 1LTR          16               14,349.000    229,584.00",
                "50361-001  WEATHERGUARD TINTING BASE WO 1LTR        16               15,905.000    254,480.00")) {
            graphics.drawString(line, 80, y);
            y += 90;
        }
        graphics.dispose();

        Path photo = temporary.resolve("supplier-invoice.png");
        assertTrue(ImageIO.write(image, "png", photo.toFile()));

        // Exercise the exact service invoked by the Run Offline OCR button.
        ExtractedInvoice result = new OfflineInvoiceExtractionService().extract(photo);
        assertEquals(2, result.getItems().size(), result.getRawExtractedText());
        assertEquals("50360-001", result.getItems().get(0).getSupplierProductCode());
        assertEquals(0, new java.math.BigDecimal("229584.00").compareTo(result.getItems().get(0).getAmount()),
                () -> "amount=" + result.getItems().get(0).getAmount() + "\n" + result.getRawExtractedText());
        assertTrue(result.getExtractionMethod().startsWith("Offline Tesseract"));
    }
}
