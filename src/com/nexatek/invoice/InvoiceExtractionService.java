package com.nexatek.invoice;

import java.nio.file.Path;

public interface InvoiceExtractionService {
    ExtractedInvoice extract(Path document) throws InvoiceExtractionException;

    default void cancel() {
    }
}
