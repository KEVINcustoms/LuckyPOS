package com.nexatek.invoice;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class OfflineInvoiceExtractionServiceTest {

    @TempDir Path temporary;

    @Test
    void retainsOcrTextWarningsAndExtractsKansaiStyleMetadataAndItems() throws Exception {
        Path invoiceFile = temporary.resolve("kansai-invoice.txt");
        Files.writeString(invoiceFile, """
                KANSAI PLASCON UGANDA LIMITED
                Plot 28, Block 112, Namanve Business Park, P.O. Box 4627, Tel: 200529801/4
                Reg VAT No: 10967-J
                Tax TIN: 1000026502
                Tel: 200529801/4
                CUSTOMER COPY
                TAX INVOICE
                M000881684
                Invoice date 10/07/2026
                Sales order M000817361
                Order date 10/07/2026
                Ship date 10/07/2026
                Salesperson JUDITH NAMANDE
                Customer purchase order no BASES /
                Customer code FRA63
                Customer name FRAMERA ENTERPRISES LIMITED
                Customer address KAMPALA
                Customer TIN 1015848921
                Customer telephone 0783 356940
                Shipping address FRAMERA ENTERPRISES LIMITED
                Code Description Warehouse Ship quantity Unit price Gross amount
                P275-0360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000 229,584.00
                P290-0360-001 WEATHERGUARD TINTING BASE WO 1LTR FG 16 15,905.000 254,480.00
                P803-0243-001 STANDARD LACQUER THINNER 1LTR FG 30 12,203.000 366,090.00
                P809-6013-001 WHITE SPIRIT 1LTR FG 30 8,983.000 269,490.00
                FDN 126094214017
                Verification Code 126746266678674036
                Prepared by WARIMU PATRICIA
                Printed on 10/07/2026 11:08:50
                Total gross amount 1,119,644.00
                Total discount 0.00
                Total freight 0.00
                Misc charges 0.00
                Total tax 201,535.00
                Total invoice 1,321,180.00
                Currency UGX
                """);

        ExtractedInvoice result = new OfflineInvoiceExtractionService().extract(invoiceFile);

        assertEquals("KANSAI PLASCON UGANDA LIMITED", result.getSupplierName());
        assertEquals("M000881684", result.getInvoiceNumber());
        assertEquals("TAX INVOICE", result.getDocumentType());
        assertEquals("CUSTOMER COPY", result.getCopyType());
        assertEquals("10967-J", result.getSupplierVatRegistrationNumber());
        assertEquals("1000026502", result.getSupplierTaxId());
        assertEquals("Plot 28, Block 112, Namanve Business Park, P.O. Box 4627", result.getSupplierAddress());
        assertEquals("200529801/4", result.getSupplierTelephone());
        assertEquals("M000817361", result.getSalesOrderNumber());
        assertEquals("JUDITH NAMANDE", result.getSalesperson());
        assertEquals("BASES /", result.getPurchaseOrderNumber());
        assertEquals("FRA63", result.getCustomerCode());
        assertEquals("FRAMERA ENTERPRISES LIMITED", result.getCustomerName());
        assertEquals("KAMPALA", result.getCustomerAddress());
        assertEquals("1015848921", result.getCustomerTaxId());
        assertEquals("0783 356940", result.getCustomerTelephone());
        assertEquals("FRAMERA ENTERPRISES LIMITED", result.getShippingAddress());
        assertEquals("126094214017", result.getFiscalDocumentNumber());
        assertEquals("126746266678674036", result.getVerificationCode());
        assertEquals("WARIMU PATRICIA", result.getPreparedBy());
        assertEquals("10/07/2026 11:08:50", result.getPrintedAt());
        assertEquals(LocalDate.of(2026, 7, 10), result.getInvoiceDate());
        assertEquals(LocalDate.of(2026, 7, 10), result.getOrderDate());
        assertEquals(LocalDate.of(2026, 7, 10), result.getShipDate());
        assertEquals(0, new BigDecimal("1119644.00").compareTo(result.getSubtotal()));
        assertEquals(0, new BigDecimal("201535.00").compareTo(result.getTax()));
        assertEquals(0, new BigDecimal("1321180.00").compareTo(result.getTotal()));
        assertEquals("UGX", result.getCurrency());
        assertEquals(4, result.getItems().size());
        assertEquals("P275-0360-001", result.getItems().get(0).getSupplierProductCode());
        assertEquals("P809-6013-001", result.getItems().get(3).getSupplierProductCode());
        assertEquals("FG", result.getItems().get(0).getWarehouse());
        assertTrue(result.getExtractionMethod().startsWith("Offline Tesseract"));
        assertTrue(result.getRawExtractedText().contains("VINYL SILK"));
    }
}
