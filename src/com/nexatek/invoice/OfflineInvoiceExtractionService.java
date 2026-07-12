package com.nexatek.invoice;

import java.io.IOException;
import java.nio.file.Path;

/** Explicit fallback that preserves the former local OCR workflow without pretending missing headers are known. */
public final class OfflineInvoiceExtractionService implements InvoiceExtractionService {

    @Override
    public ExtractedInvoice extract(Path document) throws InvoiceExtractionException {
        try {
            InvoiceParseResult result = new InvoiceImportService().extract(document);
            ExtractedInvoice invoice = new ExtractedInvoice();
            invoice.setOriginalDocumentPath(document.toAbsolutePath().normalize());
            invoice.setExtractionMethod("Offline Tesseract - " + result.getExtractionMethod());
            invoice.setRawExtractedText(result.getRawText());
            invoice.setExtractionWarnings(result.getWarnings());
            new OfflineInvoiceMetadataParser().populate(invoice, result.getRawText());
            invoice.setOverallConfidence(result.getLines().stream()
                    .mapToInt(InvoiceLineDraft::getConfidence).average().orElse(0.0) / 100.0);
            for (InvoiceLineDraft draft : result.getLines()) {
                ExtractedInvoiceItem item = new ExtractedInvoiceItem();
                item.setSupplierProductCode(draft.getProductCode());
                item.setDescription(draft.getDescription());
                item.setWarehouse(draft.getWarehouse());
                item.setQuantity(draft.getQuantity());
                item.setUnitPrice(draft.getUnitCost());
                item.setAmount(draft.getLineTotal());
                item.setConfidence(draft.getConfidence() / 100.0);
                invoice.getItems().add(item);
            }
            if (invoice.getSubtotal() == null && !result.getLines().isEmpty()) {
                invoice.setSubtotal(result.getComputedSubtotal());
            }
            return invoice;
        } catch (IOException ex) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                    "Offline OCR could not read the selected invoice. " + safeDetail(ex), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.TIMEOUT,
                    "Offline OCR was cancelled.", ex);
        }
    }

    private String safeDetail(Exception failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank() ? "Check that Tesseract is installed and the image is readable." : message;
    }
}
