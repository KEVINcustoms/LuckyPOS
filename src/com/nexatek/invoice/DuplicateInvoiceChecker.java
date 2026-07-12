package com.nexatek.invoice;

import java.sql.SQLException;

@FunctionalInterface
public interface DuplicateInvoiceChecker {
    boolean exists(int supplierId, String invoiceNumber) throws SQLException;
}
