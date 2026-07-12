package com.nexatek.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A supplier invoice row extracted from OCR, before it is saved as stock. */
public final class InvoiceLineDraft {

    private final String productCode;
    private final String description;
    private final String warehouse;
    private final BigDecimal quantity;
    private final BigDecimal unitCost;
    private final BigDecimal lineTotal;
    private final int confidence;
    private final List<String> warnings;

    public InvoiceLineDraft(String productCode, String description, BigDecimal quantity,
            BigDecimal unitCost, BigDecimal lineTotal, int confidence, List<String> warnings) {
        this(productCode, description, null, quantity, unitCost, lineTotal, confidence, warnings);
    }

    public InvoiceLineDraft(String productCode, String description, String warehouse, BigDecimal quantity,
            BigDecimal unitCost, BigDecimal lineTotal, int confidence, List<String> warnings) {
        this.productCode = clean(productCode);
        this.description = clean(description);
        this.warehouse = clean(warehouse);
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.lineTotal = lineTotal;
        this.confidence = Math.max(0, Math.min(100, confidence));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings == null ? List.of() : warnings));
    }

    public String getProductCode() {
        return productCode;
    }

    public String getDescription() {
        return description;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public int getConfidence() {
        return confidence;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public BigDecimal getCalculatedTotal() {
        if (quantity == null || unitCost == null) {
            return null;
        }
        return quantity.multiply(unitCost);
    }

    public boolean totalsMatch() {
        BigDecimal calculated = getCalculatedTotal();
        if (calculated == null || lineTotal == null) {
            return false;
        }
        BigDecimal tolerance = lineTotal.abs().multiply(new BigDecimal("0.001"));
        if (tolerance.compareTo(BigDecimal.ONE) < 0) {
            tolerance = BigDecimal.ONE;
        }
        return calculated.subtract(lineTotal).abs().compareTo(tolerance) <= 0;
    }

    public boolean hasRequiredValues() {
        return !description.isBlank()
                && quantity != null && quantity.signum() > 0
                && unitCost != null && unitCost.signum() > 0
                && lineTotal != null && lineTotal.signum() >= 0;
    }

    public boolean hasWholeQuantity() {
        return quantity != null && quantity.stripTrailingZeros().scale() <= 0;
    }

    public boolean requiresReview() {
        return confidence < 85 || !warnings.isEmpty() || !hasRequiredValues() || !totalsMatch();
    }

    public String getStatus() {
        if (!hasRequiredValues() || !hasWholeQuantity() || confidence < 60) {
            return "CHECK";
        }
        return requiresReview() ? "REVIEW" : "READY";
    }

    public String warningsText() {
        return String.join("; ", warnings);
    }

    public String quantityText() {
        return quantity == null ? "" : quantity.stripTrailingZeros().toPlainString();
    }

    public String moneyText(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
