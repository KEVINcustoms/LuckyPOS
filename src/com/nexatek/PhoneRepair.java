/*
 * Professional Phone Repair Service Management System
 * Features: Intake form, repair tracking, receipt generation, auto-print on save
 * Enhanced UI with professional styling
 */
package com.nexatek;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Professional Phone Repair Service Management Panel
 * @author LuckyPOS Development Team
 */
public class PhoneRepair extends javax.swing.JPanel {
    
    Connection conn;
    ResultSet rst;
    PreparedStatement pst;
    
    // Color Palette - Professional Modern Theme
    private static final Color PRIMARY_COLOR = new Color(13, 71, 161);      // Deep Blue
    private static final Color SECONDARY_COLOR = new Color(25, 103, 210);    // Medium Blue
    private static final Color ACCENT_COLOR = new Color(255, 152, 0);        // Amber
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);       // Green
    private static final Color DANGER_COLOR = new Color(198, 40, 40);        // Red
    private static final Color WARNING_COLOR = new Color(251, 140, 0);       // Orange
    private static final Color BG_LIGHT = new Color(245, 247, 250);          // Light Gray Blue
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);         // Dark Gray
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);    // Medium Gray
    private static final Color BORDER_COLOR = new Color(224, 224, 224);      // Light Gray
    
    // UI Components
    private JPanel headerPanel;
    private JPanel intakePanel;
    private JPanel repairsTablePanel;
    private JPanel actionsPanel;
    
    // Intake form fields
    private JTextField customerName;
    private JTextField customerPhone;
    private JTextField phoneModel;
    private JTextField phoneBrand;
    private JComboBox<String> phoneColor;
    private JComboBox<String> repairType;
    private JSpinner agreedAmount;
    private JLabel receiptNumberLabel;
    private JLabel receiptStatusLabel;
    
    // Table for repairs
    private JTable repairsTable;
    
    // Buttons
    private JButton saveButton;
    private JButton completeButton;
    private JButton recordPaymentButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton printReceiptButton;
    private JButton searchButton;
    private JButton refreshButton;
    
    // Search field
    private JTextField searchField;
    
    // Last saved receipt number
    private String lastReceiptNumber;
    
    public PhoneRepair() {
        conn = connection.connect();
        initComponents();
        setupTable();
        loadRepairs();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        createHeaderPanel();
        
        // Intake Panel
        createIntakePanel();
        
        // Table Panel
        createTablePanel();
        
        // Actions Panel
        createActionsPanel();
        
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add panels with proper sizing
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setPreferredSize(new Dimension(900, 40));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        intakePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        intakePanel.setPreferredSize(new Dimension(900, 250));
        intakePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setPreferredSize(new Dimension(900, 50));
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        repairsTablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        repairsTablePanel.setPreferredSize(new Dimension(900, 300));
        repairsTablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(intakePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(actionsPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(repairsTablePanel);
        contentPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createHeaderPanel() {
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout(15, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setOpaque(false);
        
        // Left: Icon and Title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("PHONE REPAIR SERVICE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Professional Device Repair Management");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalGlue());
        
        // Right: Receipt Info
        JPanel receiptInfoPanel = new JPanel();
        receiptInfoPanel.setLayout(new BoxLayout(receiptInfoPanel, BoxLayout.Y_AXIS));
        receiptInfoPanel.setOpaque(false);
        
        JLabel receiptTitleLabel = new JLabel("Last Receipt");
        receiptTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        receiptTitleLabel.setForeground(new Color(200, 220, 255));
        
        receiptNumberLabel = new JLabel("AUTO-GENERATED");
        receiptNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        receiptNumberLabel.setForeground(ACCENT_COLOR);
        
        receiptStatusLabel = new JLabel("Ready for new repair");
        receiptStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        receiptStatusLabel.setForeground(new Color(200, 220, 255));
        
        receiptInfoPanel.add(Box.createVerticalGlue());
        receiptInfoPanel.add(receiptTitleLabel);
        receiptInfoPanel.add(receiptNumberLabel);
        receiptInfoPanel.add(receiptStatusLabel);
        receiptInfoPanel.add(Box.createVerticalGlue());
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(receiptInfoPanel, BorderLayout.EAST);
    }
    
    private void createIntakePanel() {
        intakePanel = new JPanel();
        intakePanel.setBackground(Color.WHITE);
        intakePanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        intakePanel.setLayout(new GridBagLayout());
        intakePanel.setPreferredSize(new Dimension(900, 260));
        
        // Panel title
        JLabel formTitleLabel = new JLabel("REPAIR INTAKE FORM");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitleLabel.setForeground(PRIMARY_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 15, 0);
        intakePanel.add(formTitleLabel, gbc);
        
        // Reset for form fields
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1: Customer Details
        gbc.gridy = 1;
        addFormLabel("👤 Customer Name:", intakePanel, gbc, 0);
        customerName = addFormField(intakePanel, gbc, 1, 1.0);
        
        addFormLabel("📞 Phone Number:", intakePanel, gbc, 2);
        customerPhone = addFormField(intakePanel, gbc, 3, 1.0);
        
        // Row 2: Device Details
        gbc.gridy = 2;
        addFormLabel("Phone Brand:", intakePanel, gbc, 0);
        phoneBrand = addFormField(intakePanel, gbc, 1, 0.5);
        
        addFormLabel("Phone Model:", intakePanel, gbc, 2);
        phoneModel = addFormField(intakePanel, gbc, 3, 1.0);
        
        // Row 3: Color and Repair Type
        gbc.gridy = 3;
        String[] colors = {"Black", "White", "Silver", "Gold", "Space Gray", "Blue", "Red", "Green", "Purple", "Other"};
        phoneColor = new JComboBox<>(colors);
        styleComboBox(phoneColor);
        addFormLabel("🎨 Phone Color:", intakePanel, gbc, 0);
        gbc.gridx = 1;
        intakePanel.add(phoneColor, gbc);
        
        String[] repairTypes = {"Screen Replacement", "Battery Replacement", "Charging Port", "Software Issue", 
                                "Water Damage", "Speaker/Mic Issue", "Button Repair", "Back Glass", 
                                "Front Glass", "General Maintenance", "Other"};
        repairType = new JComboBox<>(repairTypes);
        styleComboBox(repairType);
        addFormLabel("🔧 Repair Type:", intakePanel, gbc, 2);
        gbc.gridx = 3;
        intakePanel.add(repairType, gbc);
        
        // Row 4: Amount and Receipt Number
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        addFormLabel("💰 Agreed Amount (UGX):", intakePanel, gbc, 0);
        
        agreedAmount = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000000.0, 5000.0));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(agreedAmount);
        editor.getFormat().setGroupingUsed(true);
        agreedAmount.setEditor(editor);
        agreedAmount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        agreedAmount.setPreferredSize(new Dimension(180, 38));
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        intakePanel.add(agreedAmount, gbc);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        addFormLabel("🧾 Receipt Number:", intakePanel, gbc, 0);
        
        receiptNumberLabel = new JLabel("AUTO-GENERATED");
        receiptNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        receiptNumberLabel.setForeground(ACCENT_COLOR);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        intakePanel.add(receiptNumberLabel, gbc);
    }
    
    private JTextField addFormField(JPanel panel, GridBagConstraints gbc, int col, double weightx) {
        gbc.gridx = col;
        gbc.weightx = weightx;
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBackground(BG_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(0, 38));
        panel.add(field, gbc);
        return field;
    }
    
    private void addFormLabel(String text, JPanel panel, GridBagConstraints gbc, int col) {
        gbc.gridx = col;
        gbc.weightx = 0;
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(PRIMARY_COLOR);
        panel.add(label, gbc);
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(BG_LIGHT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        combo.setPreferredSize(new Dimension(0, 38));
    }
    
    private void createTablePanel() {
        repairsTablePanel = new JPanel(new BorderLayout(10, 10));
        repairsTablePanel.setBackground(Color.WHITE);
        repairsTablePanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Find Repair:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        searchLabel.setForeground(PRIMARY_COLOR);
        
        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        searchField.setBackground(BG_LIGHT);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        searchButton = createStyledButton("Search", SECONDARY_COLOR);
        searchButton.addActionListener(e -> searchRepairs());
        
        refreshButton = createStyledButton("Refresh", SECONDARY_COLOR);
        refreshButton.addActionListener(e -> loadRepairs());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        repairsTablePanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table
        repairsTable = new JTable();
        repairsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        repairsTable.setRowHeight(34);
        repairsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        repairsTable.setGridColor(BORDER_COLOR);
        repairsTable.setShowGrid(true);
        repairsTable.setFillsViewportHeight(true);
        repairsTable.setDefaultRenderer(Object.class, new RepairStatusRenderer());
        
        // Style table header
        JTableHeader header = repairsTable.getTableHeader();
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        JScrollPane tableScroll = new JScrollPane(repairsTable);
        tableScroll.getViewport().setBackground(Color.WHITE);
        repairsTablePanel.add(tableScroll, BorderLayout.CENTER);
    }
    
    private void createActionsPanel() {
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionsPanel.setBackground(BG_LIGHT);
        actionsPanel.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        saveButton = createStyledButton("💾 SAVE & PRINT RECEIPT", SUCCESS_COLOR);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveButton.setPreferredSize(new Dimension(200, 45));
        saveButton.addActionListener(e -> saveRepair());
        
        completeButton = createStyledButton("✓ Confirm Complete", PRIMARY_COLOR);
        completeButton.addActionListener(e -> markCompleted());
        
        recordPaymentButton = createStyledButton("💳 Record Payment", SUCCESS_COLOR);
        recordPaymentButton.addActionListener(e -> recordPayment());
        
        deleteButton = createStyledButton("🗑 Delete", DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteRepair());
        
        clearButton = createStyledButton("↻ Clear", TEXT_SECONDARY);
        clearButton.addActionListener(e -> clearForm());
        
        printReceiptButton = createStyledButton("🧾 Print Receipt", ACCENT_COLOR);
        printReceiptButton.addActionListener(e -> printReceipt());
        
        actionsPanel.add(saveButton);
        actionsPanel.add(completeButton);
        actionsPanel.add(recordPaymentButton);
        actionsPanel.add(printReceiptButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darkenColor(bgColor, 0.85f));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }
    
    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max(0, (int) (color.getRed() * factor)),
            Math.max(0, (int) (color.getGreen() * factor)),
            Math.max(0, (int) (color.getBlue() * factor))
        );
    }
    
    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "Receipt #", "Customer", "Phone", "Device", "Repair Type",
            "Agreed", "Paid", "Balance", "Payment Status", "Date", "Status", "Action"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11;
            }
        };
        repairsTable.setModel(model);
        
        repairsTable.getColumnModel().getColumn(10).setPreferredWidth(110);
        repairsTable.getColumnModel().getColumn(11).setPreferredWidth(140);
        repairsTable.getColumnModel().getColumn(11).setMaxWidth(160);
        repairsTable.getColumnModel().getColumn(11).setMinWidth(130);
        
        repairsTable.getColumnModel().getColumn(11).setCellRenderer(new ButtonRenderer());
        repairsTable.getColumnModel().getColumn(11).setCellEditor(new ButtonEditor(new JCheckBox()));
        repairsTable.getSelectionModel().addListSelectionListener(e -> updateActionButtonState());
        updateActionButtonState();
    }
    
    private void updateActionButtonState() {
        if (printReceiptButton == null || repairsTable == null) {
            return;
        }
        int viewRow = repairsTable.getSelectedRow();
        if (viewRow < 0) {
            printReceiptButton.setEnabled(false);
            return;
        }
        int modelRow = repairsTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            printReceiptButton.setEnabled(false);
            return;
        }
        String paymentStatus = String.valueOf(model.getValueAt(modelRow, 8));
        String displayStatus = String.valueOf(model.getValueAt(modelRow, 10));
        printReceiptButton.setEnabled(
            "Paid".equalsIgnoreCase(paymentStatus) || "Completed".equalsIgnoreCase(displayStatus)
        );
    }
    
    private void loadRepairs() {
        String query = "SELECT receipt_number, customer_name, customer_phone, phone_brand, phone_model, " +
                      "repair_type, agreed_amount, amount_paid, balance_due, payment_status, date_received, status " +
                      "FROM phone_repairs ORDER BY date_received DESC";
        try {
            pst = conn.prepareStatement(query);
            rst = pst.executeQuery();
            DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
            model.setRowCount(0);
            
            while (rst.next()) {
                String receiptNumber = rst.getString("receipt_number");
                String customerNameValue = rst.getString("customer_name");
                String phone = rst.getString("customer_phone");
                String device = rst.getString("phone_brand") + " " + rst.getString("phone_model");
                String repairTypeValue = rst.getString("repair_type");
                double agreed = rst.getDouble("agreed_amount");
                double paid = rst.getDouble("amount_paid");
                double balance = rst.getDouble("balance_due");
                String paymentStatus = rst.getString("payment_status");
                String date = rst.getString("date_received");
                String status = rst.getString("status");
                String displayStatus = computeDisplayStatus(status, paymentStatus, balance);
                
                Object[] row = {
                    receiptNumber,
                    customerNameValue,
                    phone,
                    device,
                    repairTypeValue,
                    String.format("%,.0f", agreed),
                    String.format("%,.0f", paid),
                    String.format("%,.0f", balance),
                    paymentStatus,
                    date.substring(0, 10),
                    displayStatus,
                    ""
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error loading repairs: " + ex.getMessage());
        }
    }
    
    private void saveRepair() {
        if (!validateForm()) {
            return;
        }
        
        String receiptNumber = generateReceiptNumber();
        String insertQuery = "INSERT INTO phone_repairs (receipt_number, customer_name, customer_phone, phone_brand, " +
                           "phone_model, phone_color, repair_type, agreed_amount, amount_paid, balance_due, payment_status, date_received, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'Pending')";
        
        try {
            pst = conn.prepareStatement(insertQuery);
            pst.setString(1, receiptNumber);
            pst.setString(2, customerName.getText());
            pst.setString(3, customerPhone.getText());
            pst.setString(4, phoneBrand.getText());
            pst.setString(5, phoneModel.getText());
            pst.setString(6, (String) phoneColor.getSelectedItem());
            pst.setString(7, (String) repairType.getSelectedItem());
            pst.setDouble(8, (Double) agreedAmount.getValue());
            pst.setDouble(9, 0.0);
            pst.setDouble(10, (Double) agreedAmount.getValue());
            pst.setString(11, "Not Paid");
            
            pst.executeUpdate();
            
            lastReceiptNumber = receiptNumber;
            receiptNumberLabel.setText(receiptNumber);
            receiptStatusLabel.setText("✓ Saved - Printing receipt...");
            
            // Show success with receipt number
            JOptionPane.showMessageDialog(this, 
                "✓ Repair recorded successfully!\n\nReceipt: " + receiptNumber + 
                "\n\nPrinting receipt for customer...", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear form before printing
            clearForm();
            loadRepairs();
            
            // Auto-print receipt
            final String finalReceiptNumber = receiptNumber;
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(500);
                    printReceiptByNumber(finalReceiptNumber);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error saving repair: " + ex.getMessage());
        }
    }
    
    private void printReceipt() {
        String receiptNumber = getSelectedReceiptNumber();
        if (receiptNumber == null || receiptNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a repair to print receipt", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        printReceiptByNumber(receiptNumber);
    }
    
    private int getSelectedModelRow() {
        int viewRow = repairsTable.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        int modelRow = repairsTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            return -1;
        }
        return modelRow;
    }
    
    private String getSelectedReceiptNumber() {
        int modelRow = getSelectedModelRow();
        if (modelRow < 0) {
            return null;
        }
        DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
        return String.valueOf(model.getValueAt(modelRow, 0));
    }
    
private void printReceiptByNumber(String receiptNumber) {

    try {

        if (conn == null || conn.isClosed()) {
            showError("Database connection is not available.");
            return;
        }

        // Validate receipt number
        if (receiptNumber == null || receiptNumber.trim().isEmpty()) {
            showError("Invalid receipt number.");
            return;
        }

        // Locate JRXML report
        java.io.File reportFile = resolveReportFile();

        if (reportFile == null) {

            showError(
                "Receipt report file not found.\n\n" +
                "Expected:\n" +
                "phone_repair_receipt.jrxml"
            );

            return;
        }

        System.out.println("[Receipt] Loading template from: "
                + reportFile.getAbsolutePath());

        // Load JRXML
        JasperDesign jdesign = JRXmlLoader.load(reportFile);

        // Compile report using the report template's own parameterized SQL.
        JasperReport jreport =
                JasperCompileManager.compileReport(jdesign);

        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("receipt_number", receiptNumber.trim());

        System.out.println("[Receipt] Filling report for receipt: " + receiptNumber.trim());

        JasperPrint jprint =
                JasperFillManager.fillReport(
                        jreport,
                        params,
                        conn
                );

        System.out.println("[Receipt] Pages: "
                + jprint.getPages().size());

        // Ensure data exists
        if (jprint.getPages().isEmpty()) {
            showError(
                    "Receipt generated but contains no data.\n\n" +
                    "Receipt #: " + receiptNumber
            );
            return;
        }

        // Open viewer
        JasperViewer viewer =
                new JasperViewer(jprint, false);
        viewer.setVisible(true);

    } catch (Exception ex) {

        ex.printStackTrace();

        Throwable root = getRootCause(ex);

        Logger.getLogger(PhoneRepair.class.getName())
                .log(Level.SEVERE, null, ex);

        showError(
                "Error generating receipt.\n\n" +
                "Cause: " + root.getMessage()
        );
    }
}

    /**
     * Tries to find the .jrxml in several locations and returns the first
     * {@link java.io.File} that exists, or null if none are found.
     */
    private java.io.File resolveReportFile() {
        System.out.println("[Receipt] Working directory: " + new java.io.File(".").getAbsolutePath());

        // 1. Classpath-based resolution (works when resources are copied into build/classes)
        java.net.URL resource = getClass().getResource("/reports/phone_repair_receipt.jrxml");
        if (resource != null) {
            try {
                java.io.File resourceFile = new java.io.File(resource.toURI());
                if (resourceFile.exists()) {
                    System.out.println("[Receipt] Loaded from classpath: /reports/phone_repair_receipt.jrxml");
                    return resourceFile;
                }
            } catch (Exception ignore) {
                // Ignore and continue to filesystem search.
            }
        }

        // 2. Root classpath fallback
        resource = getClass().getResource("/phone_repair_receipt.jrxml");
        if (resource != null) {
            try {
                java.io.File resourceFile = new java.io.File(resource.toURI());
                if (resourceFile.exists()) {
                    System.out.println("[Receipt] Loaded from classpath root: /phone_repair_receipt.jrxml");
                    return resourceFile;
                }
            } catch (Exception ignore) {
                // Ignore and continue to filesystem search.
            }
        }

        // 3. Search from current directory and its parents (handles IDE/launcher differences)
        java.io.File current = new java.io.File(".").getAbsoluteFile();
        java.io.File[] searchRoots = new java.io.File[] {
            current,
            current.getParentFile()
        };

        for (java.io.File root : searchRoots) {
            if (root == null) {
                continue;
            }
            java.io.File candidate = new java.io.File(root, "src/reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }

            candidate = new java.io.File(root, "reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }

            candidate = new java.io.File(root, "build/classes/reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }
        }

        // 4. Last resort: search upwards from the current directory until the filesystem root.
        java.io.File walk = current;
        while (walk != null) {
            java.io.File candidate = new java.io.File(walk, "src/reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }
            candidate = new java.io.File(walk, "reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }
            candidate = new java.io.File(walk, "build/classes/reports/phone_repair_receipt.jrxml");
            if (candidate.exists()) {
                System.out.println("[Receipt] Loaded from file: " + candidate.getAbsolutePath());
                return candidate;
            }
            walk = walk.getParentFile();
        }

        return null;
    }

    /** Walks the cause chain to find the real root exception. */
    private Throwable getRootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
    
    private void markCompleted() {
        String receiptNumber = getSelectedReceiptNumber();
        if (receiptNumber == null) {
            showWarning("Please select a repair to confirm completion");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Mark this service as completed for receipt # " + receiptNumber + "?",
            "Confirm Service Completion",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            String checkQuery = "SELECT agreed_amount, amount_paid, balance_due FROM phone_repairs WHERE receipt_number = ?";
            pst = conn.prepareStatement(checkQuery);
            pst.setString(1, receiptNumber);
            rst = pst.executeQuery();
            
            if (!rst.next()) {
                showError("Receipt not found.");
                return;
            }
            
            double balanceDue = rst.getDouble("balance_due");
            String updateQuery = "UPDATE phone_repairs SET status = 'Completed', date_completed = NOW(), updated_at = NOW() WHERE receipt_number = ?";
            pst = conn.prepareStatement(updateQuery);
            pst.setString(1, receiptNumber);
            pst.executeUpdate();
            
            if (balanceDue <= 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "✓ Service completed and payment is fully settled.\n\nReceipt: " + receiptNumber,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "✓ Service completed successfully.\n\nReceipt: " + receiptNumber +
                    "\nBalance still due: " + String.format("%.0f", balanceDue) + " UGX",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            
            loadRepairs();
            printReceiptByNumber(receiptNumber);
            
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error updating repair: " + ex.getMessage());
        }
    }
    
    private void recordPayment() {
        String receiptNumber = getSelectedReceiptNumber();
        if (receiptNumber == null) {
            showWarning("Please select a repair to record payment");
            return;
        }
        
        int modelRow = repairsTable.convertRowIndexToModel(repairsTable.getSelectedRow());
        DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
        String customer = (String) model.getValueAt(modelRow, 1);
        String phone = (String) model.getValueAt(modelRow, 2);
        String device = (String) model.getValueAt(modelRow, 3);
        String repairTypeValue = (String) model.getValueAt(modelRow, 4);
        double agreedAmt = Double.parseDouble(((String) model.getValueAt(modelRow, 5)).replace(",", ""));
        double amountPaid = Double.parseDouble(((String) model.getValueAt(modelRow, 6)).replace(",", ""));
        double balanceDue = Double.parseDouble(((String) model.getValueAt(modelRow, 7)).replace(",", ""));
        String paymentStatus = (String) model.getValueAt(modelRow, 8);
        String serviceStatus = (String) model.getValueAt(modelRow, 10);
        
        showPaymentDialog(receiptNumber, customer, phone, device, repairTypeValue,
                agreedAmt, amountPaid, balanceDue, paymentStatus, serviceStatus);
    }
    
    private void showPaymentDialog(String receiptNumber, String customer, String phone, String device,
                                   String repairTypeValue, double agreedAmt, double amountPaid,
                                   double balanceDue, String paymentStatus, String serviceStatus) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Record Payment");
        dialog.setModal(true);
        dialog.setLayout(new GridBagLayout());
        dialog.setResizable(false);
        dialog.setSize(520, 420);
        
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(6, 8, 6, 8);
        dgbc.fill = GridBagConstraints.HORIZONTAL;
        dgbc.anchor = GridBagConstraints.WEST;
        
        JLabel title = new JLabel("Payment Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY_COLOR);
        dgbc.gridx = 0;
        dgbc.gridy = 0;
        dgbc.gridwidth = 2;
        dialog.add(title, dgbc);
        
        String[] labels = {
            "Receipt:", "Customer:", "Phone:", "Device:", "Repair Type:",
            "Agreed Amount:", "Already Paid:", "Balance Due:", "Payment Status:",
            "New Payment:"
        };
        
        JTextField receiptField    = new JTextField(receiptNumber);
        JTextField customerField   = new JTextField(customer);
        JTextField phoneField      = new JTextField(phone);
        JTextField deviceField     = new JTextField(device);
        JTextField typeField       = new JTextField(repairTypeValue);
        JTextField agreedField     = new JTextField(String.format("%,.0f", agreedAmt));
        JTextField paidField       = new JTextField(String.format("%,.0f", amountPaid));
        JTextField balanceField    = new JTextField(String.format("%,.0f", balanceDue));
        JTextField statusField     = new JTextField(paymentStatus);
        JSpinner paymentInput      = new JSpinner(new SpinnerNumberModel(
                Math.max(0, (int) balanceDue), 0, (int) Math.max(agreedAmt, balanceDue), 1000));
        
        JTextField[] fields = {receiptField, customerField, phoneField, deviceField, typeField,
            agreedField, paidField, balanceField, statusField};
        for (JTextField f : fields) {
            f.setEditable(false);
            f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        paymentInput.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        int row = 1;
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dgbc.gridx = 0;
            dgbc.gridy = row;
            dgbc.gridwidth = 1;
            dialog.add(label, dgbc);
            
            JComponent comp;
            if      (i == 0) comp = receiptField;
            else if (i == 1) comp = customerField;
            else if (i == 2) comp = phoneField;
            else if (i == 3) comp = deviceField;
            else if (i == 4) comp = typeField;
            else if (i == 5) comp = agreedField;
            else if (i == 6) comp = paidField;
            else if (i == 7) comp = balanceField;
            else if (i == 8) comp = statusField;
            else             comp = paymentInput;

            dgbc.gridx = 1;
            dialog.add(comp, dgbc);
            row++;
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        JButton saveBtn = new JButton("Save Payment");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(SUCCESS_COLOR);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(TEXT_SECONDARY);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dgbc.gridx = 0;
        dgbc.gridy = row;
        dgbc.gridwidth = 2;
        dialog.add(buttonPanel, dgbc);
        
        saveBtn.addActionListener(e -> {
            double paymentAmount = ((Number) paymentInput.getValue()).doubleValue();
            if (paymentAmount <= 0) {
                showWarning("Payment amount must be greater than 0.");
                return;
            }
            if (paymentAmount > balanceDue) {
                showWarning("Payment amount cannot exceed the remaining balance.");
                return;
            }
            dialog.dispose();
            updatePaymentRecord(receiptNumber, paymentAmount, serviceStatus);
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void updatePaymentRecord(String receiptNumber, double paymentAmount, String serviceStatus) {
        try {
            String selectQuery = "SELECT agreed_amount, amount_paid, balance_due, payment_status FROM phone_repairs WHERE receipt_number = ?";
            pst = conn.prepareStatement(selectQuery);
            pst.setString(1, receiptNumber);
            rst = pst.executeQuery();
            if (!rst.next()) {
                showError("Receipt not found.");
                return;
            }
            
            double agreedAmt   = rst.getDouble("agreed_amount");
            double amountPaid  = rst.getDouble("amount_paid");
            double balanceDue  = rst.getDouble("balance_due");
            
            if (paymentAmount > balanceDue) {
                showWarning("Payment amount cannot exceed the remaining balance.");
                return;
            }
            
            double newAmountPaid  = amountPaid + paymentAmount;
            double newBalanceDue  = Math.max(0, agreedAmt - newAmountPaid);
            String newPaymentStatus = newBalanceDue <= 0 ? "Paid" : (newAmountPaid > 0 ? "Partial" : "Not Paid");
            
            if (newBalanceDue <= 0) {
                String updatePaymentQuery = "UPDATE phone_repairs SET amount_paid = ?, balance_due = ?, payment_status = ?, status = 'Completed', date_completed = NOW(), updated_at = NOW() WHERE receipt_number = ?";
                pst = conn.prepareStatement(updatePaymentQuery);
                pst.setDouble(1, newAmountPaid);
                pst.setDouble(2, newBalanceDue);
                pst.setString(3, newPaymentStatus);
                pst.setString(4, receiptNumber);
                pst.executeUpdate();
                
                loadRepairs();
                JOptionPane.showMessageDialog(this, "Payment completed successfully. You can now print the receipt.", 
                        "Payment Complete", JOptionPane.INFORMATION_MESSAGE);
                printReceiptByNumber(receiptNumber);
            } else {
                String updatePaymentQuery = "UPDATE phone_repairs SET amount_paid = ?, balance_due = ?, payment_status = ?, updated_at = NOW() WHERE receipt_number = ?";
                pst = conn.prepareStatement(updatePaymentQuery);
                pst.setDouble(1, newAmountPaid);
                pst.setDouble(2, newBalanceDue);
                pst.setString(3, newPaymentStatus);
                pst.setString(4, receiptNumber);
                pst.executeUpdate();
                
                loadRepairs();
                JOptionPane.showMessageDialog(this, "Payment recorded successfully.", 
                        "Payment Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error recording payment: " + ex.getMessage());
        }
    }
    
    private void deleteRepair() {
        String receiptNumber = getSelectedReceiptNumber();
        if (receiptNumber == null) {
            JOptionPane.showMessageDialog(this, "Please select a repair to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this repair record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            pst = conn.prepareStatement("DELETE FROM phone_repairs WHERE receipt_number = ?");
            pst.setString(1, receiptNumber);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Repair deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRepairs();
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error deleting repair: " + ex.getMessage());
        }
    }
    
    private void searchRepairs() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadRepairs();
            return;
        }
        
        String query = "SELECT receipt_number, customer_name, customer_phone, phone_brand, phone_model, " +
                      "repair_type, agreed_amount, date_received, status FROM phone_repairs " +
                      "WHERE customer_name ILIKE ? OR customer_phone ILIKE ? OR receipt_number ILIKE ? " +
                      "ORDER BY date_received DESC";
        
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, "%" + searchTerm + "%");
            pst.setString(2, "%" + searchTerm + "%");
            pst.setString(3, "%" + searchTerm + "%");
            rst = pst.executeQuery();
            
            DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
            model.setRowCount(0);
            
            while (rst.next()) {
                Object[] row = {
                    rst.getString("receipt_number"),
                    rst.getString("customer_name"),
                    rst.getString("customer_phone"),
                    rst.getString("phone_brand") + " " + rst.getString("phone_model"),
                    rst.getString("repair_type"),
                    String.format("%.0f", rst.getDouble("agreed_amount")),
                    rst.getString("date_received").substring(0, 10),
                    rst.getString("status")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PhoneRepair.class.getName()).log(Level.SEVERE, null, ex);
            showError("Error searching repairs: " + ex.getMessage());
        }
    }
    
    private void clearForm() {
        customerName.setText("");
        customerPhone.setText("");
        phoneBrand.setText("");
        phoneModel.setText("");
        phoneColor.setSelectedIndex(0);
        repairType.setSelectedIndex(0);
        agreedAmount.setValue(0.0);
        receiptNumberLabel.setText("AUTO-GENERATED");
        receiptStatusLabel.setText("Ready for new repair");
    }
    
    private boolean validateForm() {
        if (customerName.getText().trim().isEmpty()) {
            showWarning("Please enter customer name");
            return false;
        }
        if (customerPhone.getText().trim().isEmpty()) {
            showWarning("Please enter customer phone number");
            return false;
        }
        if (phoneBrand.getText().trim().isEmpty()) {
            showWarning("Please enter phone brand");
            return false;
        }
        if (phoneModel.getText().trim().isEmpty()) {
            showWarning("Please enter phone model");
            return false;
        }
        if ((Double) agreedAmount.getValue() <= 0) {
            showWarning("Please enter agreed amount greater than 0");
            return false;
        }
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
    
    private String generateReceiptNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PR-" + timestamp;
    }
    
    private String computeDisplayStatus(String dbStatus, String paymentStatus, double balanceDue) {
        if ("Completed".equalsIgnoreCase(dbStatus) || balanceDue <= 0) {
            return "Completed";
        }
        if ("Not Paid".equalsIgnoreCase(paymentStatus) || "Unpaid".equalsIgnoreCase(paymentStatus)) {
            return "Unpaid";
        }
        return "Pending";
    }
    
    private Color getStatusColor(String status) {
        switch (status) {
            case "Completed": return new Color(232, 245, 233);
            case "Pending":   return new Color(255, 248, 225);
            case "Unpaid":    return new Color(255, 235, 238);
            default:          return Color.WHITE;
        }
    }
    
    private class RepairStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 10) {
                String status = String.valueOf(table.getValueAt(row, 10));
                setForeground(TEXT_PRIMARY);
                setBackground(getStatusColor(status));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
            return c;
        }
    }
    
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String paymentStatus = String.valueOf(table.getValueAt(row, 8));
            String displayStatus = String.valueOf(table.getValueAt(row, 10));
            if ("Paid".equalsIgnoreCase(paymentStatus) || "Completed".equalsIgnoreCase(displayStatus)) {
                setText("Print Receipt");
                setEnabled(true);
            } else {
                setText("");
                setEnabled(false);
            }
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private int modelRow = -1;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.addActionListener(e -> {
                if (modelRow >= 0) {
                    DefaultTableModel model = (DefaultTableModel) repairsTable.getModel();
                    if (modelRow >= 0 && modelRow < model.getRowCount()) {
                        String receiptNumber = String.valueOf(model.getValueAt(modelRow, 0));
                        printReceiptByNumber(receiptNumber);
                    }
                }
                fireEditingStopped();
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            int viewRow = row;
            modelRow = table.convertRowIndexToModel(viewRow);
            String paymentStatus = String.valueOf(table.getValueAt(viewRow, 8));
            String displayStatus = String.valueOf(table.getValueAt(viewRow, 10));
            if ("Paid".equalsIgnoreCase(paymentStatus) || "Completed".equalsIgnoreCase(displayStatus)) {
                button.setText("Print Receipt");
                button.setEnabled(true);
            } else {
                button.setText("");
                button.setEnabled(false);
            }
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}