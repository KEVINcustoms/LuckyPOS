/*
 * Responsive Sales Admin Panel using MigLayout
 * Best layout manager for responsive Java Swing applications
 */

package com.nexatek;

import static com.nexatek.counter.cash;
import static com.nexatek.counter.customer_name;
import static com.nexatek.counter.telephone_number;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import static java.lang.Thread.sleep;
import java.sql.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.Component;

/**
 * Responsive Sales Admin Panel
 * Uses MigLayout for flexible, responsive design
 * @author mrrobot / KEVINcustoms
 */
public class sales_admin extends javax.swing.JPanel {
 Connection conn;
 ResultSet rst;
 PreparedStatement pst;
 PreparedStatement pstInsert;
 PreparedStatement pstDelete;
 public Double Stcok_qty = 0.0;
 static String customer_name;
  static String telephone_number;
  static Float cash;
  String usertext;

    // UI Components
    private JPanel headerPanel;
    private JPanel inputPanel;
    private JPanel productsPanel;
    private JPanel cartPanel;
    private JPanel summaryPanel;
    private JPanel actionsPanel;
    
    // Header components
    private JLabel receiptLabel;
    private JLabel invoice_no;
    private JComboBox<String> selectcombo;
    private JLabel stockQtyLabel;
    private JLabel stock_qty;
    private JTextField counter;
    private JTextField date;
    private JTextField time;
    
    // Input components
    private JLabel barcodeLabel;
    private JTextField barcode;
    private JLabel nameLabel;
    private JTextField name;
    private JLabel priceLabel;
    private JTextField price;
    private JLabel sizeLabel;
    private JTextField size;
    private JLabel quantityLabel;
    private JTextField quantity;
    private JLabel idLabel;
    private JTextField id;
    
    // Tables
    private JTable products_table;
    private JTable items;
    
    // Summary components
    private JLabel totalLabel;
    private JTextField total_amount;
    private JLabel changeLabel;
    private JTextField change;
    
    // Action buttons
    private JButton add_to_cart;
    private JButton remove;
    private JButton removeall;
    private JButton jButton1; // Statistics
    private JLabel cashpaid;
    private JButton finish;
 
     public sales_admin(String user) {
         this.usertext = user;
        conn = connection.connect();
        initComponents();
        setupTableHeaders();
        counter.setText(usertext);
        currentdate();
        calculateTotal();
        InvoiceNumbers();   
        Update_table();
        
        // Set sizes to fill available space
        setMinimumSize(new Dimension(800, 500));
        setPreferredSize(new Dimension(1000, 600));
    }

    private void initComponents() {
        // Use BorderLayout with scroll pane
        setLayout(new java.awt.BorderLayout());
        
        createHeaderPanel();
        createInputPanel();
        createProductsTablePanel();
        createCartAndSummaryPanel();
        createActionsPanel();
        
        // Create content panel with ALL components
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // Align all panels left
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        productsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Set sizes - balanced to fit all components
        headerPanel.setPreferredSize(new Dimension(900, 40));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        inputPanel.setPreferredSize(new Dimension(900, 70));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        productsPanel.setPreferredSize(new Dimension(900, 240));
        productsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        
        cartPanel.setPreferredSize(new Dimension(900, 155));
        cartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        
        actionsPanel.setPreferredSize(new Dimension(900, 55));
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        
        // Add panels with minimal gaps
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(productsPanel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(cartPanel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(actionsPanel);
        
        // Scroll pane with ALWAYS visible scrollbar
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0)); // Wider scrollbar
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private void createHeaderPanel() {
        // Single row compact header using FlowLayout
        headerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        headerPanel.setBackground(new Color(248, 248, 248));
        
        receiptLabel = new JLabel("Receipt#:");
        receiptLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        invoice_no = new JLabel("0");
        invoice_no.setFont(new Font("Segoe UI", Font.BOLD, 16));
        invoice_no.setForeground(new Color(0, 102, 0));
        
        selectcombo = new JComboBox<>(new String[]{"Select Prices"});
        selectcombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        selectcombo.setPreferredSize(new Dimension(100, 25));
        selectcombo.addActionListener(e -> selectcomboActionPerformed());
        
        counter = new JTextField(12);
        counter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        counter.setEditable(false);
        counter.setBackground(new Color(240, 255, 240));
        
        date = new JTextField(10);
        date.setFont(new Font("Segoe UI", Font.BOLD, 11));
        date.setEditable(false);
        date.setHorizontalAlignment(JTextField.CENTER);
        date.setBackground(new Color(240, 248, 255));
        
        time = new JTextField(8);
        time.setFont(new Font("Segoe UI", Font.BOLD, 11));
        time.setEditable(false);
        time.setHorizontalAlignment(JTextField.CENTER);
        time.setBackground(new Color(255, 250, 240));
        
        stockQtyLabel = new JLabel("Stock:");
        stockQtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        stock_qty = new JLabel("0");
        stock_qty.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stock_qty.setForeground(new Color(0, 102, 204));
        
        headerPanel.add(receiptLabel);
        headerPanel.add(invoice_no);
        headerPanel.add(selectcombo);
        headerPanel.add(counter);
        headerPanel.add(date);
        headerPanel.add(time);
        headerPanel.add(stockQtyLabel);
        headerPanel.add(stock_qty);
    }

    private void createInputPanel() {
        // Use GridLayout for simple 2-row input
        inputPanel = new JPanel(new java.awt.GridLayout(2, 6, 5, 3));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Product Details", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 10), new Color(0, 102, 0)));
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 10);
        LineBorder cyanBorder = new LineBorder(new Color(0, 180, 180), 1, true);
        Font fieldFont = new Font("Segoe UI", Font.BOLD, 12);
        
        barcodeLabel = new JLabel("Barcode:");
        barcodeLabel.setFont(labelFont);
        barcodeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        barcode = new JTextField();
        barcode.setFont(fieldFont);
        barcode.setBorder(cyanBorder);
        barcode.setBackground(new Color(255, 255, 240));
        barcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                barcodeKeyPressed(evt);
            }
        });
        
        nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        name = new JTextField();
        name.setFont(fieldFont);
        name.setBorder(cyanBorder);
        name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameKeyTyped(evt);
            }
        });
        
        quantityLabel = new JLabel("Qty:");
        quantityLabel.setFont(labelFont);
        quantityLabel.setHorizontalAlignment(SwingConstants.RIGHT);  // Align right to be closer to field
        
        quantity = new JTextField();
        quantity.setFont(fieldFont);
        quantity.setBorder(cyanBorder);
        quantity.setBackground(new Color(240, 255, 240));
        quantity.setHorizontalAlignment(JTextField.CENTER);
        
        priceLabel = new JLabel("Price:");
        priceLabel.setFont(labelFont);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        price = new JTextField();
        price.setFont(fieldFont);
        price.setBorder(cyanBorder);
        price.setBackground(new Color(255, 255, 240));
        price.setHorizontalAlignment(JTextField.RIGHT);
        
        sizeLabel = new JLabel("Size:");
        sizeLabel.setFont(labelFont);
        sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        size = new JTextField();
        size.setFont(fieldFont);
        size.setBorder(cyanBorder);
        size.setEditable(false);
        size.setBackground(new Color(245, 245, 245));
        
        idLabel = new JLabel("ID:");
        idLabel.setFont(labelFont);
        idLabel.setHorizontalAlignment(SwingConstants.RIGHT);  // Align right to be closer to field
        
        id = new JTextField();
        id.setFont(fieldFont);
        id.setBorder(cyanBorder);
        id.setEditable(false);
        id.setBackground(new Color(245, 245, 245));
        
        // Row 1: Barcode, Name, Quantity
        inputPanel.add(barcodeLabel);
        inputPanel.add(barcode);
        inputPanel.add(nameLabel);
        inputPanel.add(name);
        inputPanel.add(quantityLabel);
        inputPanel.add(quantity);
        
        // Row 2: Price, Size, ID
        inputPanel.add(priceLabel);
        inputPanel.add(price);
        inputPanel.add(sizeLabel);
        inputPanel.add(size);
        inputPanel.add(idLabel);
        inputPanel.add(id);
    }

    private void createProductsTablePanel() {
        productsPanel = new JPanel(new java.awt.BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Products (Click to Select)", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 10), new Color(0, 102, 0)));
        
        products_table = new JTable();
        products_table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        products_table.setRowHeight(25);  // Better row height
        products_table.setSelectionBackground(new Color(0, 153, 0));
        products_table.setSelectionForeground(Color.WHITE);
        products_table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        products_table.getTableHeader().setBackground(new Color(242, 242, 242));
        products_table.getTableHeader().setForeground(new Color(0, 0, 255));
        products_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        products_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                products_tableMouseClicked(evt);
            }
        });
        
        JScrollPane productsScroll = new JScrollPane(products_table);
        productsPanel.add(productsScroll, java.awt.BorderLayout.CENTER);
    }

    private void createCartAndSummaryPanel() {
        // Simple horizontal split - cart on left, summary on right
        cartPanel = new JPanel(new java.awt.BorderLayout(5, 0));
        
        // Cart table - compact
        JPanel cartTablePanel = new JPanel(new java.awt.BorderLayout());
        cartTablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Cart", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 10), new Color(0, 102, 0)));
        
        items = new JTable();
        items.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        items.setRowHeight(24);  // Better row height
        items.setSelectionBackground(new Color(51, 255, 51));
        items.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        items.getTableHeader().setBackground(new Color(242, 242, 242));
        items.getTableHeader().setForeground(new Color(0, 0, 255));
        items.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Initialize cart table model
        DefaultTableModel model = new DefaultTableModel();
        items.setModel(model);
        model.addColumn("ID");
        model.addColumn("BARCODE");
        model.addColumn("NAME");
        model.addColumn("QTY");
        model.addColumn("PRICE");
        model.addColumn("TOTAL");
        
        JScrollPane cartScroll = new JScrollPane(items);
        cartTablePanel.add(cartScroll, java.awt.BorderLayout.CENTER);
        
        // Summary panel - compact to fit
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Summary", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 10), new Color(0, 102, 0)));
        summaryPanel.setBackground(new Color(250, 250, 250));
        summaryPanel.setPreferredSize(new Dimension(180, 110));
        
        totalLabel = new JLabel("Total Amount:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        total_amount = new JTextField("00.00");
        total_amount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        total_amount.setEditable(false);
        total_amount.setHorizontalAlignment(JTextField.RIGHT);
        total_amount.setForeground(new Color(0, 102, 0));
        total_amount.setBackground(Color.WHITE);
        total_amount.setBorder(new LineBorder(new Color(0, 102, 0), 2, true));
        total_amount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        total_amount.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        changeLabel = new JLabel("Balance/Due:");
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        change = new JTextField("00.00");
        change.setFont(new Font("Segoe UI", Font.BOLD, 20));
        change.setEditable(false);
        change.setHorizontalAlignment(JTextField.RIGHT);
        change.setForeground(new Color(204, 0, 0));
        change.setBackground(new Color(255, 245, 245));
        change.setBorder(new LineBorder(new Color(204, 0, 0), 2, true));
        change.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        change.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        summaryPanel.add(Box.createVerticalStrut(3));
        summaryPanel.add(totalLabel);
        summaryPanel.add(total_amount);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(changeLabel);
        summaryPanel.add(change);
        summaryPanel.add(Box.createVerticalGlue());
        
        cartPanel.add(cartTablePanel, java.awt.BorderLayout.CENTER);
        cartPanel.add(summaryPanel, java.awt.BorderLayout.EAST);
    }

    private void createActionsPanel() {
        // Simple GridLayout for action buttons
        actionsPanel = new JPanel(new java.awt.GridLayout(1, 6, 6, 0));
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        actionsPanel.setBackground(new Color(235, 235, 235));
        
        Font btnFont = new Font("Segoe UI", Font.BOLD, 12);
        
        add_to_cart = new JButton("Add to Cart");
        add_to_cart.setFont(btnFont);
        add_to_cart.setBackground(new Color(0, 102, 0));
        add_to_cart.setForeground(Color.WHITE);
        add_to_cart.setFocusPainted(false);
        add_to_cart.setOpaque(true);
        add_to_cart.setBorderPainted(true);
        add_to_cart.addActionListener(e -> add_to_cartActionPerformed());
        
        remove = new JButton("Remove");
        remove.setFont(btnFont);
        remove.setBackground(new Color(255, 51, 102));
        remove.setForeground(Color.WHITE);
        remove.setFocusPainted(false);
        remove.setOpaque(true);
        remove.setBorderPainted(true);
        remove.addActionListener(e -> removeActionPerformed());
        
        removeall = new JButton("Clear All");
        removeall.setFont(btnFont);
        removeall.setBackground(new Color(153, 0, 0));
        removeall.setForeground(Color.WHITE);
        removeall.setFocusPainted(false);
        removeall.setOpaque(true);
        removeall.setBorderPainted(true);
        removeall.addActionListener(e -> removeallActionPerformed());
        
        jButton1 = new JButton("Stats");
        jButton1.setFont(btnFont);
        jButton1.setBackground(new Color(70, 130, 180));
        jButton1.setForeground(Color.WHITE);
        jButton1.setFocusPainted(false);
        jButton1.setOpaque(true);
        jButton1.setBorderPainted(true);
        jButton1.addActionListener(e -> jButton1ActionPerformed());
        
        cashpaid = new JLabel("Paid Amt", SwingConstants.CENTER);
        cashpaid.setFont(btnFont);
        cashpaid.setOpaque(true);
        cashpaid.setBackground(new Color(255, 204, 0));
        cashpaid.setBorder(BorderFactory.createRaisedBevelBorder());
        cashpaid.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cashpaid.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cashpaidMouseClicked();
            }
        });
        
        finish = new JButton("PAY");
        finish.setFont(new Font("Segoe UI", Font.BOLD, 18));
        finish.setBackground(new Color(0, 153, 76));
        finish.setForeground(Color.WHITE);
        finish.setFocusPainted(false);
        finish.setOpaque(true);
        finish.setBorderPainted(true);
        finish.setBorder(BorderFactory.createRaisedBevelBorder());
        try {
            finish.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/img/icons8-samsung-pay-56.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        finish.addActionListener(e -> finishActionPerformed());
        
        actionsPanel.add(add_to_cart);
        actionsPanel.add(remove);
        actionsPanel.add(removeall);
        actionsPanel.add(jButton1);
        actionsPanel.add(cashpaid);
        actionsPanel.add(finish);
    }

    private void setupTableHeaders() {
        items.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        items.getTableHeader().setOpaque(true);
        items.getTableHeader().setBackground(new Color(242, 242, 242));
        items.getTableHeader().setForeground(new Color(0, 0, 255));
    }

    // ==================== Business Logic Methods ====================
    
    private void Update_table() {
        try {
    String sql = "select * from products";
    pst = conn.prepareStatement(sql);
    rst = pst.executeQuery();
    products_table.setModel(DbUtils.resultSetToTableModel(rst));
        } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e);
    }
   }

public void InvoiceNumbers() {
        String sql = "SELECT MAX(invoice_number) FROM invoice_details";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int highestInvoiceNumber = rs.getInt(1);
                highestInvoiceNumber++;
                invoice_no.setText(String.valueOf(highestInvoiceNumber));
            } else {
                invoice_no.setText("1");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void updateCombo() {
        String sql = "select * from products where barcode = '" + barcode.getText() + "'";
        try {
          pst = conn.prepareStatement(sql);
          rst = pst.executeQuery();
            while (rst.next()) {
          selectcombo.addItem(rst.getString("price"));
          selectcombo.addItem(rst.getString("price2"));
          selectcombo.addItem(rst.getString("price3"));
          }
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "An error occurred!");
          }
}

    public void clear() {
        name.setText("");
        size.setText("");
        price.setText("");
        quantity.setText("");
        barcode.setText("");
        id.setText("");
        stock_qty.setText("");
        selectcombo.removeAllItems();
        selectcombo.addItem("Select Prices");
    }

 public double calculateTotal() {
    double total = 0.0;
    DefaultTableModel model = (DefaultTableModel) items.getModel();
    int rowCount = model.getRowCount();

    for (int i = 0; i < rowCount; i++) {
            String subTotalStr = (String) model.getValueAt(i, 5);
        try {
            double subTotal = Double.parseDouble(subTotalStr);
            total += subTotal;
            total_amount.setText(String.valueOf(total));
        } catch (NumberFormatException e) {
                e.printStackTrace();
        }
    }
    return total;
}

 public void view_receipt() {
    try {
        JasperDesign jdesign = JRXmlLoader.load("src\\reports\\receipt2.jrxml");
        String query = "select * from solditems where invoice_number = '" + invoice_no.getText() + "'";
        JRDesignQuery updateQuery = new JRDesignQuery();
        updateQuery.setText(query);
        jdesign.setQuery(updateQuery);

        JasperReport jreport = JasperCompileManager.compileReport(jdesign);
        JasperPrint jprint = JasperFillManager.fillReport(jreport, null, conn);

        JFrame frame = new JFrame("KEVINcustoms Receipt Viewer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JasperViewer viewer = new JasperViewer(jprint, false);
        frame.getContentPane().add(viewer.getContentPane());
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    } catch (JRException ex) {
        Logger.getLogger(sales_admin.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    public void stkup() {
 DefaultTableModel dt = (DefaultTableModel) items.getModel();
 int rc = dt.getRowCount();
        for (int i = 0; i < rc; i++) {
            String bcode = dt.getValueAt(i, 0).toString();
            String sell_qty = dt.getValueAt(i, 3).toString();
     try {
         Statement s = connection.connect().createStatement();
                ResultSet rs = s.executeQuery("select quantity from products where productid = '" + bcode + "'");
                if (rs.next()) {
         Stcok_qty = Double.valueOf(rs.getString("quantity"));
         }
     } catch (SQLException e) {
         System.out.println(e);
     }
     Double st_qty = Stcok_qty;
     Double sel_qty = Double.valueOf(sell_qty);
     Double new_qty = st_qty - sel_qty;
     String nqty = String.valueOf(new_qty);
            try {
     Statement ss = connection.connect().createStatement();
                ss.executeUpdate("update products set quantity = '" + nqty + "' where productid = '" + bcode + "'");
            } catch (Exception e) {
         System.out.println(e);
     }
 }
 }

  public void currentdate() {
        Thread clock = new Thread() {
            public void run() {
                for (;;) {
                    Calendar cal = new GregorianCalendar();
                    int month = cal.get(Calendar.MONTH);
                    int year = cal.get(Calendar.YEAR);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    date.setText(" " + year + "/" + (month + 1) + "/" + day);

                    int second = cal.get(Calendar.SECOND);
                    int minute = cal.get(Calendar.MINUTE);
                    int hour = cal.get(Calendar.HOUR);
                    time.setText(String.format(" %02d:%02d:%02d", hour, minute, second));
                    
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LOGIN.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }
            }
        };
        clock.start();
    }
  
   public void sendOutOfStockData() {
    String selectQuery = "SELECT * FROM products";
    try {
        pst = conn.prepareStatement(selectQuery);
        rst = pst.executeQuery();

        while (rst.next()) {
            int productid = Integer.parseInt(rst.getString("productid"));
            String barcode = rst.getString("barcode");
            String name = rst.getString("name");
            String size = rst.getString("size");
            String price = rst.getString("price");
            String price2 = rst.getString("price2");
            String price3 = rst.getString("price3");
            String quantity = rst.getString("quantity");
            String category = rst.getString("category");
            String supplier_id = rst.getString("supplier_id");
            String cost_price = rst.getString("cost_price");
            Float qty = Float.valueOf(quantity);
            Float prc = Float.valueOf(price);
            Float prc2 = Float.valueOf(price2);
            Float prc3 = Float.valueOf(price3);
            Float costp = Float.valueOf(cost_price);

                if ("0".equals(quantity)) {
                try {
                    String insertQuery = "INSERT INTO out_of_stock (productid,barcode, name, size, price, price2, price3, quantity, category, supplier_id, cost_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
                    pstInsert = conn.prepareStatement(insertQuery);
                    pstInsert.setInt(1, productid);
                        pstInsert.setString(2, barcode);
                        pstInsert.setString(3, name);
                        pstInsert.setString(4, size);
            pstInsert.setFloat(5, prc);
            pstInsert.setFloat(6, prc2);
            pstInsert.setFloat(7, prc3);
            pstInsert.setInt(8, Integer.parseInt(quantity));
            pstInsert.setString(9, category);
                        pstInsert.setInt(10, Integer.valueOf(supplier_id));
            pstInsert.setFloat(11, costp);
                    pstInsert.execute();
                    JOptionPane.showMessageDialog(null, "You have sold your last items and product is now out of stock \n KEVINcustoms is advising you to refill the stock");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
            }
        }
        Update_table();
    } catch (SQLException ex) {
        Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
                if (rst != null) rst.close();
                if (pst != null) pst.close();
                if (pstDelete != null) pstDelete.close();
                if (pstInsert != null) pstInsert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
   
    // ==================== Event Handlers ====================
    
    private void selectcomboActionPerformed() {
        if (selectcombo.getSelectedItem() != null) {
            price.setText(selectcombo.getSelectedItem().toString());
        }
    }

    private void nameKeyTyped(java.awt.event.KeyEvent evt) {
         String searchCriteria = name.getText().trim().toLowerCase();
    try {
    String sql = "SELECT * FROM products WHERE barcode LIKE ? OR name LIKE ? OR category LIKE ? OR size LIKE ?";
    pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + searchCriteria + "%");
    pst.setString(2, "%" + searchCriteria + "%");
    pst.setString(3, "%" + searchCriteria + "%");
    pst.setString(4, "%" + searchCriteria + "%");
    rst = pst.executeQuery();       
       products_table.setModel(DbUtils.resultSetToTableModel(rst));
} catch (SQLException e) {
    JOptionPane.showMessageDialog(null, e);
} 
    }

    private void products_tableMouseClicked(java.awt.event.MouseEvent evt) {
try {
    selectcombo.removeAllItems();
        DefaultTableModel t = (DefaultTableModel) products_table.getModel();
        int i = products_table.getSelectedRow();
        id.setText(t.getValueAt(i, 0).toString());
        barcode.setText(t.getValueAt(i, 1).toString());
        name.setText(t.getValueAt(i, 2).toString());
        size.setText(t.getValueAt(i, 3).toString());
        price.setText(t.getValueAt(i, 4).toString());
        quantity.setText("1");
        
            String sql = "select quantity from products where productid = '" + id.getText() + "'";
            pst = conn.prepareStatement(sql);
         rst = pst.executeQuery();
            if (rst.next()) {
             String quantity1 = rst.getString("quantity");
             stock_qty.setText(quantity1);
                }   
            
            selectcombo.addItem(t.getValueAt(i, 4).toString());
            selectcombo.addItem(t.getValueAt(i, 5).toString());
            selectcombo.addItem(t.getValueAt(i, 6).toString());
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    private void barcodeKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
     String brcode = barcode.getText().toLowerCase();
         try {
             Statement s = connection.connect().createStatement();
                ResultSet rs = s.executeQuery("select * from products where barcode = '" + brcode + "'");
                if (rs.next()) {
                  String ProductName = rs.getString("name");
             String retail_price = rs.getString("price");
             String productSize = rs.getString("size");
                    String qty = rs.getString("quantity");
             String prodId = rs.getString("productid");
                    stock_qty.setText(qty);
             price.setText(retail_price);
             name.setText(ProductName);
             size.setText(productSize);
             id.setText(prodId);
             updateCombo();
                } else {
             JOptionPane.showMessageDialog(this, "Barcode not Found");
             }
         } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeActionPerformed() {
        int selectedRowIndex = items.getSelectedRow();
        if (selectedRowIndex != -1) {
            DefaultTableModel model = (DefaultTableModel) items.getModel();
            model.removeRow(selectedRowIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
        }
        clear();
        calculateTotal();
    }

    private void removeallActionPerformed() {
        if (items.getRowCount() > 0) {
            DefaultTableModel model = (DefaultTableModel) items.getModel();
            model.setRowCount(0);
        } else {
            JOptionPane.showMessageDialog(this, "The table is already empty.", "Empty Table", JOptionPane.INFORMATION_MESSAGE);
        }
        clear();
    }

    private void finishActionPerformed() {
        Float totalamount = Float.valueOf(total_amount.getText());
        String Status = null;

        if (cash == null || cash == 0.0) {
            Status = "UnPaid";
        } else if (totalamount > cash) {
            Status = "Partial";
        } else if (totalamount <= cash) {
            Status = "Paid";
        }

        LocalDate currentDate = LocalDate.now();
        DefaultTableModel model = (DefaultTableModel) items.getModel();
        int rowCount = model.getRowCount();

        try {
            String sql = "INSERT INTO solditems (invoice_number, itemId, name, quantity, itemPrice, total,customer_name, customer_phone,sold_by,time,status,selldate,paid_amount,balanc) VALUES (?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?)";
            String invoiceDetailsSql = "INSERT INTO invoice_details (invoice_number) VALUES (?)";
            String customerSql = "insert into customers(customer_name,phone_number,invoice_number) values(?,?,?)";

            pst = conn.prepareStatement(sql);
            PreparedStatement invoiceDetailsPst = conn.prepareStatement(invoiceDetailsSql);
            PreparedStatement customerPst = conn.prepareStatement(customerSql);

            pst.setInt(1, Integer.valueOf(invoice_no.getText()));
            invoiceDetailsPst.setInt(1, Integer.valueOf(invoice_no.getText()));
            customerPst.setString(1, customer_name);
            customerPst.setString(2, telephone_number);
            customerPst.setInt(3, Integer.valueOf(invoice_no.getText()));

            for (int i = 0; i < rowCount; i++) {
                String tempid = model.getValueAt(i, 0).toString();
                int tempid2 = Integer.parseInt(tempid);
                pst.setInt(2, tempid2);
                pst.setString(3, model.getValueAt(i, 2).toString());
                pst.setInt(4, Integer.parseInt(model.getValueAt(i, 3).toString()));
                pst.setFloat(5, Float.parseFloat(model.getValueAt(i, 4).toString()));
                pst.setFloat(6, Float.valueOf(total_amount.getText()));
                pst.setString(7, customer_name);
                pst.setString(8, telephone_number);
                pst.setString(9, counter.getText());
                pst.setString(10, time.getText());
                pst.setString(11, Status);
                pst.setObject(12, currentDate);
                pst.setFloat(13, cash != null ? cash : 0f);
                pst.setFloat(14, Float.parseFloat(change.getText()));
                pst.executeUpdate();
            }
            view_receipt();
            stkup();
            sendOutOfStockData();
            invoiceDetailsPst.executeUpdate();
            customerPst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Transaction successful");
            cash = null;
            customer_name = null;
            telephone_number = null;
            model.setRowCount(0);
            total_amount.setText("00.00");
            change.setText("00.00");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        InvoiceNumbers();
    }

    private void cashpaidMouseClicked() {
        customer_name = JOptionPane.showInputDialog("Customer's Name: ");
        telephone_number = JOptionPane.showInputDialog("Phone Number: ");
        String cashInput = JOptionPane.showInputDialog("Enter Cash Brought!");
        if (cashInput != null && !cashInput.isEmpty()) {
            cash = Float.valueOf(cashInput);
            Float total_ = Float.valueOf(total_amount.getText());
            change.setText(String.valueOf(cash - total_));
        }
    }

    private void jButton1ActionPerformed() {
        this.setVisible(false);
        Statistics_table state = new Statistics_table();
        state.setVisible(true);
    }

    private void add_to_cartActionPerformed() {
        try {
        int stqty = Integer.parseInt(stock_qty.getText());
        int selqty = Integer.parseInt(quantity.getText());
        float priceField = Float.parseFloat(price.getText());
        int idforuse = Integer.parseInt(id.getText());

            if (selqty > 0) {
                if (selqty <= stqty) {
                String sql = "select price,price2,price3 from products where productid = ?";
                    pst = conn.prepareStatement(sql);
                    pst.setInt(1, idforuse);
                    rst = pst.executeQuery();
                    if (rst.next()) {
                        float price1 = Float.parseFloat(rst.getString("price").trim());
                        float price2 = Float.parseFloat(rst.getString("price2").trim());
                        float price3 = Float.parseFloat(rst.getString("price3").trim());
                        if (priceField <= price1 && priceField >= price3) {
                            String itemId = id.getText();
                            String itemBarcode = barcode.getText();
                            String itemName = name.getText();
                            String itemSize = size.getText();
                            String itemPrice = price.getText();
                            String itemQuantity = quantity.getText();

                            Float q = Float.valueOf(itemQuantity);
                            Float p = Float.valueOf(itemPrice);
                            Float t = q * p;
                            String sub_total = String.valueOf(t);

                            String itemNameWithSize = itemName + " (" + itemSize + ")";
                            String[] rowData = {itemId, itemBarcode, itemNameWithSize, itemQuantity, itemPrice, sub_total};

                            DefaultTableModel model = (DefaultTableModel) items.getModel();
                            model.addRow(rowData);
                            calculateTotal();
                            clear();
                        } else {
                            JOptionPane.showMessageDialog(null, "You must play within the given range! Please check the unit price and try again!");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "The quantity you have inserted is bigger or the product is out of stock");
                }
            } else {
                JOptionPane.showMessageDialog(null, "The value you have inserted is invalid please check the value and try again");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please fill in all required fields correctly.");
        } catch (SQLException ex) {
            Logger.getLogger(sales_admin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
