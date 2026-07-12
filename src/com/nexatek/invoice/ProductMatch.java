package com.nexatek.invoice;

public record ProductMatch(int productId, String barcode, String name, String size,
        ExtractedInvoiceItem.MatchingStatus status, double similarity) {
}
