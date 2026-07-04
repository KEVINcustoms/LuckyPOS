package com.nexatek;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class QuotationPanel extends JPanel {

    private static final Color PAGE_BG = new Color(244, 247, 250);
    private static final Color INK = new Color(25, 42, 65);
    private static final Color GREEN = new Color(0, 128, 64);
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

    private final Connection conn;
    private final DefaultTableModel productModel;
    private final DefaultTableModel quoteModel;
    private final TableRowSorter<DefaultTableModel> productSorter;
    private final String preparedBy;

    private JTextField searchField;
    private JTextField customerField;
    private JTextField phoneField;
    private JTextField validDaysField;
    private JTextField preparedByField;
    private JLabel selectedProductLabel;
    private JLabel totalLabel;
    private JTable productsTable;
    private JTable quoteTable;
    private JDialog productPickerDialog;

    public QuotationPanel() {
        this("");
    }

    public QuotationPanel(String preparedBy) {
        this.preparedBy = preparedBy == null || preparedBy.trim().isEmpty() ? "Current User" : preparedBy.trim();
        conn = connection.connect();
        MONEY.setMaximumFractionDigits(2);
        productModel = new DefaultTableModel(new Object[]{"ID", "Barcode", "Name", "Size", "Price", "Stock", "Retail", "XL", "XXL", "Add"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        quoteModel = new DefaultTableModel(new Object[]{"ID", "Description", "Qty", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                if (column == 2 || column == 3) {
                    recalculateQuoteLine(row);
                }
            }
        };
        productSorter = new TableRowSorter<>(productModel);
        buildLayout();
        loadProducts();
    }

    private void buildLayout() {
        setLayout(new MigLayout("insets 10, gapy 8, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
        setBackground(PAGE_BG);

        JLabel title = new JLabel("Quotations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Prepare a complete customer quotation on one printable form.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(92, 104, 118));

        JPanel top = new JPanel(new MigLayout("insets 0, fillx", "[grow]", "[][]"));
        top.setOpaque(false);
        top.add(title, "growx");
        top.add(subtitle, "growx");
        add(top, "growx");
        add(createQuotationForm(), "grow");
    }

    private JPanel createQuotationForm() {
        JPanel form = new JPanel(new MigLayout(
                "insets 14, gapy 8, fill, wrap 1",
                "[grow,fill]",
                "[56!][86!][grow,fill][48!]"));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 232)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        form.putClientProperty(FlatClientProperties.STYLE, "arc:8");

        form.add(createFormHeader(), "growx");
        form.add(createCustomerSection(), "growx");
        form.add(createLineItemsSection(), "grow");
        form.add(createFormFooter(), "growx");
        return form;
    }

    private JPanel createFormHeader() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[][]"));
        header.setOpaque(false);

        JLabel company = new JLabel("KEBZ PHONE SERVICE CENTRE");
        company.setFont(new Font("Segoe UI", Font.BOLD, 22));
        company.setForeground(INK);

        JLabel document = new JLabel("QUOTATION");
        document.setFont(new Font("Segoe UI", Font.BOLD, 20));
        document.setForeground(GREEN);

        JLabel meta = new JLabel("Quote No: " + defaultQuoteNumber() + "    Date: " + LocalDate.now());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(92, 104, 118));

        JLabel terms = new JLabel("Prepared for customer approval. Stock is not reduced until a sale is completed.");
        terms.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        terms.setForeground(new Color(92, 104, 118));

        header.add(company);
        header.add(document);
        header.add(meta, "span 2");
        header.add(terms, "span 2");
        return header;
    }

    private JPanel createCustomerSection() {
        JPanel section = new JPanel(new MigLayout(
                "insets 6 0 0 0, gap 8, fillx, wrap 8",
                "[right][grow,fill][right][grow,fill][right][90!][right][190!]",
                "[30!][38!]"));
        section.setOpaque(false);

        customerField = new JTextField();
        phoneField = new JTextField();
        validDaysField = new JTextField("14");
        preparedByField = new JTextField(preparedBy);
        preparedByField.setEditable(false);
        preparedByField.setBackground(new Color(248, 250, 252));
        selectedProductLabel = new JLabel("No product selected");
        selectedProductLabel.setForeground(new Color(92, 104, 118));
        JButton openPicker = createButton("Add Product / Items", GREEN);
        openPicker.setFont(new Font("Segoe UI", Font.BOLD, 13));
        openPicker.addActionListener(e -> showProductPickerDialog());

        section.add(fieldLabel("Customer"));
        section.add(customerField, "h 32!");
        section.add(fieldLabel("Phone"));
        section.add(phoneField, "h 32!");
        section.add(fieldLabel("Valid Days"));
        section.add(validDaysField, "h 32!");
        section.add(fieldLabel("Prepared By"));
        section.add(preparedByField, "h 32!");
        section.add(fieldLabel("Last Added"));
        section.add(selectedProductLabel, "span 5, growx");
        section.add(openPicker, "h 38!");
        return section;
    }

    private void showProductPickerDialog() {
        if (productPickerDialog != null && productPickerDialog.isShowing()) {
            productPickerDialog.toFront();
            return;
        }

        productPickerDialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Add Products to Quotation", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        productPickerDialog.setLayout(new MigLayout("insets 18, gapy 12, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
        productPickerDialog.getContentPane().setBackground(PAGE_BG);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search products");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc:24;margin:8,16,8,16");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterProducts();
            }
        });

        productsTable = new JTable(productModel);
        productsTable.setRowSorter(productSorter);
        styleTable(productsTable);
        productsTable.setRowHeight(32);
        configureQuotationProductColumns();
        int actionColumn = productsTable.getColumnModel().getColumnCount() - 1;
        productsTable.getColumnModel().getColumn(actionColumn).setCellRenderer(new AddButtonRenderer());
        productsTable.getColumnModel().getColumn(actionColumn).setPreferredWidth(90);
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = productsTable.rowAtPoint(evt.getPoint());
                int column = productsTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && column == productsTable.getColumnModel().getColumnCount() - 1) {
                    addProductFromModelRow(productsTable.convertRowIndexToModel(row));
                }
            }
        });

        JPanel tablePanel = new JPanel(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(224, 230, 238)));
        tablePanel.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        tablePanel.add(new JScrollPane(productsTable), "grow");

        productPickerDialog.add(searchField, "growx, h 44!");
        productPickerDialog.add(tablePanel, "grow");
        productPickerDialog.setMinimumSize(new Dimension(820, 520));
        productPickerDialog.setSize(900, 560);
        productPickerDialog.setLocationRelativeTo(this);
        javax.swing.SwingUtilities.invokeLater(() -> searchField.requestFocusInWindow());
        productPickerDialog.setVisible(true);
    }

    private JPanel createLineItemsSection() {
        JPanel section = new JPanel(new MigLayout("insets 10 0 0 0, fill, wrap 1", "[grow,fill]", "[][grow,fill]"));
        section.setOpaque(false);
        quoteTable = new JTable(quoteModel);
        styleTable(quoteTable);
        quoteTable.setRowHeight(28);
        quoteTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        section.add(sectionTitle("Quotation Items"), "growx");
        section.add(new JScrollPane(quoteTable), "grow");
        return section;
    }

    private JPanel createFormFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 10 0 0 0, fillx", "[grow,fill][110!][130!][110!][110!]", "[40!]"));
        footer.setOpaque(false);

        totalLabel = new JLabel("0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalLabel.setForeground(GREEN);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton removeButton = createButton("Remove Item", new Color(220, 53, 69));
        JButton exportButton = createButton("Export PDF", new Color(41, 128, 185));
        JButton printButton = createButton("Print", GREEN);
        JButton clearButton = createButton("Clear", new Color(120, 120, 120));
        removeButton.addActionListener(e -> removeSelectedQuoteLine());
        exportButton.addActionListener(e -> exportPdf());
        printButton.addActionListener(e -> printQuotation());
        clearButton.addActionListener(e -> clearQuotation());

        JPanel totalPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][right][180!]", "[40!]"));
        totalPanel.setOpaque(false);
        JLabel note = new JLabel("Thank you for considering LuckyPOS services.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(new Color(92, 104, 118));
        JLabel totalCaption = new JLabel("Total:");
        totalCaption.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPanel.add(note);
        totalPanel.add(totalCaption);
        totalPanel.add(totalLabel, "growx");

        footer.add(totalPanel, "growx");
        footer.add(removeButton, "h 38!");
        footer.add(exportButton, "h 38!");
        footer.add(printButton, "h 38!");
        footer.add(clearButton, "h 38!");
        return footer;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(INK);
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(INK);
        label.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 230, 238)));
        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        return button;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionBackground(new Color(25, 42, 65));
        table.setSelectionForeground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void configureQuotationProductColumns() {
        if (productsTable == null || productsTable.getColumnModel().getColumnCount() < 10) {
            return;
        }
        productsTable.removeColumn(productsTable.getColumnModel().getColumn(8));
        productsTable.removeColumn(productsTable.getColumnModel().getColumn(7));
        productsTable.removeColumn(productsTable.getColumnModel().getColumn(6));
        productsTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        productsTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        productsTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        productsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        productsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        productsTable.getColumnModel().getColumn(5).setPreferredWidth(70);
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Products cannot be loaded.", "Quotations", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT productid, barcode, name, size, price, price2, price3, quantity FROM products ORDER BY name";
                try (PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getInt("productid"),
                            rs.getString("barcode"),
                            rs.getString("name"),
                            rs.getString("size"),
                            Math.max(rs.getDouble("price"), Math.max(rs.getDouble("price2"), rs.getDouble("price3"))),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getDouble("price2"),
                            rs.getDouble("price3"),
                            "Add"
                        });
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    productModel.setRowCount(0);
                    for (Object[] row : get()) {
                        productModel.addRow(row);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuotationPanel.this, "Unable to load products: " + ex.getMessage(), "Quotations", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void filterProducts() {
        if (searchField == null) {
            return;
        }
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            productSorter.setRowFilter(null);
        } else {
            productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void addProductFromModelRow(int modelRow) {
        Product product = new Product(
                intValue(productModel.getValueAt(modelRow, 0)),
                stringValue(productModel.getValueAt(modelRow, 1)),
                stringValue(productModel.getValueAt(modelRow, 2)),
                stringValue(productModel.getValueAt(modelRow, 3)),
                doubleValue(productModel.getValueAt(modelRow, 6)),
                doubleValue(productModel.getValueAt(modelRow, 7)),
                doubleValue(productModel.getValueAt(modelRow, 8)));
        double qty = 1;
        double unitPrice = Math.max(product.retailPrice, Math.max(product.xlPrice, product.xxlPrice));
        double total = qty * unitPrice;
        quoteModel.addRow(new Object[]{
            product.id,
            product.name + " (" + product.size + ")",
            formatPlain(qty),
            formatPlain(unitPrice),
            formatPlain(total)
        });
        selectedProductLabel.setText("Added: " + product.name + " (" + product.size + ")");
        updateTotal();
        if (productsTable != null) {
            productsTable.clearSelection();
        }
    }

    private void recalculateQuoteLine(int row) {
        if (row < 0 || row >= quoteModel.getRowCount()) {
            return;
        }
        double qty = Math.max(0, parseDouble(String.valueOf(quoteModel.getValueAt(row, 2)), 0));
        double unitPrice = Math.max(0, parseDouble(String.valueOf(quoteModel.getValueAt(row, 3)), 0));
        quoteModel.setValueAt(formatPlain(qty * unitPrice), row, 4);
        updateTotal();
    }

    private void removeSelectedQuoteLine() {
        int row = quoteTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a quotation item to remove.", "Quotations", JOptionPane.WARNING_MESSAGE);
            return;
        }
        quoteModel.removeRow(quoteTable.convertRowIndexToModel(row));
        updateTotal();
    }

    private void clearQuotation() {
        quoteModel.setRowCount(0);
        customerField.setText("");
        phoneField.setText("");
        validDaysField.setText("14");
        selectedProductLabel.setText("No product selected");
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(MONEY.format(calculateTotal()));
    }

    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < quoteModel.getRowCount(); i++) {
            total += parseDouble(String.valueOf(quoteModel.getValueAt(i, 4)), 0);
        }
        return total;
    }

    private void exportPdf() {
        if (!validateQuotation()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Quotation PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        chooser.setSelectedFile(new File(defaultQuoteNumber() + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.US).endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        try {
            SimplePdfWriter.writeQuotation(file, buildQuotation());
            int open = JOptionPane.showConfirmDialog(this, "PDF exported successfully.\nOpen it now?", "Quotations", JOptionPane.YES_NO_OPTION);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to export PDF: " + ex.getMessage(), "Quotations", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printQuotation() {
        if (!validateQuotation()) {
            return;
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Quotation " + defaultQuoteNumber());
        job.setPrintable(new QuotationPrintable(buildQuotation()));
        try {
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Unable to print quotation: " + ex.getMessage(), "Quotations", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateQuotation() {
        if (quoteModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one product to the quotation.", "Quotations", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (customerField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer name.", "Quotations", JOptionPane.WARNING_MESSAGE);
            customerField.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private Quotation buildQuotation() {
        List<QuoteLine> lines = new ArrayList<>();
        for (int i = 0; i < quoteModel.getRowCount(); i++) {
            lines.add(new QuoteLine(
                    stringValue(quoteModel.getValueAt(i, 1)),
                    parseDouble(String.valueOf(quoteModel.getValueAt(i, 2)), 0),
                    parseDouble(String.valueOf(quoteModel.getValueAt(i, 3)), 0),
                    parseDouble(String.valueOf(quoteModel.getValueAt(i, 4)), 0)));
        }
        int validDays = (int) parseDouble(validDaysField.getText(), 14);
        LocalDate date = LocalDate.now();
        return new Quotation(
                defaultQuoteNumber(),
                customerField.getText().trim(),
                phoneField.getText().trim(),
                preparedByField.getText().trim(),
                date,
                date.plusDays(Math.max(validDays, 1)),
                lines,
                calculateTotal());
    }

    private String defaultQuoteNumber() {
        return "QTN-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()) + "-" + String.format("%04d", quoteModel.getRowCount() + 1);
    }

    private static String formatPlain(double value) {
        if (Math.rint(value) == value) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double doubleValue(Object value) {
        return parseDouble(String.valueOf(value), 0);
    }

    private static int intValue(Object value) {
        return (int) parseDouble(String.valueOf(value), 0);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private class AddButtonRenderer extends JButton implements TableCellRenderer {

        private AddButtonRenderer() {
            setText("Add");
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBackground(GREEN);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private static class Product {

        private final int id;
        private final String barcode;
        private final String name;
        private final String size;
        private final double retailPrice;
        private final double xlPrice;
        private final double xxlPrice;

        private Product(int id, String barcode, String name, String size, double retailPrice, double xlPrice, double xxlPrice) {
            this.id = id;
            this.barcode = barcode;
            this.name = name;
            this.size = size;
            this.retailPrice = retailPrice;
            this.xlPrice = xlPrice;
            this.xxlPrice = xxlPrice;
        }
    }

    private static class QuoteLine {

        private final String description;
        private final double quantity;
        private final double unitPrice;
        private final double total;

        private QuoteLine(String description, double quantity, double unitPrice, double total) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }
    }

    private static class Quotation {

        private final String number;
        private final String customer;
        private final String phone;
        private final String preparedBy;
        private final LocalDate date;
        private final LocalDate validUntil;
        private final List<QuoteLine> lines;
        private final double total;

        private Quotation(String number, String customer, String phone, String preparedBy, LocalDate date, LocalDate validUntil, List<QuoteLine> lines, double total) {
            this.number = number;
            this.customer = customer;
            this.phone = phone;
            this.preparedBy = preparedBy;
            this.date = date;
            this.validUntil = validUntil;
            this.lines = lines;
            this.total = total;
        }
    }

    private static class QuotationPrintable implements Printable {

        private final Quotation quotation;

        private QuotationPrintable(Quotation quotation) {
            this.quotation = quotation;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            drawQuotation(g2, quotation, (int) pageFormat.getImageableWidth());
            return PAGE_EXISTS;
        }
    }

    private static void drawQuotation(Graphics2D g2, Quotation quotation, int width) {
        int y = 28;
        g2.setColor(INK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.drawString("KEBZ PHONE SERVICE CENTRE", 20, y);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString("QUOTATION", width - 140, y);
        y += 28;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.drawString("Quotation No: " + quotation.number, 20, y);
        g2.drawString("Date: " + quotation.date, width - 180, y);
        y += 16;
        g2.drawString("Customer: " + quotation.customer, 20, y);
        g2.drawString("Valid Until: " + quotation.validUntil, width - 180, y);
        y += 16;
        g2.drawString("Phone: " + quotation.phone, 20, y);
        g2.drawString("Prepared By: " + quotation.preparedBy, width - 180, y);
        y += 24;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.drawLine(20, y, width - 20, y);
        y += 15;
        g2.drawString("Description", 20, y);
        g2.drawString("Qty", width - 230, y);
        g2.drawString("Unit Price", width - 170, y);
        g2.drawString("Total", width - 70, y);
        y += 8;
        g2.drawLine(20, y, width - 20, y);
        y += 16;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (QuoteLine line : quotation.lines) {
            g2.drawString(trim(line.description, 44), 20, y);
            g2.drawString(formatPlain(line.quantity), width - 230, y);
            g2.drawString(MONEY.format(line.unitPrice), width - 170, y);
            g2.drawString(MONEY.format(line.total), width - 70, y);
            y += 18;
        }
        y += 8;
        g2.drawLine(width - 220, y, width - 20, y);
        y += 20;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString("Total: " + MONEY.format(quotation.total), width - 220, y);
        y += 34;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.drawString("Thank you for considering LuckyPOS services. This quotation is valid until the date shown above.", 20, y);
    }

    private static String trim(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static class SimplePdfWriter {

        private static void writeQuotation(File file, Quotation quotation) throws Exception {
            byte[] stream = buildQuotationPage(quotation).getBytes(StandardCharsets.US_ASCII);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            write(out, "%PDF-1.4\n");
            offsets.add(out.size());
            write(out, "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
            offsets.add(out.size());
            write(out, "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
            offsets.add(out.size());
            write(out, "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 6 0 R >> >> /Contents 5 0 R >> endobj\n");
            offsets.add(out.size());
            write(out, "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
            offsets.add(out.size());
            write(out, "5 0 obj << /Length " + stream.length + " >> stream\n");
            out.write(stream);
            write(out, "\nendstream endobj\n");
            offsets.add(out.size());
            write(out, "6 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> endobj\n");
            int xref = out.size();
            write(out, "xref\n0 7\n0000000000 65535 f \n");
            for (Integer offset : offsets) {
                write(out, String.format(Locale.US, "%010d 00000 n \n", offset));
            }
            write(out, "trailer << /Size 7 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                out.writeTo(fos);
            }
        }

        private static String buildQuotationPage(Quotation quotation) {
            StringBuilder pdf = new StringBuilder();
            drawHeader(pdf, quotation);
            drawInfoBlocks(pdf, quotation);
            drawItemsTable(pdf, quotation);
            drawSummary(pdf, quotation);
            drawFooter(pdf);
            return pdf.toString();
        }

        private static void drawHeader(StringBuilder pdf, Quotation quotation) {
            fill(pdf, 38, 760, 519, 48, "0.000 0.502 0.251");
            whiteText(pdf, "F2", 20, 54, 790, "KEBZ PHONE SERVICE CENTRE");
            whiteText(pdf, "F1", 9, 55, 775, "Phone service, accessories, repairs and sales");
            whiteText(pdf, "F2", 18, 430, 790, "QUOTATION");
            whiteText(pdf, "F1", 9, 430, 775, quotation.number);
        }

        private static void drawInfoBlocks(StringBuilder pdf, Quotation quotation) {
            stroke(pdf, 38, 675, 250, 62, "0.820 0.850 0.890");
            stroke(pdf, 307, 675, 250, 62, "0.820 0.850 0.890");

            text(pdf, "F2", 10, 52, 718, "Prepared For");
            text(pdf, "F1", 10, 52, 701, emptyAsDash(quotation.customer));
            text(pdf, "F1", 9, 52, 685, "Phone: " + emptyAsDash(quotation.phone));

            text(pdf, "F2", 10, 321, 718, "Quotation Details");
            text(pdf, "F1", 9, 321, 702, "Date: " + quotation.date);
            text(pdf, "F1", 9, 321, 688, "Valid Until: " + quotation.validUntil);
            text(pdf, "F1", 9, 430, 688, "Prepared By: " + emptyAsDash(quotation.preparedBy));
        }

        private static void drawItemsTable(StringBuilder pdf, Quotation quotation) {
            int left = 38;
            int top = 642;
            int width = 519;
            int rowHeight = 22;
            fill(pdf, left, top, width, rowHeight, "0.945 0.965 0.980");
            stroke(pdf, left, top, width, rowHeight, "0.760 0.800 0.850");

            text(pdf, "F2", 9, 50, top + 7, "Description");
            text(pdf, "F2", 9, 320, top + 7, "Qty");
            text(pdf, "F2", 9, 375, top + 7, "Unit Price");
            text(pdf, "F2", 9, 485, top + 7, "Total");

            int y = top - rowHeight;
            int shownRows = 0;
            for (QuoteLine line : quotation.lines) {
                if (shownRows >= 20) {
                    text(pdf, "F1", 9, 50, y + 7, "...additional items continue in the application view");
                    break;
                }
                if (shownRows % 2 == 1) {
                    fill(pdf, left, y, width, rowHeight, "0.985 0.990 0.995");
                }
                stroke(pdf, left, y, width, rowHeight, "0.900 0.920 0.945");
                text(pdf, "F1", 9, 50, y + 7, trim(line.description, 46));
                rightText(pdf, "F1", 9, 340, y + 7, formatPlain(line.quantity));
                rightText(pdf, "F1", 9, 445, y + 7, MONEY.format(line.unitPrice));
                rightText(pdf, "F1", 9, 542, y + 7, MONEY.format(line.total));
                y -= rowHeight;
                shownRows++;
            }
        }

        private static void drawSummary(StringBuilder pdf, Quotation quotation) {
            int boxX = 355;
            int boxY = 120;
            int boxW = 202;
            int boxH = 68;
            stroke(pdf, boxX, boxY, boxW, boxH, "0.760 0.800 0.850");
            fill(pdf, boxX, boxY, boxW, 30, "0.000 0.502 0.251");
            text(pdf, "F2", 11, boxX + 14, boxY + 48, "Grand Total");
            rightText(pdf, "F2", 16, boxX + boxW - 14, boxY + 10, MONEY.format(quotation.total));

            text(pdf, "F2", 10, 38, 170, "Terms and Notes");
            text(pdf, "F1", 9, 38, 154, "1. This quotation is valid until the date shown above.");
            text(pdf, "F1", 9, 38, 140, "2. Stock is not reduced until a sale is completed.");
            text(pdf, "F1", 9, 38, 126, "3. Prices may change if product availability or selected quantities change.");
        }

        private static void drawFooter(StringBuilder pdf) {
            line(pdf, 38, 88, 557, 88, "0.820 0.850 0.890");
            text(pdf, "F1", 8, 38, 70, "Thank you for considering LuckyPOS services.");
            rightText(pdf, "F1", 8, 557, 70, "Generated by LuckyPOS");
        }

        private static void text(StringBuilder pdf, String font, int size, int x, int y, String value) {
            colorText(pdf, "0 0 0", font, size, x, y, value);
        }

        private static void whiteText(StringBuilder pdf, String font, int size, int x, int y, String value) {
            colorText(pdf, "1 1 1", font, size, x, y, value);
        }

        private static void colorText(StringBuilder pdf, String rgb, String font, int size, int x, int y, String value) {
            pdf.append(rgb).append(" rg BT /").append(font).append(' ').append(size).append(" Tf ")
                    .append(x).append(' ').append(y).append(" Td (")
                    .append(escape(value)).append(") Tj ET\n");
        }

        private static void rightText(StringBuilder pdf, String font, int size, int rightX, int y, String value) {
            int approximateWidth = safe(value).length() * size / 2;
            text(pdf, font, size, rightX - approximateWidth, y, value);
        }

        private static void fill(StringBuilder pdf, int x, int y, int w, int h, String rgb) {
            pdf.append(rgb).append(" rg ").append(x).append(' ').append(y).append(' ')
                    .append(w).append(' ').append(h).append(" re f\n");
        }

        private static void stroke(StringBuilder pdf, int x, int y, int w, int h, String rgb) {
            pdf.append(rgb).append(" RG 0.8 w ").append(x).append(' ').append(y).append(' ')
                    .append(w).append(' ').append(h).append(" re S\n");
        }

        private static void line(StringBuilder pdf, int x1, int y1, int x2, int y2, String rgb) {
            pdf.append(rgb).append(" RG 0.8 w ").append(x1).append(' ').append(y1)
                    .append(" m ").append(x2).append(' ').append(y2).append(" l S\n");
        }

        private static void write(ByteArrayOutputStream out, String value) {
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
            out.write(bytes, 0, bytes.length);
        }

        private static String escape(String value) {
            return safe(value).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        }

        private static String emptyAsDash(String value) {
            String text = safe(value).trim();
            return text.isEmpty() ? "-" : text;
        }

        private static String safe(String value) {
            return value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "");
        }
    }
}
