package com.nexatek.invoice;

import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzedDocument;
import com.azure.ai.documentintelligence.models.CurrencyValue;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.ai.documentintelligence.models.DocumentFieldType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Keeps Azure response types at the extraction boundary and maps them into application DTOs. */
final class AzureInvoiceResultMapper {

    ExtractedInvoice map(AnalyzeResult result) throws InvoiceExtractionException {
        if (result == null) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.SERVICE,
                    "Azure completed the analysis but returned no invoice result.");
        }
        if (result.getDocuments() == null || result.getDocuments().isEmpty()) {
            String text = blankToNull(result.getContent());
            if (text == null || text.isBlank()) {
                throw new InvoiceExtractionException(InvoiceExtractionException.Reason.SERVICE,
                        "Azure completed the analysis but returned no invoice data for this file. Try a different invoice image or use Offline OCR.");
            }
            ExtractedInvoice invoice = new ExtractedInvoice();
            invoice.setRawExtractedText(text);
            invoice.setExtractionMethod("Azure Document Intelligence - prebuilt-invoice");
            invoice.setOriginalDocumentPath(null);
            return invoice;
        }
        AnalyzedDocument document = result.getDocuments().get(0);
        Map<String, DocumentField> fields = document.getFields() == null ? Map.of() : document.getFields();

        ExtractedInvoice invoice = new ExtractedInvoice();
        invoice.setOverallConfidence(document.getConfidence());
        invoice.setSupplierName(text(fields, "VendorName", "SupplierName"));
        invoice.setSupplierAddress(text(fields, "VendorAddress", "SupplierAddress"));
        invoice.setSupplierVatRegistrationNumber(text(fields, "VendorRegistrationId", "VendorVatId"));
        invoice.setSupplierTaxId(text(fields, "VendorTaxId", "SupplierTaxId"));
        invoice.setSupplierTelephone(text(fields, "VendorPhoneNumber", "SupplierPhoneNumber"));
        invoice.setCustomerCode(text(fields, "CustomerId", "CustomerCode"));
        invoice.setCustomerName(text(fields, "CustomerName"));
        invoice.setCustomerAddress(text(fields, "CustomerAddress", "BillingAddress"));
        invoice.setCustomerTaxId(text(fields, "CustomerTaxId"));
        invoice.setCustomerTelephone(text(fields, "CustomerPhoneNumber"));
        invoice.setShippingAddress(text(fields, "ShippingAddress", "ServiceAddress"));
        invoice.setShippingInstructions(text(fields, "ShippingInstructions", "DeliveryInstructions"));
        invoice.setDocumentType(text(fields, "DocumentType"));
        invoice.setInvoiceNumber(text(fields, "InvoiceId", "InvoiceNumber"));
        invoice.setInvoiceDate(date(fields, "InvoiceDate"));
        invoice.setDueDate(date(fields, "DueDate"));
        invoice.setSalesOrderNumber(text(fields, "SalesOrder", "SalesOrderNumber"));
        invoice.setOrderDate(date(fields, "OrderDate"));
        invoice.setShipDate(date(fields, "ShipDate", "DeliveryDate"));
        invoice.setSalesperson(text(fields, "SalesPerson", "Salesperson"));
        invoice.setPurchaseOrderNumber(text(fields, "PurchaseOrder", "PurchaseOrderNumber"));
        invoice.setSubtotal(amount(fields, "SubTotal", "Subtotal"));
        invoice.setDiscount(amount(fields, "TotalDiscount", "Discount"));
        invoice.setFreight(amount(fields, "ShippingCost", "Freight"));
        invoice.setMiscellaneousCharges(amount(fields, "MiscellaneousCharges", "OtherCharges"));
        invoice.setTax(amount(fields, "TotalTax", "Tax"));
        invoice.setTotal(amount(fields, "InvoiceTotal", "Total"));
        invoice.setCurrency(currency(fields));

        for (String name : fields.keySet()) {
            DocumentField field = fields.get(name);
            if (field != null) {
                invoice.setFieldConfidence(name, field.getConfidence());
            }
        }
        mapItems(fields.get("Items"), invoice.getItems());
        invoice.setRawExtractedText(result.getContent());
        // The prebuilt model supplies standard fields; this conservative pass adds labelled
        // supplier-specific values such as FDN, sales order, warehouse and verification code.
        new OfflineInvoiceMetadataParser().populate(invoice, result.getContent());
        return invoice;
    }

    private void mapItems(DocumentField itemsField, List<ExtractedInvoiceItem> destination) {
        if (itemsField == null || itemsField.getValueList() == null) {
            return;
        }
        for (DocumentField itemField : itemsField.getValueList()) {
            if (itemField == null || itemField.getValueMap() == null) {
                continue;
            }
            Map<String, DocumentField> values = itemField.getValueMap();
            ExtractedInvoiceItem item = new ExtractedInvoiceItem();
            item.setSupplierProductCode(text(values, "ProductCode", "ItemCode", "Code"));
            item.setDescription(text(values, "Description", "Name"));
            item.setWarehouse(text(values, "Warehouse", "Store", "Location"));
            item.setQuantity(amount(values, "Quantity"));
            item.setUnit(text(values, "Unit", "UnitOfMeasure"));
            item.setUnitPrice(amount(values, "UnitPrice", "UnitCost"));
            item.setTax(amount(values, "Tax", "TaxAmount"));
            item.setDiscount(amount(values, "Discount", "DiscountAmount"));
            item.setAmount(amount(values, "Amount", "TotalPrice", "LineTotal"));
            item.setConfidence(averageConfidence(values));
            destination.add(item);
        }
    }

    private String text(Map<String, DocumentField> fields, String... names) {
        DocumentField field = first(fields, names);
        if (field == null) {
            return null;
        }
        if (DocumentFieldType.STRING.equals(field.getType())) return field.getValueString();
        if (DocumentFieldType.PHONE_NUMBER.equals(field.getType())) return field.getValuePhoneNumber();
        if (DocumentFieldType.COUNTRY_REGION.equals(field.getType())) return field.getValueCountryRegion();
        if (DocumentFieldType.NUMBER.equals(field.getType()) && field.getValueNumber() != null)
            return BigDecimal.valueOf(field.getValueNumber()).stripTrailingZeros().toPlainString();
        if (DocumentFieldType.INTEGER.equals(field.getType()) && field.getValueInteger() != null)
            return String.valueOf(field.getValueInteger());
        return blankToNull(field.getContent());
    }

    private LocalDate date(Map<String, DocumentField> fields, String... names) {
        DocumentField field = first(fields, names);
        if (field == null) return null;
        if (field.getValueDate() != null) return field.getValueDate();
        try {
            String value = blankToNull(field.getContent());
            return value == null ? null : LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal amount(Map<String, DocumentField> fields, String... names) {
        DocumentField field = first(fields, names);
        if (field == null) return null;
        if (field.getValueCurrency() != null) {
            return BigDecimal.valueOf(field.getValueCurrency().getAmount());
        }
        if (field.getValueNumber() != null) return BigDecimal.valueOf(field.getValueNumber());
        if (field.getValueInteger() != null) return BigDecimal.valueOf(field.getValueInteger());
        return parseAmount(field.getContent());
    }

    private BigDecimal parseAmount(String source) {
        if (source == null || source.isBlank()) return null;
        String value = source.replaceAll("[^0-9,.-]", "");
        int comma = value.lastIndexOf(',');
        int dot = value.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            if (comma > dot) value = value.replace(".", "").replace(',', '.');
            else value = value.replace(",", "");
        } else if (comma >= 0) {
            int trailing = value.length() - comma - 1;
            value = trailing > 0 && trailing <= 2 ? value.replace(',', '.') : value.replace(",", "");
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String currency(Map<String, DocumentField> fields) {
        for (String name : List.of("InvoiceTotal", "SubTotal", "TotalTax")) {
            DocumentField field = fields.get(name);
            CurrencyValue value = field == null ? null : field.getValueCurrency();
            if (value != null) {
                if (value.getCurrencyCode() != null && !value.getCurrencyCode().isBlank()) return value.getCurrencyCode();
                if (value.getCurrencySymbol() != null && !value.getCurrencySymbol().isBlank()) return value.getCurrencySymbol();
            }
        }
        return text(fields, "Currency", "CurrencyCode");
    }

    private double averageConfidence(Map<String, DocumentField> fields) {
        List<Double> values = new ArrayList<>();
        for (DocumentField field : fields.values()) {
            if (field != null && field.getConfidence() != null) values.add(field.getConfidence());
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private DocumentField first(Map<String, DocumentField> fields, String... names) {
        for (String name : names) {
            DocumentField value = fields.get(name);
            if (value != null) return value;
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
