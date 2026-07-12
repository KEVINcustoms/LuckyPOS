package com.nexatek.invoice;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Application-owned supplier invoice extracted for review before receiving stock. */
public final class ExtractedInvoice {

    private Integer supplierId;
    private String supplierName;
    private String supplierAddress;
    private String supplierVatRegistrationNumber;
    private String supplierTaxId;
    private String supplierTelephone;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private String customerTaxId;
    private String customerTelephone;
    private String shippingAddress;
    private String shippingInstructions;
    private String documentType;
    private String copyType;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String salesOrderNumber;
    private LocalDate orderDate;
    private LocalDate shipDate;
    private String salesperson;
    private String purchaseOrderNumber;
    private String fiscalDocumentNumber;
    private String verificationCode;
    private String preparedBy;
    private String printedAt;
    private String checkedBy;
    private String receivedByName;
    private LocalDate receivedDate;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal freight;
    private BigDecimal miscellaneousCharges;
    private BigDecimal tax;
    private BigDecimal total;
    private String currency;
    private double overallConfidence;
    private String extractionMethod;
    private String rawExtractedText;
    private Path originalDocumentPath;
    private Instant extractionTimestamp = Instant.now();
    private final List<ExtractedInvoiceItem> items = new ArrayList<>();
    private final Map<String, Double> fieldConfidences = new LinkedHashMap<>();
    private final List<String> extractionWarnings = new ArrayList<>();
    private final Set<String> manuallyCorrectedFields = new LinkedHashSet<>();

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer value) { supplierId = value; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String value) { supplierName = clean(value); }
    public String getSupplierAddress() { return supplierAddress; }
    public void setSupplierAddress(String value) { supplierAddress = clean(value); }
    public String getSupplierVatRegistrationNumber() { return supplierVatRegistrationNumber; }
    public void setSupplierVatRegistrationNumber(String value) { supplierVatRegistrationNumber = clean(value); }
    public String getSupplierTaxId() { return supplierTaxId; }
    public void setSupplierTaxId(String value) { supplierTaxId = clean(value); }
    public String getSupplierTelephone() { return supplierTelephone; }
    public void setSupplierTelephone(String value) { supplierTelephone = clean(value); }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String value) { customerCode = clean(value); }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { customerName = clean(value); }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String value) { customerAddress = clean(value); }
    public String getCustomerTaxId() { return customerTaxId; }
    public void setCustomerTaxId(String value) { customerTaxId = clean(value); }
    public String getCustomerTelephone() { return customerTelephone; }
    public void setCustomerTelephone(String value) { customerTelephone = clean(value); }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String value) { shippingAddress = clean(value); }
    public String getShippingInstructions() { return shippingInstructions; }
    public void setShippingInstructions(String value) { shippingInstructions = clean(value); }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String value) { documentType = clean(value); }
    public String getCopyType() { return copyType; }
    public void setCopyType(String value) { copyType = clean(value); }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String value) { invoiceNumber = clean(value); }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate value) { invoiceDate = value; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate value) { dueDate = value; }
    public String getSalesOrderNumber() { return salesOrderNumber; }
    public void setSalesOrderNumber(String value) { salesOrderNumber = clean(value); }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate value) { orderDate = value; }
    public LocalDate getShipDate() { return shipDate; }
    public void setShipDate(LocalDate value) { shipDate = value; }
    public String getSalesperson() { return salesperson; }
    public void setSalesperson(String value) { salesperson = clean(value); }
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String value) { purchaseOrderNumber = clean(value); }
    public String getFiscalDocumentNumber() { return fiscalDocumentNumber; }
    public void setFiscalDocumentNumber(String value) { fiscalDocumentNumber = clean(value); }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String value) { verificationCode = clean(value); }
    public String getPreparedBy() { return preparedBy; }
    public void setPreparedBy(String value) { preparedBy = clean(value); }
    public String getPrintedAt() { return printedAt; }
    public void setPrintedAt(String value) { printedAt = clean(value); }
    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String value) { checkedBy = clean(value); }
    public String getReceivedByName() { return receivedByName; }
    public void setReceivedByName(String value) { receivedByName = clean(value); }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate value) { receivedDate = value; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal value) { subtotal = value; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal value) { discount = value; }
    public BigDecimal getFreight() { return freight; }
    public void setFreight(BigDecimal value) { freight = value; }
    public BigDecimal getMiscellaneousCharges() { return miscellaneousCharges; }
    public void setMiscellaneousCharges(BigDecimal value) { miscellaneousCharges = value; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal value) { tax = value; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal value) { total = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { currency = clean(value); }
    public double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(double value) { overallConfidence = Math.max(0.0, Math.min(1.0, value)); }
    public String getExtractionMethod() { return extractionMethod; }
    public void setExtractionMethod(String value) { extractionMethod = clean(value); }
    public String getRawExtractedText() { return rawExtractedText; }
    public void setRawExtractedText(String value) { rawExtractedText = value == null ? null : value.trim(); }
    public List<String> getExtractionWarnings() { return List.copyOf(extractionWarnings); }
    public void setExtractionWarnings(List<String> values) {
        extractionWarnings.clear();
        if (values != null) values.stream().filter(value -> value != null && !value.isBlank())
                .map(String::trim).forEach(extractionWarnings::add);
    }
    public Path getOriginalDocumentPath() { return originalDocumentPath; }
    public void setOriginalDocumentPath(Path value) { originalDocumentPath = value; }
    public Instant getExtractionTimestamp() { return extractionTimestamp; }
    public void setExtractionTimestamp(Instant value) { extractionTimestamp = value == null ? Instant.now() : value; }
    public List<ExtractedInvoiceItem> getItems() { return items; }
    public Map<String, Double> getFieldConfidences() { return Map.copyOf(fieldConfidences); }
    public void setFieldConfidence(String field, Double confidence) {
        if (field != null && confidence != null) fieldConfidences.put(field, confidence);
    }
    public Set<String> getManuallyCorrectedFields() { return Set.copyOf(manuallyCorrectedFields); }
    public void markCorrected(String field) {
        if (field != null && !field.isBlank()) manuallyCorrectedFields.add(field);
    }

    private String clean(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
