package com.nexatek.invoice;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

/** Persists an approved invoice and receives stock atomically. */
public final class InvoiceImportRepository implements DuplicateInvoiceChecker {

    private final Connection connection;
    private final Path documentDirectory;

    public InvoiceImportRepository(Connection connection) {
        this(connection, Path.of(System.getProperty("luckypos.invoice.storage", "invoice-documents")));
    }

    InvoiceImportRepository(Connection connection, Path documentDirectory) {
        this.connection = connection;
        this.documentDirectory = documentDirectory;
    }

    @Override
    public boolean exists(int supplierId, String invoiceNumber) throws SQLException {
        String sql = "select 1 from supplier_purchase_invoices where supplier_id=? "
                + "and lower(trim(invoice_number))=lower(trim(?)) limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            statement.setString(2, invoiceNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public long receive(ExtractedInvoice invoice, String approvedBy) throws SQLException, IOException {
        requireApprovedInvoice(invoice);
        StoredDocument stored = storeOriginal(invoice.getOriginalDocumentPath());
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            long invoiceId = insertInvoice(invoice, approvedBy, stored);
            for (int index = 0; index < invoice.getItems().size(); index++) {
                ExtractedInvoiceItem item = invoice.getItems().get(index);
                insertItem(invoiceId, index + 1, item);
                if (item.isSaveSupplierMapping() && item.getSupplierProductCode() != null
                        && !item.getSupplierProductCode().isBlank()) {
                    saveMapping(invoice.getSupplierId(), item);
                }
                updateStock(invoiceId, item, approvedBy);
            }
            connection.commit();
            return invoiceId;
        } catch (SQLException | RuntimeException ex) {
            rollbackQuietly();
            if (stored.created()) {
                try {
                    Files.deleteIfExists(stored.path());
                } catch (IOException cleanupFailure) {
                    ex.addSuppressed(cleanupFailure);
                }
            }
            throw ex;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private long insertInvoice(ExtractedInvoice invoice, String approvedBy, StoredDocument stored) throws SQLException {
        String sql = "insert into supplier_purchase_invoices (supplier_id,supplier_name,supplier_address,"
                + "supplier_vat_registration,supplier_tax_id,supplier_phone,document_type,copy_type,invoice_number,"
                + "invoice_date,due_date,sales_order_number,order_date,ship_date,salesperson,purchase_order_number,"
                + "customer_code,customer_name,customer_address,customer_tax_id,customer_phone,shipping_address,"
                + "shipping_instructions,fiscal_document_number,verification_code,prepared_by,printed_at,checked_by,"
                + "received_by_name,received_date,subtotal,discount,freight,miscellaneous_charges,tax,total,currency,"
                + "extraction_confidence,extraction_timestamp,approved_by,original_document_path,source_sha256,corrected_fields) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, invoice.getSupplierId());
            statement.setString(2, invoice.getSupplierName());
            statement.setString(3, invoice.getSupplierAddress());
            statement.setString(4, invoice.getSupplierVatRegistrationNumber());
            statement.setString(5, invoice.getSupplierTaxId());
            statement.setString(6, invoice.getSupplierTelephone());
            statement.setString(7, invoice.getDocumentType());
            statement.setString(8, invoice.getCopyType());
            statement.setString(9, invoice.getInvoiceNumber());
            statement.setDate(10, Date.valueOf(invoice.getInvoiceDate()));
            setDate(statement, 11, invoice.getDueDate());
            statement.setString(12, invoice.getSalesOrderNumber());
            setDate(statement, 13, invoice.getOrderDate());
            setDate(statement, 14, invoice.getShipDate());
            statement.setString(15, invoice.getSalesperson());
            statement.setString(16, invoice.getPurchaseOrderNumber());
            statement.setString(17, invoice.getCustomerCode());
            statement.setString(18, invoice.getCustomerName());
            statement.setString(19, invoice.getCustomerAddress());
            statement.setString(20, invoice.getCustomerTaxId());
            statement.setString(21, invoice.getCustomerTelephone());
            statement.setString(22, invoice.getShippingAddress());
            statement.setString(23, invoice.getShippingInstructions());
            statement.setString(24, invoice.getFiscalDocumentNumber());
            statement.setString(25, invoice.getVerificationCode());
            statement.setString(26, invoice.getPreparedBy());
            statement.setString(27, invoice.getPrintedAt());
            statement.setString(28, invoice.getCheckedBy());
            statement.setString(29, invoice.getReceivedByName());
            setDate(statement, 30, invoice.getReceivedDate());
            setDecimal(statement, 31, invoice.getSubtotal());
            setDecimal(statement, 32, invoice.getDiscount());
            setDecimal(statement, 33, invoice.getFreight());
            setDecimal(statement, 34, invoice.getMiscellaneousCharges());
            setDecimal(statement, 35, invoice.getTax());
            setDecimal(statement, 36, invoice.getTotal());
            statement.setString(37, invoice.getCurrency());
            statement.setBigDecimal(38, BigDecimal.valueOf(invoice.getOverallConfidence()));
            statement.setTimestamp(39, Timestamp.from(invoice.getExtractionTimestamp()));
            statement.setString(40, approvedBy);
            statement.setString(41, stored.path().toAbsolutePath().normalize().toString());
            statement.setString(42, stored.sha256());
            statement.setString(43, String.join(",", invoice.getManuallyCorrectedFields()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("The supplier invoice ID was not generated.");
                return keys.getLong(1);
            }
        }
    }

    private void insertItem(long invoiceId, int line, ExtractedInvoiceItem item) throws SQLException {
        String sql = "insert into supplier_purchase_invoice_items (invoice_id,line_number,supplier_product_code,"
                + "supplier_description,warehouse,quantity,unit,unit_price,line_tax,line_discount,line_amount,"
                + "extraction_confidence,internal_product_id,matching_status,corrected_fields) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            statement.setInt(2, line);
            statement.setString(3, item.getSupplierProductCode());
            statement.setString(4, item.getDescription());
            statement.setString(5, item.getWarehouse());
            statement.setBigDecimal(6, item.getQuantity());
            statement.setString(7, item.getUnit());
            statement.setBigDecimal(8, item.getUnitPrice());
            setDecimal(statement, 9, item.getTax());
            setDecimal(statement, 10, item.getDiscount());
            statement.setBigDecimal(11, item.getAmount());
            statement.setBigDecimal(12, BigDecimal.valueOf(item.getConfidence()));
            statement.setInt(13, item.getMatchedProductId());
            statement.setString(14, item.getMatchingStatus().name());
            statement.setString(15, String.join(",", item.getManuallyCorrectedFields()));
            statement.executeUpdate();
        }
    }

    private void saveMapping(int supplierId, ExtractedInvoiceItem item) throws SQLException {
        String insert = "insert into supplier_product_mappings (supplier_id,supplier_product_code,supplier_description,internal_product_id) "
                + "values (?,?,?,?) on conflict do nothing";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setInt(1, supplierId);
            statement.setString(2, item.getSupplierProductCode());
            statement.setString(3, item.getDescription());
            statement.setInt(4, item.getMatchedProductId());
            statement.executeUpdate();
        }
        String update = "update supplier_product_mappings set supplier_description=?,internal_product_id=?,last_used_at=current_timestamp "
                + "where supplier_id=? and lower(supplier_product_code)=lower(?)";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setString(1, item.getDescription());
            statement.setInt(2, item.getMatchedProductId());
            statement.setInt(3, supplierId);
            statement.setString(4, item.getSupplierProductCode());
            statement.executeUpdate();
        }
    }

    private void updateStock(long invoiceId, ExtractedInvoiceItem item, String approvedBy) throws SQLException {
        String update = "update products set quantity=coalesce(quantity,0)+?, cost_price=? where productid=?";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setBigDecimal(1, item.getQuantity());
            statement.setBigDecimal(2, item.getUnitPrice());
            statement.setInt(3, item.getMatchedProductId());
            if (statement.executeUpdate() != 1) throw new SQLException("A matched product no longer exists.");
        }
        String movement = "insert into stock_movements (product_id,movement_type,quantity,unit_cost,reference_type,reference_id,approved_by) "
                + "values (?,'SUPPLIER_INVOICE',?,?,'SUPPLIER_PURCHASE_INVOICE',?,?)";
        try (PreparedStatement statement = connection.prepareStatement(movement)) {
            statement.setInt(1, item.getMatchedProductId());
            statement.setBigDecimal(2, item.getQuantity());
            statement.setBigDecimal(3, item.getUnitPrice());
            statement.setLong(4, invoiceId);
            statement.setString(5, approvedBy);
            statement.executeUpdate();
        }
    }

    private StoredDocument storeOriginal(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) throw new IOException("The original invoice file is unavailable.");
        String sha256 = sha256(source);
        LocalDate today = LocalDate.now();
        Path folder = documentDirectory.resolve(String.valueOf(today.getYear()))
                .resolve(String.format(Locale.ROOT, "%02d", today.getMonthValue()));
        Files.createDirectories(folder);
        String filename = source.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
        Path destination = folder.resolve(sha256.substring(0, 16) + "-" + filename);
        boolean created = !Files.exists(destination);
        if (created) Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
        return new StoredDocument(destination, sha256, created);
    }

    private String sha256(Path source) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(source); DigestInputStream hashing = new DigestInputStream(input, digest)) {
                hashing.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private void requireApprovedInvoice(ExtractedInvoice invoice) {
        if (invoice == null || invoice.getSupplierId() == null || invoice.getInvoiceNumber() == null
                || invoice.getInvoiceDate() == null || invoice.getItems().isEmpty())
            throw new IllegalArgumentException("The invoice has not passed approval validation.");
        for (ExtractedInvoiceItem item : invoice.getItems()) {
            if (!item.isMatched() || item.getQuantity() == null || item.getUnitPrice() == null || item.getAmount() == null)
                throw new IllegalArgumentException("Every invoice item must be valid and matched before receiving stock.");
        }
    }

    private void setDecimal(PreparedStatement statement, int index, BigDecimal value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.NUMERIC); else statement.setBigDecimal(index, value);
    }
    private void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.DATE); else statement.setDate(index, Date.valueOf(value));
    }
    private void rollbackQuietly() {
        try { connection.rollback(); } catch (SQLException ignored) { }
    }

    private record StoredDocument(Path path, String sha256, boolean created) { }
}
