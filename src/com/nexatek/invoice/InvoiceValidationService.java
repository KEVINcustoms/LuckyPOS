package com.nexatek.invoice;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Blocking and advisory checks applied after every extraction/user correction and before stock receipt. */
public final class InvoiceValidationService {

    private static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("1.00");
    private final BigDecimal tolerance;

    public InvoiceValidationService() {
        this(readTolerance(System.getenv("LUCKYPOS_INVOICE_ROUNDING_TOLERANCE")));
    }

    public InvoiceValidationService(BigDecimal tolerance) {
        this.tolerance = tolerance == null || tolerance.signum() < 0 ? DEFAULT_TOLERANCE : tolerance;
    }

    public InvoiceValidationResult validate(ExtractedInvoice invoice, DuplicateInvoiceChecker duplicateChecker) {
        List<ValidationMessage> messages = new ArrayList<>();
        if (invoice == null) {
            messages.add(red("invoice.missing", "No invoice has been analyzed.", null));
            return new InvoiceValidationResult(messages);
        }

        if (blank(invoice.getInvoiceNumber())) messages.add(red("invoice.number", "Invoice number is required.", null));
        if (invoice.getSupplierId() == null) messages.add(red("invoice.supplier", "Select the matching supplier.", null));
        if (invoice.getInvoiceDate() == null) messages.add(red("invoice.date", "Invoice date is required.", null));
        else if (invoice.getInvoiceDate().isAfter(LocalDate.now().plusDays(1)))
            messages.add(red("invoice.date.future", "Invoice date cannot be in the future.", null));
        if (invoice.getItems().isEmpty()) messages.add(red("invoice.items", "At least one invoice item is required.", null));

        if (invoice.getSupplierId() != null && !blank(invoice.getInvoiceNumber()) && duplicateChecker != null) {
            try {
                if (duplicateChecker.exists(invoice.getSupplierId(), invoice.getInvoiceNumber()))
                    messages.add(red("invoice.duplicate", "This supplier invoice number has already been received.", null));
            } catch (SQLException ex) {
                messages.add(red("invoice.duplicate.check", "Duplicate-invoice validation could not be completed.", null));
            }
        }

        BigDecimal itemSum = BigDecimal.ZERO;
        boolean completeItemAmounts = true;
        for (int index = 0; index < invoice.getItems().size(); index++) {
            ExtractedInvoiceItem item = invoice.getItems().get(index);
            Integer displayIndex = index + 1;
            if (blank(item.getDescription())) messages.add(red("item.description", "Item " + displayIndex + " needs a description.", index));
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0)
                messages.add(red("item.quantity", "Item " + displayIndex + " quantity must be greater than zero.", index));
            if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0)
                messages.add(red("item.unitPrice", "Item " + displayIndex + " unit price is missing or negative.", index));
            if (item.getAmount() == null || item.getAmount().signum() < 0) {
                messages.add(red("item.amount", "Item " + displayIndex + " line amount is missing or negative.", index));
                completeItemAmounts = false;
            } else {
                itemSum = itemSum.add(item.getAmount());
            }
            if (!item.isMatched()) messages.add(red("item.product", "Item " + displayIndex + " must be matched to a product.", index));
            if (item.getConfidence() < 0.80)
                messages.add(yellow("item.confidence", "Item " + displayIndex + " has low extraction confidence and needs review.", index));

            if (item.getQuantity() != null && item.getUnitPrice() != null && item.getAmount() != null) {
                BigDecimal expected = item.getQuantity().multiply(item.getUnitPrice())
                        .subtract(zero(item.getDiscount())).add(zero(item.getTax()));
                if (!approximately(expected, item.getAmount()))
                    messages.add(red("item.total", "Item " + displayIndex
                            + " amount does not agree with quantity, unit price, discount, and tax.", index));
            }
        }

        if (invoice.getSubtotal() == null) {
            messages.add(red("invoice.subtotal", "Invoice subtotal is missing.", null));
        } else if (completeItemAmounts && !approximately(itemSum, invoice.getSubtotal())) {
            messages.add(red("invoice.subtotal.mismatch", "Invoice subtotal does not equal the sum of line amounts.", null));
        }

        if (invoice.getTotal() == null) {
            messages.add(red("invoice.total", "Invoice total is missing.", null));
        } else if (invoice.getSubtotal() != null) {
            BigDecimal expected = invoice.getSubtotal().subtract(zero(invoice.getDiscount()))
                    .add(zero(invoice.getFreight())).add(zero(invoice.getMiscellaneousCharges()))
                    .add(zero(invoice.getTax()));
            if (!approximately(expected, invoice.getTotal()))
                messages.add(red("invoice.total.mismatch", "Invoice total does not agree with subtotal, charges, discount, and tax.", null));
        }

        if (invoice.getOverallConfidence() < 0.80)
            messages.add(yellow("invoice.confidence", "Some invoice header fields have low extraction confidence.", null));
        if (messages.stream().noneMatch(message -> message.severity() == ValidationMessage.Severity.RED))
            messages.add(new ValidationMessage(ValidationMessage.Severity.GREEN, "invoice.ready",
                    "All blocking validations passed; the invoice is ready to receive.", null));
        return new InvoiceValidationResult(messages);
    }

    private boolean approximately(BigDecimal expected, BigDecimal actual) {
        return expected.subtract(actual).abs().compareTo(tolerance) <= 0;
    }

    private static BigDecimal readTolerance(String value) {
        try {
            return value == null || value.isBlank() ? DEFAULT_TOLERANCE : new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return DEFAULT_TOLERANCE;
        }
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private ValidationMessage red(String code, String message, Integer item) {
        return new ValidationMessage(ValidationMessage.Severity.RED, code, message, item);
    }
    private ValidationMessage yellow(String code, String message, Integer item) {
        return new ValidationMessage(ValidationMessage.Severity.YELLOW, code, message, item);
    }
}
