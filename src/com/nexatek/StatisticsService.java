package com.nexatek;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Shared source for LuckyPOS product statistics.
 */
public class StatisticsService {

    public static final String[] TABLE_COLUMNS = {
        "Product Id",
        "Product Name",
        "Initial Quantity",
        "Unit Cost Price",
        "Total Cost Prices",
        "Total Sales",
        "Stock Quantity",
        "Profits"
    };

    private final Connection conn;

    public StatisticsService(Connection conn) {
        this.conn = conn;
    }

    public List<ProfitRow> loadProfitRows() throws SQLException {
        List<ProfitRow> rows = new ArrayList<>();
        String sql = ""
                + "SELECT p.productid, p.name, sc.quantity AS initial_quantity, "
                + "p.cost_price AS unit_cost_price, sc.sub_costp AS total_cost_prices, "
                + "COALESCE(SUM(si.paid_amount), 0) AS total_sales, p.quantity AS stock_quantity "
                + "FROM products p "
                + "INNER JOIN sub_cost_price sc ON p.name = sc.product_name "
                + "LEFT JOIN solditems si ON si.itemid = p.productid "
                + "GROUP BY p.productid, p.name, sc.quantity, p.cost_price, sc.sub_costp, p.quantity "
                + "ORDER BY p.name";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rst = pst.executeQuery()) {
            while (rst.next()) {
                double totalCost = rst.getDouble("total_cost_prices");
                double totalSales = rst.getDouble("total_sales");
                rows.add(new ProfitRow(
                        rst.getInt("productid"),
                        rst.getString("name"),
                        rst.getInt("initial_quantity"),
                        rst.getDouble("unit_cost_price"),
                        totalCost,
                        totalSales,
                        rst.getInt("stock_quantity"),
                        totalSales - totalCost
                ));
            }
        }
        return rows;
    }

    public DefaultTableModel createTableModel(List<ProfitRow> rows) {
        DefaultTableModel model = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return String.class;
                }
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 6) {
                    return Integer.class;
                }
                return Double.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (ProfitRow row : rows) {
            model.addRow(row.toTableRow());
        }
        return model;
    }

    public double totalPositiveProfit(List<ProfitRow> rows) {
        double total = 0;
        for (ProfitRow row : rows) {
            if (row.getProfit() > 0) {
                total += row.getProfit();
            }
        }
        return total;
    }

    public void saveProfitSnapshot(List<ProfitRow> rows) throws SQLException {
        insertProfitRows(rows, resolveProfitDateColumn());
    }

    private void insertProfitRows(List<ProfitRow> rows, String dateColumn) throws SQLException {
        String sql = "INSERT INTO profits "
                + "(productid, productname, initialquantity, unitcost, totalcostprices, totalsales, stockquantity, profits, "
                + dateColumn + ") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            Date today = Date.valueOf(LocalDate.now());
            for (ProfitRow row : rows) {
                pst.setInt(1, row.getProductId());
                pst.setString(2, row.getProductName());
                pst.setInt(3, row.getInitialQuantity());
                pst.setDouble(4, row.getUnitCostPrice());
                pst.setDouble(5, row.getTotalCostPrices());
                pst.setDouble(6, row.getTotalSales());
                pst.setInt(7, row.getStockQuantity());
                pst.setDouble(8, row.getProfit());
                pst.setDate(9, today);
                pst.addBatch();
            }
            pst.executeBatch();
        }
    }

    private String resolveProfitDateColumn() throws SQLException {
        if (hasColumn("profits", "profit_date")) {
            return "profit_date";
        }
        if (hasColumn("profits", "date")) {
            return "date";
        }
        return "profit_date";
    }

    private boolean hasColumn(String tableName, String columnName) throws SQLException {
        try (ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    public static final class ProfitRow {
        private final int productId;
        private final String productName;
        private final int initialQuantity;
        private final double unitCostPrice;
        private final double totalCostPrices;
        private final double totalSales;
        private final int stockQuantity;
        private final double profit;

        public ProfitRow(int productId, String productName, int initialQuantity,
                double unitCostPrice, double totalCostPrices, double totalSales,
                int stockQuantity, double profit) {
            this.productId = productId;
            this.productName = productName;
            this.initialQuantity = initialQuantity;
            this.unitCostPrice = unitCostPrice;
            this.totalCostPrices = totalCostPrices;
            this.totalSales = totalSales;
            this.stockQuantity = stockQuantity;
            this.profit = profit;
        }

        public Object[] toTableRow() {
            return new Object[]{
                productId,
                productName,
                initialQuantity,
                unitCostPrice,
                totalCostPrices,
                totalSales,
                stockQuantity,
                profit
            };
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getInitialQuantity() {
            return initialQuantity;
        }

        public double getUnitCostPrice() {
            return unitCostPrice;
        }

        public double getTotalCostPrices() {
            return totalCostPrices;
        }

        public double getTotalSales() {
            return totalSales;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }

        public double getProfit() {
            return profit;
        }
    }
}
