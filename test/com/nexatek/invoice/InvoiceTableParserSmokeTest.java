package com.nexatek.invoice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvoiceTableParserSmokeTest {
    public static void main(String[] args) {
        InvoiceTableParser parser = new InvoiceTableParser();

        List<OcrWord> words = new ArrayList<>();
        words.addAll(List.of(
                new OcrWord("Description", 1, 1, 1, 1, 40, 20, 120, 18, 95),
                new OcrWord("Qty", 1, 1, 1, 1, 220, 20, 40, 18, 95),
                new OcrWord("Price", 1, 1, 1, 1, 320, 20, 70, 18, 95),
                new OcrWord("Amount", 1, 1, 1, 1, 430, 20, 70, 18, 95)
        ));
        words.addAll(List.of(
                new OcrWord("Widget", 1, 1, 1, 2, 40, 55, 70, 18, 90),
                new OcrWord("A", 1, 1, 1, 2, 120, 55, 20, 18, 90),
                new OcrWord("2", 1, 1, 1, 2, 220, 55, 20, 18, 90),
                new OcrWord("15", 1, 1, 1, 2, 320, 55, 20, 18, 90),
                new OcrWord("30", 1, 1, 1, 2, 430, 55, 20, 18, 90)
        ));
        words.addAll(List.of(
                new OcrWord("Discount", 1, 1, 1, 3, 40, 90, 90, 18, 90),
                new OcrWord("1", 1, 1, 1, 3, 220, 90, 20, 18, 90),
                new OcrWord("10", 1, 1, 1, 3, 320, 90, 20, 18, 90),
                new OcrWord("10", 1, 1, 1, 3, 430, 90, 20, 18, 90)
        ));
        words.addAll(List.of(
                new OcrWord("Service", 1, 1, 1, 4, 40, 125, 70, 18, 90),
                new OcrWord("B", 1, 1, 1, 4, 120, 125, 20, 18, 90),
                new OcrWord("1", 1, 1, 1, 4, 220, 125, 20, 18, 90),
                new OcrWord("20", 1, 1, 1, 4, 320, 125, 20, 18, 90),
                new OcrWord("20", 1, 1, 1, 4, 430, 125, 20, 18, 90)
        ));
        words.addAll(List.of(
                new OcrWord("Items", 1, 1, 1, 5, 40, 160, 45, 18, 90),
                new OcrWord("Sold", 1, 1, 1, 5, 90, 160, 50, 18, 90),
                new OcrWord("2", 1, 1, 1, 5, 220, 160, 20, 18, 90),
                new OcrWord("10", 1, 1, 1, 5, 320, 160, 20, 18, 90),
                new OcrWord("20", 1, 1, 1, 5, 430, 160, 20, 18, 90)
        ));

        OcrPage page = new OcrPage(1, 600, 300, words, "Description Qty Price Amount\nWidget A 2 15 30\nDiscount 1 10 10\nService B 1 20 20\nItems Sold 2 10 20");
        InvoiceParseResult result = parser.parseOcr(List.of(page), "test");

        if (result.getLines().size() != 2) {
            throw new AssertionError("Expected 2 product rows, got " + result.getLines().size());
        }
        if (!"Widget A".equals(result.getLines().get(0).getDescription())) {
            throw new AssertionError("First row should be Widget A, got " + result.getLines().get(0).getDescription());
        }
        if (!"Service B".equals(result.getLines().get(1).getDescription())) {
            throw new AssertionError("Second row should be Service B, got " + result.getLines().get(1).getDescription());
        }

        try {
            Method parseTextRow = InvoiceTableParser.class.getDeclaredMethod("parseTextRow", String.class, int.class);
            parseTextRow.setAccessible(true);
            InvoiceLineDraft fallbackLine = (InvoiceLineDraft) parseTextRow.invoke(parser,
                    "50360-001 VINYL SILK TINTING BASE WO 1LTR FG 16 14,349.000", 70);
            if (fallbackLine == null) {
                throw new AssertionError("Expected low-confidence fallback row to be preserved even without a line total");
            }
        } catch (Exception ex) {
            throw new AssertionError("Low-confidence fallback parsing failed: " + ex.getMessage(), ex);
        }
        System.out.println("Invoice parser smoke test passed");
    }
}
