package com.nexatek.invoice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductMatchingServiceTest {

    @Test
    void matchesExactSupplierCodeThenSavedMappingAndExactName() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:matching;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("create table products(productid int primary key, barcode varchar, name varchar not null, size varchar)");
                statement.execute("create table suppliers(supplier_id int, name varchar)");
                statement.execute("create table supplier_product_mappings(supplier_id int,supplier_product_code varchar,internal_product_id int)");
                statement.execute("insert into products values(1,'50360-001','Vinyl Silk Tinting Base','1LTR')");
                statement.execute("insert into products values(2,'BAR-2','Weather Guard Tinting Base','1LTR')");
                statement.execute("insert into supplier_product_mappings values(7,'WG-100',2)");
            }
            ProductMatchingService service = new ProductMatchingService(connection);

            ExtractedInvoiceItem exact = item("50360-001", "anything");
            service.matchItem(7, exact);
            assertEquals(1, exact.getMatchedProductId());
            assertEquals(ExtractedInvoiceItem.MatchingStatus.EXACT_CODE, exact.getMatchingStatus());

            ExtractedInvoiceItem mapped = item("WG-100", "anything");
            service.matchItem(7, mapped);
            assertEquals(2, mapped.getMatchedProductId());
            assertEquals(ExtractedInvoiceItem.MatchingStatus.SAVED_MAPPING, mapped.getMatchingStatus());

            ExtractedInvoiceItem byName = item(null, "Vinyl Silk Tinting Base");
            service.matchItem(7, byName);
            assertEquals(1, byName.getMatchedProductId());
            assertEquals(ExtractedInvoiceItem.MatchingStatus.EXACT_NAME, byName.getMatchingStatus());
        }
    }

    @Test
    void fuzzyCandidateIsSuggestionAndNeverAutomaticallyApproved() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:fuzzy;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("create table products(productid int primary key, barcode varchar, name varchar not null, size varchar)");
                statement.execute("create table supplier_product_mappings(supplier_id int,supplier_product_code varchar,internal_product_id int)");
                statement.execute("insert into products values(1,'A','Weather Guard Tinting Base','1LTR')");
            }
            ExtractedInvoiceItem item = item(null, "Weather Guard Tnting Base");
            new ProductMatchingService(connection).matchItem(null, item);
            assertEquals(ExtractedInvoiceItem.MatchingStatus.SUGGESTED_NAME, item.getMatchingStatus());
            assertFalse(item.isMatched());
        }
    }

    private ExtractedInvoiceItem item(String code, String name) {
        ExtractedInvoiceItem item = new ExtractedInvoiceItem();
        item.setSupplierProductCode(code);
        item.setDescription(name);
        return item;
    }
}
