package com.nexatek.invoice;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceTableParserTest {

    @Test
    void parsesKansaiLayoutAndRejectsInvoiceFooter() {
        String text = """
            KANSAI PLASCON UGANDA LIMITED
            TAX INVOICE M000881684
            Code Description Warehouse Ship quantity Unit price Gross amount
            50360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000 229,584.00
            50361-001 WEATHERGUARD TINTING BASE WO 1LTR FG 16 15,905.000 254,480.00
            50243-001 STANDARD LACQUER THINNER 1LTR FG 30 12,203.000 366,090.00
            56013-001 WHITE SPIRIT 1LTR FG 30 8,983.000 269,490.00
            Total gross amount 1,119,644.00
            Total tax 201,535.00
            Total invoice 1,321,180.00
            """;
        InvoiceParseResult result = new InvoiceTableParser().parseText(text, "fixture");
        assertEquals(4, result.getLines().size());
        assertEquals("50360-001", result.getLines().get(0).getProductCode());
        assertEquals(0, new BigDecimal("16").compareTo(result.getLines().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("14349").compareTo(result.getLines().get(0).getUnitCost()));
        assertEquals(0, new BigDecimal("1119644").compareTo(result.getComputedSubtotal()));
    }

    @Test
    void doesNotTurnQuantityTenIntoOneOrDropProductNamedTotal() {
        String text = """
            Code Description Qty Unit Price Gross Amount
            000123 TOTAL WALL CARE PUTTY 10 2,500.00 25,000.00
            """;
        InvoiceParseResult result = new InvoiceTableParser().parseText(text, "fixture");
        assertEquals(1, result.getLines().size());
        assertEquals("000123", result.getLines().get(0).getProductCode());
        assertEquals(0, BigDecimal.TEN.compareTo(result.getLines().get(0).getQuantity()));
        assertEquals("TOTAL WALL CARE PUTTY", result.getLines().get(0).getDescription());
    }

    @Test
    void keepsMissingCodeUnknownAndFlagsArithmeticMismatch() {
        String text = """
            Description Qty Unit Price Gross Amount
            BLUE HAMMER PAINT 2 48,500 90,000
            """;
        InvoiceLineDraft item = new InvoiceTableParser().parseText(text, "fixture").getLines().get(0);
        assertTrue(item.getProductCode().isBlank());
        assertFalse(item.totalsMatch());
        assertTrue(item.requiresReview());
    }

    @Test
    void keepsRowsWithMissingLineTotalWhenTheDescriptionAndNumbersAreStillVisible() {
        String text = """
            Code Description Qty Unit Price
            50360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000
            """;
        InvoiceParseResult result = new InvoiceTableParser().parseText(text, "fixture");
        assertEquals(1, result.getLines().size());
        assertEquals("50360-001", result.getLines().get(0).getProductCode());
        assertEquals("VINYL SILK TINTING BASE WO 1LTR FG", result.getLines().get(0).getDescription());
        assertEquals(0, new BigDecimal("16").compareTo(result.getLines().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("14349").compareTo(result.getLines().get(0).getUnitCost()));
        assertNull(result.getLines().get(0).getLineTotal());
    }

    @Test
    void preservesRowsInLowConfidenceFallbackParsingWhenTheLineTotalIsMissing() throws Exception {
        String text = """
            Code Description Qty Unit Price
            50360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000
            """;
        InvoiceTableParser parser = new InvoiceTableParser();
        Method parseText = InvoiceTableParser.class.getDeclaredMethod("parseText", String.class, String.class, int.class);
        parseText.setAccessible(true);
        InvoiceParseResult result = (InvoiceParseResult) parseText.invoke(parser, text, "fixture", 70);
        assertEquals(1, result.getLines().size());
        assertEquals("50360-001", result.getLines().get(0).getProductCode());
        assertEquals("VINYL SILK TINTING BASE WO 1LTR FG", result.getLines().get(0).getDescription());
        assertEquals(0, new BigDecimal("16").compareTo(result.getLines().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("14349").compareTo(result.getLines().get(0).getUnitCost()));
    }
}
