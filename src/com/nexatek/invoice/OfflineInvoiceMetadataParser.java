package com.nexatek.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative label-based metadata extraction for the local Tesseract fallback. */
final class OfflineInvoiceMetadataParser {

    private static final Pattern IDENTIFIER = Pattern.compile("(?i)(?=[A-Z0-9./-]*\\d)[A-Z0-9][A-Z0-9./-]{2,}");
    private static final Pattern DATE = Pattern.compile("(?<!\\d)(\\d{1,4}[-/.]\\d{1,2}[-/.]\\d{1,4})(?!\\d)");
    private static final Pattern MONEY = Pattern.compile("(?<![A-Za-z0-9])[-(]?[0-9Oo][0-9OoIl|,.' ]*(?:[.,][0-9OoIl|]{1,3})?\\)?");
    private static final Pattern CURRENCY = Pattern.compile("(?i)\\b(UGX|USH|USD|EUR|GBP|KES|TZS|RWF)\\b");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("d.M.uuuu"), DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("uuuu/M/d"));

    void populate(ExtractedInvoice invoice, String rawText) {
        List<String> lines = cleanLines(rawText);
        if (invoice.getSupplierName() == null) invoice.setSupplierName(findSupplier(lines));
        if (invoice.getSupplierAddress() == null) invoice.setSupplierAddress(findSupplierAddress(lines));
        if (invoice.getDocumentType() == null && contains(lines, "tax invoice")) invoice.setDocumentType("TAX INVOICE");
        if (invoice.getCopyType() == null && contains(lines, "customer copy")) invoice.setCopyType("CUSTOMER COPY");
        if (invoice.getInvoiceNumber() == null)
            invoice.setInvoiceNumber(findIdentifier(lines, "invoice number", "invoice no", "tax invoice", "invoice #"));
        if (invoice.getSalesOrderNumber() == null)
            invoice.setSalesOrderNumber(findIdentifier(lines, "sales order", "sales order no"));
        if (invoice.getPurchaseOrderNumber() == null)
            invoice.setPurchaseOrderNumber(findText(lines, "customer purchase order no", "purchase order", "po no", "p.o. no"));
        if (invoice.getInvoiceDate() == null) invoice.setInvoiceDate(findDate(lines, "invoice date"));
        if (invoice.getDueDate() == null) invoice.setDueDate(findDate(lines, "due date"));
        if (invoice.getOrderDate() == null) invoice.setOrderDate(findDate(lines, "order date"));
        if (invoice.getShipDate() == null) invoice.setShipDate(findDate(lines, "ship date"));
        if (invoice.getSubtotal() == null) invoice.setSubtotal(findMoney(lines, "total gross amount", "subtotal", "sub total"));
        if (invoice.getDiscount() == null) invoice.setDiscount(findMoney(lines, "total discount", "discount"));
        if (invoice.getFreight() == null) invoice.setFreight(findMoney(lines, "total freight", "freight"));
        if (invoice.getMiscellaneousCharges() == null)
            invoice.setMiscellaneousCharges(findMoney(lines, "misc charges", "miscellaneous charges"));
        if (invoice.getTax() == null) invoice.setTax(findMoney(lines, "total tax", "vat amount", "tax amount"));
        if (invoice.getTotal() == null) invoice.setTotal(findMoney(lines, "total invoice", "invoice total", "grand total", "amount due"));
        if (invoice.getSupplierVatRegistrationNumber() == null)
            invoice.setSupplierVatRegistrationNumber(findIdentifier(lines, "reg vat no", "vat registration", "vendor vat"));
        if (invoice.getSupplierTaxId() == null)
            invoice.setSupplierTaxId(findIdentifier(lines, "tax tin", "tax id", "vendor tin", "tin"));
        if (invoice.getSupplierTelephone() == null)
            invoice.setSupplierTelephone(findText(lines, "vendor telephone", "supplier telephone", "tel"));
        if (invoice.getCustomerCode() == null)
            invoice.setCustomerCode(findIdentifier(lines, "customer code", "customer id", "customer"));
        if (invoice.getCustomerName() == null) invoice.setCustomerName(findText(lines, "customer name"));
        if (invoice.getCustomerAddress() == null) invoice.setCustomerAddress(findText(lines, "customer address"));
        if (invoice.getCustomerTaxId() == null)
            invoice.setCustomerTaxId(findIdentifier(lines, "customer tin", "customer tax id", "tin no"));
        if (invoice.getCustomerTelephone() == null)
            invoice.setCustomerTelephone(findText(lines, "customer telephone", "telephone"));
        if (invoice.getShippingAddress() == null) invoice.setShippingAddress(findText(lines, "shipping address"));
        if (invoice.getShippingInstructions() == null) invoice.setShippingInstructions(findText(lines, "shipping instructions"));
        if (invoice.getSalesperson() == null) invoice.setSalesperson(findText(lines, "salesperson"));
        if (invoice.getFiscalDocumentNumber() == null) invoice.setFiscalDocumentNumber(findIdentifier(lines, "fdn"));
        if (invoice.getVerificationCode() == null) invoice.setVerificationCode(findIdentifier(lines, "verification code"));
        if (invoice.getPreparedBy() == null) invoice.setPreparedBy(findText(lines, "prepared by"));
        if (invoice.getPrintedAt() == null) invoice.setPrintedAt(findText(lines, "printed on", "printed at"));
        if (invoice.getCheckedBy() == null) invoice.setCheckedBy(findText(lines, "checked by"));
        if (invoice.getReceivedByName() == null) invoice.setReceivedByName(findText(lines, "received by (name)", "received by name"));
        if (invoice.getReceivedDate() == null) invoice.setReceivedDate(findDate(lines, "received date"));
        if (invoice.getCurrency() == null) invoice.setCurrency(findCurrency(lines));
    }

    private boolean contains(List<String> lines, String value) {
        return lines.stream().anyMatch(line -> line.toLowerCase(Locale.ROOT).contains(value));
    }

    private List<String> cleanLines(String rawText) {
        List<String> lines = new ArrayList<>();
        for (String line : (rawText == null ? "" : rawText).split("\\R")) {
            String cleaned = line.replace('|', ' ').replaceAll("\\s+", " ").trim();
            if (!cleaned.isBlank() && !cleaned.matches("--- Page \\d+ ---")) lines.add(cleaned);
        }
        return lines;
    }

    private String findSupplier(List<String> lines) {
        int limit = Math.min(lines.size(), 25);
        for (int index = 0; index < limit; index++) {
            String line = lines.get(index);
            String lower = line.toLowerCase(Locale.ROOT);
            if ((lower.matches(".*\\b(limited|ltd|inc|llc|company|corporation)\\.?$")
                    || lower.matches(".*\\b(limited|ltd|inc|llc)\\b.*"))
                    && !lower.contains("customer") && !lower.contains("bill to")) {
                return line.replaceAll("(?i)^(vendor|supplier)\\s*[:;-]?\\s*", "").trim();
            }
        }
        return null;
    }

    private String findSupplierAddress(List<String> lines) {
        String supplier = findSupplier(lines);
        if (supplier == null) return null;
        for (int index = 0; index < Math.min(lines.size(), 25); index++) {
            if (!lines.get(index).equalsIgnoreCase(supplier)) continue;
            StringBuilder address = new StringBuilder();
            for (int next = index + 1; next < Math.min(lines.size(), index + 5); next++) {
                String line = lines.get(next);
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.contains("vat") || lower.contains("tin") || lower.contains("tax invoice")
                        || lower.contains("customer copy")) break;
                int telephone = lower.indexOf("tel");
                if (telephone >= 0) line = line.substring(0, telephone).replaceAll("[,; ]+$", "");
                if (!line.isBlank()) {
                    if (address.length() > 0) address.append(", ");
                    address.append(line);
                }
            }
            return address.isEmpty() ? null : address.toString();
        }
        return null;
    }

    private String findIdentifier(List<String> lines, String... labels) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                int at = lower.indexOf(label);
                if (at < 0) continue;
                String after = line.substring(Math.min(line.length(), at + label.length()))
                        .replaceFirst("^[\\s:#=-]+", "");
                String found = firstIdentifier(after);
                if (found != null) return found;
                if (index + 1 < lines.size()) {
                    found = firstIdentifier(lines.get(index + 1));
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    private String firstIdentifier(String value) {
        Matcher matcher = IDENTIFIER.matcher(value == null ? "" : value);
        return matcher.find() ? matcher.group() : null;
    }

    private String findText(List<String> lines, String... labels) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                int at = lower.indexOf(label);
                if (at < 0) continue;
                String after = line.substring(Math.min(line.length(), at + label.length()))
                        .replaceFirst("^[\\s:#=-]+", "").trim();
                if (!after.isBlank() && !looksLikeAnotherLabel(after)) return after;
                if (index + 1 < lines.size() && !looksLikeAnotherLabel(lines.get(index + 1)))
                    return lines.get(index + 1);
            }
        }
        return null;
    }

    private boolean looksLikeAnotherLabel(String value) {
        String lower = value.toLowerCase(Locale.ROOT).trim();
        return lower.isBlank() || lower.matches("(invoice|order|ship|customer|salesperson|telephone|tin|tax|total|warehouse|description).*[:]?$");
    }

    private LocalDate findDate(List<String> lines, String label) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!line.toLowerCase(Locale.ROOT).contains(label)) continue;
            LocalDate parsed = parseDate(line);
            if (parsed != null) return parsed;
            if (index + 1 < lines.size() && (parsed = parseDate(lines.get(index + 1))) != null) return parsed;
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        Matcher matcher = DATE.matcher(value == null ? "" : value);
        if (!matcher.find()) return null;
        String date = matcher.group(1);
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(date, format);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal findMoney(List<String> lines, String... labels) {
        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            for (String label : labels) {
                if (!lower.contains(label)) continue;
                BigDecimal amount = lastMoney(line.substring(lower.indexOf(label) + label.length()));
                if (amount != null) return amount;
            }
        }
        return null;
    }

    private BigDecimal lastMoney(String value) {
        Matcher matcher = MONEY.matcher(value == null ? "" : value);
        String found = null;
        while (matcher.find()) found = matcher.group();
        if (found == null) return null;
        boolean negative = found.startsWith("(") && found.endsWith(")");
        String cleaned = found.replace('O', '0').replace('o', '0').replace('I', '1')
                .replace('l', '1').replace('|', '1').replace("'", "").replace(" ", "")
                .replace("(", "").replace(")", "");
        int comma = cleaned.lastIndexOf(',');
        int dot = cleaned.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            char decimal = comma > dot ? ',' : '.';
            cleaned = decimal == ',' ? cleaned.replace(".", "").replace(',', '.') : cleaned.replace(",", "");
        } else if (comma >= 0) {
            int digits = cleaned.length() - comma - 1;
            cleaned = digits > 0 && digits <= 2 ? cleaned.replace(',', '.') : cleaned.replace(",", "");
        } else if (dot >= 0 && cleaned.length() - dot - 1 > 2) {
            cleaned = cleaned.replace(".", "");
        }
        try {
            BigDecimal amount = new BigDecimal(cleaned);
            return negative ? amount.negate() : amount;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String findCurrency(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = CURRENCY.matcher(line);
            if (matcher.find()) return "USH".equalsIgnoreCase(matcher.group(1)) ? "UGX" : matcher.group(1).toUpperCase(Locale.ROOT);
        }
        return null;
    }
}
