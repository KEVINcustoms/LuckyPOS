package com.nexatek.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceValidationServiceTest {

    @Test
    void acceptsConsistentInvoiceWithinOneUnitRoundingTolerance() {
        ExtractedInvoice invoice = validInvoice();
        InvoiceValidationResult result = new InvoiceValidationService(new BigDecimal("1.00"))
                .validate(invoice, (supplier, number) -> false);
        assertFalse(result.hasBlockingErrors(), () -> result.messages().toString());
        assertEquals(1, result.count(ValidationMessage.Severity.GREEN));
    }

    @Test
    void blocksLineAndInvoiceTotalMismatches() {
        ExtractedInvoice invoice = validInvoice();
        invoice.getItems().get(0).setAmount(new BigDecimal("220000"));
        InvoiceValidationResult result = new InvoiceValidationService(BigDecimal.ONE)
                .validate(invoice, (supplier, number) -> false);
        assertTrue(result.hasBlockingErrors());
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("item.total")));
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("invoice.subtotal.mismatch")));
    }

    @Test
    void blocksDuplicateAndUnmatchedProducts() {
        ExtractedInvoice invoice = validInvoice();
        invoice.getItems().get(0).setMatchedProductId(null);
        invoice.getItems().get(0).setMatchingStatus(ExtractedInvoiceItem.MatchingStatus.UNMATCHED);
        InvoiceValidationResult result = new InvoiceValidationService(BigDecimal.ONE)
                .validate(invoice, (supplier, number) -> true);
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("invoice.duplicate")));
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("item.product")));
    }

    @Test
    void blocksMissingSupplierDatesAndItemFields() {
        ExtractedInvoice invoice = new ExtractedInvoice();
        invoice.getItems().add(new ExtractedInvoiceItem());
        InvoiceValidationResult result = new InvoiceValidationService(BigDecimal.ONE).validate(invoice, null);
        assertTrue(result.hasBlockingErrors());
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("invoice.number")));
        assertTrue(result.messages().stream().anyMatch(message -> message.code().equals("item.quantity")));
    }

    private ExtractedInvoice validInvoice() {
        ExtractedInvoice invoice = new ExtractedInvoice();
        invoice.setSupplierId(1);
        invoice.setSupplierName("Kansai");
        invoice.setInvoiceNumber("INV-1");
        invoice.setInvoiceDate(LocalDate.of(2026, 7, 10));
        invoice.setSubtotal(new BigDecimal("229584"));
        invoice.setTax(new BigDecimal("41325"));
        invoice.setTotal(new BigDecimal("270910")); // one-unit currency rounding difference is allowed
        invoice.setOverallConfidence(0.95);
        ExtractedInvoiceItem item = new ExtractedInvoiceItem();
        item.setSupplierProductCode("50360-001");
        item.setDescription("VINYL SILK TINTING BASE WO 1LTR");
        item.setQuantity(new BigDecimal("16"));
        item.setUnitPrice(new BigDecimal("14349"));
        item.setAmount(new BigDecimal("229584"));
        item.setConfidence(0.95);
        item.setMatchedProductId(10);
        item.setMatchedProductName("Vinyl Silk");
        item.setMatchingStatus(ExtractedInvoiceItem.MatchingStatus.EXACT_CODE);
        invoice.getItems().add(item);
        return invoice;
    }
}
