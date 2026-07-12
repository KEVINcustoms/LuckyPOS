package com.nexatek.invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Conservative matching against existing products; fuzzy candidates are never auto-approved. */
public final class ProductMatchingService {

    private static final double FUZZY_THRESHOLD = 0.90;
    private static final double UNIQUE_MARGIN = 0.05;
    private final Connection connection;

    public ProductMatchingService(Connection connection) {
        this.connection = connection;
    }

    public void match(ExtractedInvoice invoice) throws SQLException {
        if (invoice.getSupplierId() == null && invoice.getSupplierName() != null) {
            findSupplierByExactName(invoice.getSupplierName()).ifPresent(choice -> invoice.setSupplierId(choice.supplierId()));
        }
        for (ExtractedInvoiceItem item : invoice.getItems()) {
            matchItem(invoice.getSupplierId(), item);
        }
    }

    public Optional<ProductMatch> matchItem(Integer supplierId, ExtractedInvoiceItem item) throws SQLException {
        if (item == null) return Optional.empty();
        String code = clean(item.getSupplierProductCode());
        if (!code.isBlank()) {
            Optional<ProductMatch> directCode = findByBarcode(code, ExtractedInvoiceItem.MatchingStatus.EXACT_CODE);
            if (directCode.isPresent()) return apply(item, directCode.get(), true);

            if (supplierId != null) {
                Optional<ProductMatch> saved = findSavedMapping(supplierId, code);
                if (saved.isPresent()) return apply(item, saved.get(), true);
            }
        }

        String normalizedName = normalize(item.getDescription());
        if (!normalizedName.isBlank()) {
            Optional<ProductMatch> exactName = findByNormalizedName(normalizedName);
            if (exactName.isPresent()) return apply(item, exactName.get(), true);

            List<ProductMatch> candidates = fuzzyCandidates(normalizedName);
            if (!candidates.isEmpty()) {
                ProductMatch best = candidates.get(0);
                double second = candidates.size() > 1 ? candidates.get(1).similarity() : 0.0;
                if (best.similarity() >= FUZZY_THRESHOLD && best.similarity() - second >= UNIQUE_MARGIN) {
                    // Suggested only: the UI must obtain explicit confirmation before receiving stock.
                    return apply(item, new ProductMatch(best.productId(), best.barcode(), best.name(), best.size(),
                            ExtractedInvoiceItem.MatchingStatus.SUGGESTED_NAME, best.similarity()), false);
                }
            }
        }
        return Optional.empty();
    }

    public List<ProductMatch> searchProducts(String query) throws SQLException {
        String value = query == null ? "" : query.trim();
        String sql = "select productid, barcode, name, size from products "
                + "where lower(coalesce(barcode,'')) like lower(?) or lower(name) like lower(?) order by name limit 100";
        List<ProductMatch> products = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + value + "%");
            statement.setString(2, "%" + value + "%");
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) products.add(readProduct(result, ExtractedInvoiceItem.MatchingStatus.MANUAL, 1.0));
            }
        }
        return products;
    }

    public List<SupplierChoice> listSuppliers() throws SQLException {
        List<SupplierChoice> suppliers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select supplier_id, name from suppliers order by name");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) suppliers.add(new SupplierChoice(result.getInt(1), result.getString(2)));
        }
        return suppliers;
    }

    public Optional<SupplierChoice> findSupplierByExactName(String name) throws SQLException {
        String target = normalize(name);
        for (SupplierChoice supplier : listSuppliers()) {
            if (normalize(supplier.name()).equals(target)) return Optional.of(supplier);
        }
        return Optional.empty();
    }

    public void confirmManual(ExtractedInvoiceItem item, ProductMatch product, boolean saveMapping) {
        item.setMatchedProductId(product.productId());
        item.setMatchedProductName(product.name());
        item.setMatchingStatus(ExtractedInvoiceItem.MatchingStatus.MANUAL);
        item.setSaveSupplierMapping(saveMapping);
    }

    private Optional<ProductMatch> findByBarcode(String code, ExtractedInvoiceItem.MatchingStatus status) throws SQLException {
        String sql = "select productid, barcode, name, size from products where lower(trim(coalesce(barcode,'')))=lower(trim(?)) order by productid limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readProduct(result, status, 1.0)) : Optional.empty();
            }
        }
    }

    private Optional<ProductMatch> findSavedMapping(int supplierId, String code) throws SQLException {
        String sql = "select p.productid, p.barcode, p.name, p.size from supplier_product_mappings m "
                + "join products p on p.productid=m.internal_product_id "
                + "where m.supplier_id=? and lower(trim(m.supplier_product_code))=lower(trim(?)) limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            statement.setString(2, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readProduct(result,
                        ExtractedInvoiceItem.MatchingStatus.SAVED_MAPPING, 1.0)) : Optional.empty();
            }
        }
    }

    private Optional<ProductMatch> findByNormalizedName(String normalized) throws SQLException {
        for (ProductMatch product : allProducts()) {
            if (normalize(product.name()).equals(normalized)) {
                return Optional.of(new ProductMatch(product.productId(), product.barcode(), product.name(), product.size(),
                        ExtractedInvoiceItem.MatchingStatus.EXACT_NAME, 1.0));
            }
        }
        return Optional.empty();
    }

    private List<ProductMatch> fuzzyCandidates(String normalized) throws SQLException {
        List<ProductMatch> candidates = new ArrayList<>();
        for (ProductMatch product : allProducts()) {
            double similarity = similarity(normalized, normalize(product.name()));
            if (similarity >= 0.75) candidates.add(new ProductMatch(product.productId(), product.barcode(), product.name(),
                    product.size(), ExtractedInvoiceItem.MatchingStatus.SUGGESTED_NAME, similarity));
        }
        candidates.sort(Comparator.comparingDouble(ProductMatch::similarity).reversed());
        return candidates;
    }

    private List<ProductMatch> allProducts() throws SQLException {
        List<ProductMatch> products = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select productid, barcode, name, size from products");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) products.add(readProduct(result, ExtractedInvoiceItem.MatchingStatus.UNMATCHED, 0));
        }
        return products;
    }

    private ProductMatch readProduct(ResultSet result, ExtractedInvoiceItem.MatchingStatus status, double similarity) throws SQLException {
        return new ProductMatch(result.getInt("productid"), result.getString("barcode"), result.getString("name"),
                result.getString("size"), status, similarity);
    }

    private Optional<ProductMatch> apply(ExtractedInvoiceItem item, ProductMatch match, boolean approved) {
        item.setMatchedProductId(match.productId());
        item.setMatchedProductName(match.name());
        item.setMatchingStatus(match.status());
        if (!approved) item.setSaveSupplierMapping(false);
        return Optional.of(match);
    }

    static double similarity(String left, String right) {
        if (left == null || right == null || left.isBlank() || right.isBlank()) return 0.0;
        int distance = levenshtein(left, right);
        return 1.0 - (double) distance / Math.max(left.length(), right.length());
    }

    private static int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            int[] current = new int[right.length() + 1];
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            previous = current;
        }
        return previous[right.length()];
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
