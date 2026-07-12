package com.nexatek.invoice;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/** Application-owned invoice item; no Azure SDK types escape into the UI or database layers. */
public final class ExtractedInvoiceItem {

    public enum MatchingStatus {
        UNMATCHED, EXACT_CODE, SAVED_MAPPING, EXACT_BARCODE, EXACT_NAME, SUGGESTED_NAME, MANUAL
    }

    private String supplierProductCode;
    private String description;
    private String warehouse;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal amount;
    private double confidence;
    private Integer matchedProductId;
    private String matchedProductName;
    private MatchingStatus matchingStatus = MatchingStatus.UNMATCHED;
    private boolean saveSupplierMapping;
    private final Set<String> manuallyCorrectedFields = new LinkedHashSet<>();

    public String getSupplierProductCode() { return supplierProductCode; }
    public void setSupplierProductCode(String value) { supplierProductCode = clean(value); }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = clean(value); }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String value) { warehouse = clean(value); }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { unit = clean(value); }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal value) { tax = value; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal value) { discount = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double value) { confidence = Math.max(0.0, Math.min(1.0, value)); }
    public Integer getMatchedProductId() { return matchedProductId; }
    public void setMatchedProductId(Integer value) { matchedProductId = value; }
    public String getMatchedProductName() { return matchedProductName; }
    public void setMatchedProductName(String value) { matchedProductName = clean(value); }
    public MatchingStatus getMatchingStatus() { return matchingStatus; }
    public void setMatchingStatus(MatchingStatus value) {
        matchingStatus = value == null ? MatchingStatus.UNMATCHED : value;
    }
    public boolean isSaveSupplierMapping() { return saveSupplierMapping; }
    public void setSaveSupplierMapping(boolean value) { saveSupplierMapping = value; }
    public Set<String> getManuallyCorrectedFields() { return Set.copyOf(manuallyCorrectedFields); }
    public void markCorrected(String field) {
        if (field != null && !field.isBlank()) manuallyCorrectedFields.add(field);
    }

    public boolean isMatched() {
        return matchedProductId != null && matchingStatus != MatchingStatus.UNMATCHED
                && matchingStatus != MatchingStatus.SUGGESTED_NAME;
    }

    private String clean(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
