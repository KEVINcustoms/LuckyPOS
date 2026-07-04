package com.nexatek;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class reports extends javax.swing.JPanel {

    private static final Color PAGE_BG = new Color(244, 247, 250);
    private static final Color INK = new Color(25, 42, 65);
    private static final Color MUTED = new Color(92, 104, 118);
    private static final Color BORDER = new Color(224, 230, 238);

    private final Connection conn;
    private JTextField invoice;

    public reports() {
        conn = connection.connect();
        buildLayout();
    }

    public void view_receipt() {
        if (invoice.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an invoice number.", "Reports", JOptionPane.WARNING_MESSAGE);
            invoice.requestFocusInWindow();
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("invoice_number", invoice.getText().trim());
        JasperReportHelper.showReport(conn, reports.class, "solditemsrepo.jrxml", params, "KEVINcustoms Report Viewer");
    }

    private void buildLayout() {
        setLayout(new MigLayout("insets 12, gap 10, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
        setBackground(PAGE_BG);

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][right][150!][90!]", "[][]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Review key report samples, then open the full printable Jasper report.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        invoice = new JTextField();
        invoice.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Invoice #");
        JButton viewInvoice = createButton("View", new Color(41, 128, 185));
        viewInvoice.addActionListener(e -> view_receipt());

        header.add(title);
        header.add(new JLabel("Invoice:"));
        header.add(invoice, "h 32!");
        header.add(viewInvoice, "h 32!");
        header.add(subtitle, "span 4, growx");
        add(header, "growx");

        JPanel grid = new JPanel(new MigLayout(
                "insets 0, gap 10, fill, wrap 2",
                "[grow,fill][grow,fill]",
                "[grow,fill][grow,fill][grow,fill]"));
        grid.setOpaque(false);
        grid.add(createReportCard(
                "Sales Report",
                "Recent sales and invoice lines",
                "Open Sales",
                new String[]{"Invoice", "Item", "Qty", "Price", "Status"},
                "SELECT invoice_number, name, quantity, itemprice, status FROM solditems ORDER BY invoice_number DESC LIMIT 8",
                () -> JasperReportHelper.showReport(conn, reports.class, "solditemsrepo.jrxml", "KEVINcustoms Report Viewer")), "grow");
        grid.add(createReportCard(
                "Cost Price Report",
                "Current stock cost allocation",
                "Open Costs",
                new String[]{"Product", "Qty", "Total Cost"},
                "SELECT product_name, quantity, sub_costp FROM sub_cost_price ORDER BY product_name LIMIT 8",
                () -> JasperReportHelper.showReport(conn, reports.class, "sub_cost_prices_repo.jrxml", "KEVINcustoms Report Viewer")), "grow");
        grid.add(createReportCard(
                "Product Retail Prices",
                "Product prices visible to sales",
                "Open Prices",
                new String[]{"Product", "Size", "Retail", "XL", "Stock"},
                "SELECT name, size, price, price2, quantity FROM products ORDER BY name LIMIT 8",
                () -> JasperReportHelper.showReport(conn, reports.class, "productprices.jrxml", "KEVINcustoms Report Viewer")), "grow");
        grid.add(createReportCard(
                "Product Price Levels",
                "Complete price ladder overview",
                "Open Levels",
                new String[]{"Product", "Retail", "XL", "XXL", "Stock"},
                "SELECT name, price, price2, price3, quantity FROM products ORDER BY name LIMIT 8",
                () -> JasperReportHelper.showReport(conn, reports.class, "productprices2.jrxml", "KEVINcustoms Report Viewer")), "grow");
        add(grid, "grow");
    }

    private JPanel createReportCard(String title, String helper, String buttonText, String[] columns, String sql, Runnable action) {
        JPanel card = new JPanel(new MigLayout("insets 12, gapy 8, fill, wrap 1", "[grow,fill]", "[][][grow,fill][]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(BORDER));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:8");

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(INK);

        JLabel helperLabel = new JLabel(helper);
        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helperLabel.setForeground(MUTED);

        JTable preview = new JTable(loadPreview(columns, sql));
        preview.setRowHeight(23);
        preview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        preview.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        preview.getTableHeader().setForeground(INK);
        preview.setEnabled(false);
        preview.setFillsViewportHeight(true);

        JButton openButton = createButton(buttonText, new Color(0, 128, 64));
        openButton.addActionListener(e -> action.run());

        card.add(titleLabel, "growx");
        card.add(helperLabel, "growx");
        card.add(new JScrollPane(preview), "grow, h 145:170:190");
        card.add(openButton, "align right, w 125!, h 34!");
        return card;
    }

    private DefaultTableModel loadPreview(String[] columns, String sql) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (conn == null) {
            model.addRow(emptyRow(columns.length, "No connection"));
            return model;
        }
        try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            int columnCount = columns.length;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
        } catch (Exception ex) {
            model.setRowCount(0);
            model.addRow(emptyRow(columns.length, "Preview unavailable"));
        }
        if (model.getRowCount() == 0) {
            model.addRow(emptyRow(columns.length, "No sample data"));
        }
        return model;
    }

    private Object[] emptyRow(int length, String message) {
        Object[] row = new Object[length];
        row[0] = message;
        for (int i = 1; i < length; i++) {
            row[i] = "";
        }
        return row;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        return button;
    }
}
