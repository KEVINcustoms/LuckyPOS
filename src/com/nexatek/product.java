/*
 * Responsive Product Management Panel
 */
package com.nexatek;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

/**
 * Responsive Product Management Panel
 * @author mrrobot
 */
public class product extends javax.swing.JPanel {

    Connection conn;
    ResultSet rst;
    PreparedStatement pst;
    PreparedStatement pstDelete;
    PreparedStatement pstInsert;
    PreparedStatement pstextra;
    PreparedStatement pstextra_extra;
    
    // UI Components
    private JPanel headerPanel;
    private JPanel inputPanel;
    private JPanel productsPanel;
    private JPanel damagedPanel;
    private JPanel actionsPanel;
    
    // Input fields
    private JTextField search_bar;
    private JTextField barcode;
    private JTextField product_name;
    private JTextField product_size;
    private JTextField price;
    private JTextField price2;
    private JTextField price3;
    private JTextField quantity;
    private JTextField category;
    private JTextField supplier_id;
    private JTextField cost_price;
    
    // Tables
    private JTable products_table;
    private JTable damaged;
    
    // Buttons
    private JButton save;
    private JButton update;
    private JButton delete;
    private JButton jButton2; // Search
    private JButton save_damage;
    private JButton view_damage;
    private JButton import_invoice;

    public product() {
        conn = connection.connect();
        initComponents();
        damaged.setVisible(true);
        Update_table();
        view_damaged();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        createHeaderPanel();
        createInputPanel();
        createProductsTablePanel();
        createDamagedPanel();
        createActionsPanel();
        
        // Create content panel with all components
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Align all panels
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        productsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        damagedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set sizes
        headerPanel.setPreferredSize(new Dimension(900, 50));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        inputPanel.setPreferredSize(new Dimension(900, 160));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        actionsPanel.setPreferredSize(new Dimension(900, 95));
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        productsPanel.setPreferredSize(new Dimension(900, 250));
        productsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        damagedPanel.setPreferredSize(new Dimension(900, 180));
        damagedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Add panels
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(actionsPanel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(productsPanel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(damagedPanel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createHeaderPanel() {
        headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Product Management",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 0)));
        headerPanel.setBackground(new Color(248, 250, 252));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        search_bar = new JTextField(20);
        search_bar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        search_bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 0), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        search_bar.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                search_barKeyTyped(evt);
            }
        });
        
        headerPanel.add(searchLabel);
        headerPanel.add(search_bar);
    }
    
    private void createInputPanel() {
        inputPanel = new JPanel(new GridLayout(4, 6, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Product Details",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), new Color(0, 102, 0)));
        inputPanel.setBackground(new Color(252, 252, 252));
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        Border fieldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 153), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8));
        
        // Row 1: Barcode, Name, Size
        JLabel barcodeLabel = new JLabel("Barcode:");
        barcodeLabel.setFont(labelFont);
        barcodeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        barcode = new JTextField();
        barcode.setFont(fieldFont);
        barcode.setBorder(fieldBorder);
        
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        product_name = new JTextField();
        product_name.setFont(fieldFont);
        product_name.setBorder(fieldBorder);
        
        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setFont(labelFont);
        sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        product_size = new JTextField();
        product_size.setFont(fieldFont);
        product_size.setBorder(fieldBorder);
        
        // Row 2: Retail Price, XL Price, XXL Price
        JLabel priceLabel = new JLabel("Retail Price:");
        priceLabel.setFont(labelFont);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        price = new JTextField();
        price.setFont(fieldFont);
        price.setBorder(fieldBorder);
        
        JLabel price2Label = new JLabel("XL Price:");
        price2Label.setFont(labelFont);
        price2Label.setHorizontalAlignment(SwingConstants.RIGHT);
        price2 = new JTextField();
        price2.setFont(fieldFont);
        price2.setBorder(fieldBorder);
        
        JLabel price3Label = new JLabel("XXL Price:");
        price3Label.setFont(labelFont);
        price3Label.setHorizontalAlignment(SwingConstants.RIGHT);
        price3 = new JTextField();
        price3.setFont(fieldFont);
        price3.setBorder(fieldBorder);
        
        // Row 3: Cost Price, Quantity, Category
        JLabel costLabel = new JLabel("Cost Price:");
        costLabel.setFont(labelFont);
        costLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cost_price = new JTextField();
        cost_price.setFont(fieldFont);
        cost_price.setBorder(fieldBorder);
        
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(labelFont);
        qtyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        quantity = new JTextField();
        quantity.setFont(fieldFont);
        quantity.setBorder(fieldBorder);
        quantity.setBackground(new Color(255, 255, 240));
        
        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(labelFont);
        catLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        category = new JTextField();
        category.setFont(fieldFont);
        category.setBorder(fieldBorder);
        
        // Row 4: Supplier ID + empty cells
        JLabel supplierLabel = new JLabel("Supplier ID:");
        supplierLabel.setFont(labelFont);
        supplierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        supplier_id = new JTextField();
        supplier_id.setFont(fieldFont);
        supplier_id.setBorder(fieldBorder);
        
        // Add components
        inputPanel.add(barcodeLabel); inputPanel.add(barcode);
        inputPanel.add(nameLabel); inputPanel.add(product_name);
        inputPanel.add(sizeLabel); inputPanel.add(product_size);
        
        inputPanel.add(priceLabel); inputPanel.add(price);
        inputPanel.add(price2Label); inputPanel.add(price2);
        inputPanel.add(price3Label); inputPanel.add(price3);
        
        inputPanel.add(costLabel); inputPanel.add(cost_price);
        inputPanel.add(qtyLabel); inputPanel.add(quantity);
        inputPanel.add(catLabel); inputPanel.add(category);
        
        inputPanel.add(supplierLabel); inputPanel.add(supplier_id);
        inputPanel.add(new JLabel("")); inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel("")); inputPanel.add(new JLabel(""));
    }
    
    private void createActionsPanel() {
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        actionsPanel.setBorder(BorderFactory.createEtchedBorder());
        actionsPanel.setBackground(new Color(245, 247, 250));
        
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);
        Dimension btnSize = new Dimension(140, 40);
        
        // Save button with icon
        save = new JButton("Save");
        save.setFont(btnFont);
        save.setBackground(new Color(0, 153, 255));
        save.setForeground(Color.WHITE);
        save.setPreferredSize(btnSize);
        save.setFocusPainted(false);
        try {
            save.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/img/save.png")));
        } catch (Exception e) {}
        save.addActionListener(e -> saveActionPerformed());
        
        // Search button with icon
        jButton2 = new JButton("Search");
        jButton2.setFont(btnFont);
        jButton2.setBackground(new Color(0, 153, 51));
        jButton2.setForeground(Color.WHITE);
        jButton2.setPreferredSize(btnSize);
        jButton2.setFocusPainted(false);
        try {
            jButton2.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/img/search x30.png")));
        } catch (Exception e) {}
        jButton2.addActionListener(e -> jButton2ActionPerformed());
        
        // Update button with icon
        update = new JButton("Update");
        update.setFont(btnFont);
        update.setBackground(new Color(255, 193, 7));
        update.setForeground(new Color(30, 30, 30));
        update.setPreferredSize(btnSize);
        update.setFocusPainted(false);
        try {
            update.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/img/update.png")));
        } catch (Exception e) {}
        update.addActionListener(e -> updateActionPerformed());
        
        // Delete button with icon
        delete = new JButton("Delete");
        delete.setFont(btnFont);
        delete.setBackground(new Color(220, 53, 69));
        delete.setForeground(Color.WHITE);
        delete.setPreferredSize(btnSize);
        delete.setFocusPainted(false);
        try {
            delete.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/img/delete.png")));
        } catch (Exception e) {}
        delete.addActionListener(e -> deleteActionPerformed());
        
        // Save as Damage button with icon
        save_damage = new JButton("Save Damage");
        save_damage.setFont(btnFont);
        save_damage.setBackground(new Color(108, 117, 125));
        save_damage.setForeground(Color.WHITE);
        save_damage.setPreferredSize(new Dimension(160, 40));
        save_damage.setFocusPainted(false);
        try {
            save_damage.setIcon(new ImageIcon(getClass().getResource("/com/nexatek/images/damage.png")));
        } catch (Exception e) {}
        save_damage.addActionListener(e -> save_damageActionPerformed());
        
        // View Damaged button
        view_damage = new JButton("View Damaged");
        view_damage.setFont(btnFont);
        view_damage.setBackground(new Color(102, 16, 242));
        view_damage.setForeground(Color.WHITE);
        view_damage.setPreferredSize(new Dimension(160, 40));
        view_damage.setFocusPainted(false);
        view_damage.addActionListener(e -> view_damageActionPerformed());

        import_invoice = new JButton("Import Invoice");
        import_invoice.setFont(btnFont);
        import_invoice.setBackground(new Color(17, 94, 89));
        import_invoice.setForeground(Color.WHITE);
        import_invoice.setPreferredSize(new Dimension(170, 40));
        import_invoice.setFocusPainted(false);
        import_invoice.setToolTipText("Scan a supplier invoice with Tesseract OCR and review products before saving");
        import_invoice.addActionListener(e -> importInvoiceActionPerformed());
        
        actionsPanel.add(save);
        actionsPanel.add(jButton2);
        actionsPanel.add(update);
        actionsPanel.add(delete);
        actionsPanel.add(save_damage);
        actionsPanel.add(view_damage);
        actionsPanel.add(import_invoice);
    }
    
    private void createProductsTablePanel() {
        productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Products List (Click to Select)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), new Color(0, 102, 0)));
        
        products_table = new JTable();
        products_table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        products_table.setRowHeight(28);
        products_table.setSelectionBackground(new Color(0, 153, 0));
        products_table.setSelectionForeground(Color.WHITE);
        products_table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        products_table.getTableHeader().setBackground(new Color(242, 242, 242));
        products_table.getTableHeader().setForeground(new Color(0, 0, 255));
        products_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        products_table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                products_tableMouseClicked(evt);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(products_table);
        productsPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createDamagedPanel() {
        damagedPanel = new JPanel(new BorderLayout());
        damagedPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(153, 0, 0), 2), "Damaged Products",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), new Color(153, 0, 0)));
        
        damaged = new JTable();
        damaged.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        damaged.setRowHeight(25);
        damaged.setSelectionBackground(new Color(255, 200, 200));
        damaged.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        damaged.getTableHeader().setBackground(new Color(255, 230, 230));
        damaged.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(damaged);
        damagedPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    // Database methods
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
    
    private void view_damaged() {
        try {
            String sql = "select * from damagedproducts";
            pst = conn.prepareStatement(sql);
            rst = pst.executeQuery();
            damaged.setVisible(true);
            damaged.setModel(DbUtils.resultSetToTableModel(rst));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void clear() {
        product_name.setText("");
        product_size.setText("");
        price.setText("");
        quantity.setText("");
        category.setText("");
        supplier_id.setText("");
        barcode.setText("");
        price2.setText("");
        price3.setText("");
        cost_price.setText("");
    }
    
    public void calculateCostPrice() {
        String name = product_name.getText();
        Float cost = Float.valueOf(cost_price.getText());
        int qty = Integer.parseInt(quantity.getText());
        Float total = cost * qty;
        String sql = "insert into sub_cost_price(product_name,sub_costp,quantity) values(?,?,?)";
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setFloat(2, total);
            pst.setInt(3, qty);
            pst.execute();
        } catch (SQLException ex) {
            Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
        }
        JOptionPane.showMessageDialog(null, total);
    }

    // Event handlers
    private void search_barKeyTyped(KeyEvent evt) {
        String searchCriteria = search_bar.getText().trim();
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
    
    private void products_tableMouseClicked(MouseEvent evt) {
        try {
            DefaultTableModel t = (DefaultTableModel) products_table.getModel();
            int i = products_table.getSelectedRow();
            search_bar.setText(t.getValueAt(i, 0).toString());
            barcode.setText(t.getValueAt(i, 1).toString());
            product_name.setText(t.getValueAt(i, 2).toString());
            product_size.setText(t.getValueAt(i, 3).toString());
            price.setText(t.getValueAt(i, 4).toString());
            price2.setText(t.getValueAt(i, 5).toString());
            price3.setText(t.getValueAt(i, 6).toString());
            category.setText(t.getValueAt(i, 8).toString());
            supplier_id.setText(t.getValueAt(i, 9).toString());
            cost_price.setText(t.getValueAt(i, 10).toString());
        } catch (Exception e) {
        }
    }
    
    private void jButton2ActionPerformed() {
        // Search action - already handled by key listener
    }
    
    private void saveActionPerformed() {
        String qty = quantity.getText();
        String s_id = supplier_id.getText();
        float prc = Float.valueOf(price.getText());
        float prc2 = Float.valueOf(price2.getText());
        float prc3 = Float.valueOf(price3.getText());
        float costp = Float.valueOf(cost_price.getText());
        try {
            String sql = "Insert into products (barcode,name,size,price,price2,price3,quantity,category, supplier_id, cost_price) values(?,?,?,?,?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, barcode.getText().toLowerCase());
            pst.setString(2, product_name.getText().toLowerCase());
            pst.setString(3, product_size.getText().toLowerCase());
            pst.setFloat(4, prc);
            pst.setFloat(5, prc2);
            pst.setFloat(6, prc3);
            pst.setInt(7, Integer.parseInt(qty));
            pst.setString(8, category.getText());
            pst.setInt(9, Integer.valueOf(s_id));
            pst.setFloat(10, costp);
            pst.execute();
            calculateCostPrice();
            JOptionPane.showMessageDialog(null, "Successful");
            products_table.setModel(DbUtils.resultSetToTableModel(rst));
        } catch (SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        Update_table();
        clear();
    }
    
    private void deleteActionPerformed() {
        try {
            String sql = "Delete from products where productid=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, Integer.valueOf(search_bar.getText()));
            pst.execute();
            JOptionPane.showMessageDialog(null, "Deleted");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        Update_table();
        clear();
    }
    
    private void updateActionPerformed() {
        int id_ = Integer.valueOf(search_bar.getText());
        String qty = quantity.getText();
        float prc = Float.valueOf(price.getText());
        float prc2 = Float.valueOf(price2.getText());
        float prc3 = Float.valueOf(price3.getText());
        float costP = Float.valueOf(cost_price.getText());
        String name = product_name.getText();
        String Category = category.getText();
        String size = product_size.getText();
        Float total;

        if (qty.isEmpty()) {
            String sql1 = "select quantity from products where productid = ?";
            String sql3 = "select quantity,sub_costp from sub_cost_price where product_name = ?";
            String sql4 = "update sub_cost_price set sub_costp = ? where product_name = ?";
            try {
                pstextra = conn.prepareStatement(sql3);
                pstextra.setString(1, name);
                rst = pstextra.executeQuery();
                if (rst.next()) {
                    int quant = Integer.valueOf(rst.getString("quantity"));
                    float unitcost = Float.valueOf(cost_price.getText());
                    float totalcost = quant * unitcost;
                    JOptionPane.showMessageDialog(null, totalcost);
                    pstextra_extra = conn.prepareStatement(sql4);
                    pstextra_extra.setFloat(1, totalcost);
                    pstextra_extra.setString(2, name);
                    pstextra_extra.executeUpdate();
                }
                JOptionPane.showMessageDialog(null, "Updated the cost price too");
            } catch (SQLException ex) {
                Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pstInsert = conn.prepareStatement(sql1);
                pstInsert.setInt(1, id_);
                rst = pstInsert.executeQuery();
                if (rst.next()) {
                    Float newqty = Float.valueOf(rst.getString("quantity"));
                    JOptionPane.showMessageDialog(null, newqty);
                    String sql = "update products set name = ?, category = ?, price = ?, price2 = ?, price3 = ?, quantity = ?, size = ?, cost_price = ? where productid = ?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, name);
                    pst.setString(2, Category);
                    pst.setFloat(3, prc);
                    pst.setFloat(4, prc2);
                    pst.setFloat(5, prc3);
                    pst.setFloat(6, newqty);
                    pst.setString(7, size);
                    pst.setFloat(8, costP);
                    pst.setInt(9, id_);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Changes Tracked Successfully");
                    Update_table();
                    clear();
                }
            } catch (SQLException ex) {
                Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (Integer.valueOf(qty) > 0) {
            int qty4 = Integer.valueOf(qty);
            total = costP * qty4;
            String sql = "insert into sub_cost_price(product_name, sub_costp, quantity) values(?,?,?)";
            String sql2 = "select quantity from products where productid = ?";
            try {
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setFloat(2, total);
                pst.setInt(3, qty4);
                pst.executeUpdate();
                pstInsert = conn.prepareStatement(sql2);
                pstInsert.setInt(1, id_);
                rst = pstInsert.executeQuery();
                if (rst.next()) {
                    Float newqty = Float.valueOf(rst.getString("quantity"));
                    Float totalqty = qty4 + newqty;
                    String sql3 = "update products set name = ?, category = ?, price = ?, price2 = ?, price3 = ?, quantity = ?, size = ?, cost_price = ? where productid = ?";
                    pstextra = conn.prepareStatement(sql3);
                    pstextra.setString(1, name);
                    pstextra.setString(2, Category);
                    pstextra.setFloat(3, prc);
                    pstextra.setFloat(4, prc2);
                    pstextra.setFloat(5, prc3);
                    pstextra.setFloat(6, totalqty);
                    pstextra.setString(7, size);
                    pstextra.setFloat(8, costP);
                    pstextra.setInt(9, id_);
                    pstextra.executeUpdate();
                    JOptionPane.showMessageDialog(null, totalqty);
                }
                Update_table();
                JOptionPane.showMessageDialog(null, "Successfully added the sum of cost price and saved to db");
            } catch (SQLException ex) {
                Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void save_damageActionPerformed() {
        String qty = quantity.getText();
        String s_id = supplier_id.getText();
        float prc = Float.valueOf(price.getText());
        try {
            String sql = "Insert into damagedproducts (barcode,name,size,price,quantity,category, supplier_id) values(?,?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, barcode.getText());
            pst.setString(2, product_name.getText());
            pst.setString(3, product_size.getText());
            pst.setFloat(4, prc);
            pst.setInt(5, Integer.parseInt(qty));
            pst.setString(6, category.getText());
            pst.setInt(7, Integer.valueOf(s_id));
            pst.execute();
            JOptionPane.showMessageDialog(null, "Product Added to Damaged");
            products_table.setModel(DbUtils.resultSetToTableModel(rst));
        } catch (SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        Update_table();
        clear();
    }
    
    private void view_damageActionPerformed() {
        view_damaged();
    }

    private void importInvoiceActionPerformed() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose supplier invoice image or OCR text");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Invoice images or text", "png", "jpg", "jpeg", "tif", "tiff", "bmp", "txt"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File invoiceFile = chooser.getSelectedFile();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        import_invoice.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (invoiceFile.getName().toLowerCase().endsWith(".txt")) {
                    return Files.readString(invoiceFile.toPath());
                }
                return runTesseract(invoiceFile);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                import_invoice.setEnabled(true);
                try {
                    String text = get();
                    showInvoiceReviewDialog(text, parseInvoiceText(text));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(product.this,
                            "Could not read this invoice.\n\nLuckyPOS looked for Tesseract in PATH and common Windows install folders.\n"
                            + "If Tesseract is installed elsewhere, add its folder to PATH and restart LuckyPOS.\n\nDetails: " + ex.getMessage(),
                            "Invoice OCR", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String runTesseract(File invoiceFile) throws IOException, InterruptedException {
        String tesseractCommand = findTesseractCommand();
        ProcessBuilder builder = new ProcessBuilder(
                tesseractCommand,
                invoiceFile.getAbsolutePath(),
                "stdout",
                "-l",
                "eng",
                "--psm",
                "6");
        configureTesseractEnvironment(builder, tesseractCommand);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0 || output.trim().isEmpty()) {
            throw new IOException(output.trim().isEmpty() ? "Tesseract returned no text." : output.trim());
        }
        return output;
    }

    private String findTesseractCommand() {
        String configured = System.getProperty("luckypos.tesseract.path");
        if (isExecutableFile(configured)) {
            return configured;
        }

        String envPath = System.getenv("TESSERACT_PATH");
        if (isExecutableFile(envPath)) {
            return envPath;
        }

        String pathValue = System.getenv("PATH");
        if (pathValue != null) {
            for (String folder : pathValue.split(Pattern.quote(File.pathSeparator))) {
                if (!folder.trim().isEmpty()) {
                    Path candidate = Paths.get(folder, "tesseract.exe");
                    if (isExecutableFile(candidate.toString())) {
                        return candidate.toString();
                    }
                }
            }
        }

        String[] commonWindowsPaths = {
            "C:\\Program Files\\Tesseract-OCR\\tesseract.exe",
            "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe"
        };
        for (String path : commonWindowsPaths) {
            if (isExecutableFile(path)) {
                return path;
            }
        }

        return "tesseract";
    }

    private boolean isExecutableFile(String path) {
        return path != null && !path.trim().isEmpty() && Files.isRegularFile(Paths.get(path.trim()));
    }

    private void configureTesseractEnvironment(ProcessBuilder builder, String tesseractCommand) {
        Path tesseractPath = Paths.get(tesseractCommand);
        Path installFolder = tesseractPath.getParent();
        if (installFolder == null) {
            return;
        }

        Path tessdataFolder = installFolder.resolve("tessdata");
        if (Files.isDirectory(tessdataFolder)) {
            builder.environment().putIfAbsent("TESSDATA_PREFIX", tessdataFolder.toString());
        }
    }

    private List<InvoiceProductDraft> parseInvoiceText(String rawText) {
        List<InvoiceProductDraft> drafts = new ArrayList<>();
        String[] lines = rawText.split("\\R");
        for (String line : lines) {
            InvoiceProductDraft draft = parseInvoiceLine(line);
            if (draft != null) {
                drafts.add(draft);
            }
        }
        return drafts;
    }

    private InvoiceProductDraft parseInvoiceLine(String line) {
        String cleaned = line == null ? "" : line.trim().replaceAll("[|]+", " ").replaceAll("\\s{2,}", " ");
        if (cleaned.length() < 5 || cleaned.matches(".*(?i)(invoice|supplier|subtotal|total|amount|vat|tax|date|receipt).*")) {
            return null;
        }

        String[] tokens = cleaned.split("\\s+");
        if (tokens.length < 10 || !tokens[0].matches("\\d{5,}")) {
            return null;
        }

        int retailIndex = findRetailPriceIndex(tokens);
        if (retailIndex < 3 || retailIndex + 6 >= tokens.length) {
            return null;
        }

        int supplierIndex = tokens.length - 2;
        int costIndex = tokens.length - 1;
        if (!isNumericToken(tokens[supplierIndex]) || !isNumericToken(tokens[costIndex])) {
            return null;
        }

        String namePart = joinTokens(tokens, 1, retailIndex - 1);
        if (namePart.isEmpty()) {
            return null;
        }

        InvoiceProductDraft draft = new InvoiceProductDraft();
        draft.include = true;
        draft.barcode = tokens[0];
        draft.name = cleanOcrText(namePart.toLowerCase());
        draft.size = cleanOcrText(tokens[retailIndex - 1].toLowerCase());
        draft.retailPrice = parseNumberToken(tokens[retailIndex]);
        draft.xlPrice = parseNumberToken(tokens[retailIndex + 1]);
        draft.xxlPrice = parseNumberToken(tokens[retailIndex + 2]);
        draft.quantity = normalizeQuantityToken(tokens[retailIndex + 3]);
        draft.category = cleanOcrText(joinTokens(tokens, retailIndex + 4, supplierIndex));
        draft.supplierId = tokens[supplierIndex];
        draft.costPrice = normalizeCostToken(tokens[costIndex], draft.retailPrice);
        return draft;
    }

    private int findRetailPriceIndex(String[] tokens) {
        for (int i = 2; i + 3 < tokens.length; i++) {
            if (isNumericToken(tokens[i]) && isNumericToken(tokens[i + 1])
                    && isNumericToken(tokens[i + 2]) && isNumericToken(tokens[i + 3])) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNumericToken(String token) {
        return token != null && token.matches("\\d+(?:[,.]\\d+)?");
    }

    private double parseNumberToken(String token) {
        return Double.parseDouble(token.replace(",", ""));
    }

    private int normalizeQuantityToken(String token) {
        double quantityValue = parseNumberToken(token);
        if (!token.contains(".") && token.length() == 2 && token.endsWith("0")) {
            quantityValue = quantityValue / 10.0;
        }
        return Math.max(1, (int) Math.round(quantityValue));
    }

    private double normalizeCostToken(String token, double retailPrice) {
        double costValue = parseNumberToken(token);
        while (!token.contains(".") && costValue > retailPrice && costValue >= 100000) {
            costValue = costValue / 10.0;
        }
        return costValue;
    }

    private String joinTokens(String[] tokens, int startInclusive, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = startInclusive; i < endExclusive && i < tokens.length; i++) {
            if (i >= 0) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(tokens[i]);
            }
        }
        return builder.toString().trim();
    }

    private String cleanOcrText(String value) {
        return value == null ? "" : value.replaceFirst("^[^A-Za-z0-9]+", "").trim();
    }

    private void showInvoiceReviewDialog(String rawText, List<InvoiceProductDraft> drafts) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Review Invoice Products", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(248, 250, 252));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Use", "Barcode", "Name", "Size", "Cost", "Retail", "XL", "XXL", "Qty", "Category", "Supplier ID"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        for (InvoiceProductDraft draft : drafts) {
            model.addRow(new Object[]{
                draft.include, draft.barcode, draft.name, draft.size,
                formatNumber(draft.costPrice), formatNumber(draft.retailPrice),
                formatNumber(draft.xlPrice), formatNumber(draft.xxlPrice),
                draft.quantity, draft.category, draft.supplierId
            });
        }

        JTable reviewTable = new JTable(model);
        reviewTable.setRowHeight(28);
        reviewTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reviewTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(220);

        JTextArea rawTextArea = new JTextArea(rawText);
        rawTextArea.setEditable(false);
        rawTextArea.setLineWrap(true);
        rawTextArea.setWrapStyleWord(true);
        rawTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(reviewTable), new JScrollPane(rawTextArea));
        splitPane.setResizeWeight(0.7);

        JLabel title = new JLabel("Edit the OCR results before saving. Untick rows you do not want to import.");
        title.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addRow = new JButton("Add Row");
        JButton removeRow = new JButton("Remove Row");
        JButton saveRows = new JButton("Save Selected");
        JButton cancel = new JButton("Cancel");

        addRow.addActionListener(e -> model.addRow(new Object[]{true, "", "", "", "0", "0", "0", "0", 1, category.getText().trim(), supplier_id.getText().trim()}));
        removeRow.addActionListener(e -> {
            int selected = reviewTable.getSelectedRow();
            if (selected >= 0) {
                model.removeRow(reviewTable.convertRowIndexToModel(selected));
            }
        });
        saveRows.addActionListener(e -> {
            int saved = saveReviewedInvoiceRows(model);
            if (saved > 0) {
                dialog.dispose();
                Update_table();
                JOptionPane.showMessageDialog(product.this, saved + " product(s) imported successfully.", "Invoice OCR", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(addRow);
        buttons.add(removeRow);
        buttons.add(saveRows);
        buttons.add(cancel);

        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "OCR completed, but no product lines were detected. You can add rows manually in the review window.",
                    "Invoice OCR", JOptionPane.INFORMATION_MESSAGE);
        }

        dialog.add(title, BorderLayout.NORTH);
        dialog.add(splitPane, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setSize(1050, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int saveReviewedInvoiceRows(DefaultTableModel model) {
        int saved = 0;
        String sql = "Insert into products (barcode,name,size,price,price2,price3,quantity,category, supplier_id, cost_price) values(?,?,?,?,?,?,?,?,?,?)";
        String costSql = "insert into sub_cost_price(product_name,sub_costp,quantity) values(?,?,?)";

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement productStatement = conn.prepareStatement(sql);
                 PreparedStatement costStatement = conn.prepareStatement(costSql)) {
                for (int row = 0; row < model.getRowCount(); row++) {
                    if (!Boolean.TRUE.equals(model.getValueAt(row, 0))) {
                        continue;
                    }

                    String nameValue = stringCell(model, row, 2).trim().toLowerCase();
                    if (nameValue.isEmpty()) {
                        throw new IllegalArgumentException("Row " + (row + 1) + " is missing the product name.");
                    }

                    int quantityValue = parseIntCell(model, row, 8, "quantity");
                    double costValue = parseDoubleCell(model, row, 4, "cost price");
                    double retailValue = parseDoubleCell(model, row, 5, "retail price");
                    double xlValue = parseDoubleCell(model, row, 6, "XL price");
                    double xxlValue = parseDoubleCell(model, row, 7, "XXL price");
                    int supplierValue = parseIntCell(model, row, 10, "supplier ID");

                    productStatement.setString(1, stringCell(model, row, 1).trim().toLowerCase());
                    productStatement.setString(2, nameValue);
                    productStatement.setString(3, stringCell(model, row, 3).trim().toLowerCase());
                    productStatement.setFloat(4, (float) retailValue);
                    productStatement.setFloat(5, (float) xlValue);
                    productStatement.setFloat(6, (float) xxlValue);
                    productStatement.setInt(7, quantityValue);
                    productStatement.setString(8, stringCell(model, row, 9).trim());
                    productStatement.setInt(9, supplierValue);
                    productStatement.setFloat(10, (float) costValue);
                    productStatement.executeUpdate();

                    costStatement.setString(1, nameValue);
                    costStatement.setFloat(2, (float) (costValue * quantityValue));
                    costStatement.setInt(3, quantityValue);
                    costStatement.executeUpdate();
                    saved++;
                }
            }
            conn.commit();
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, rollbackEx);
            }
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invoice OCR", JOptionPane.ERROR_MESSAGE);
            saved = 0;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return saved;
    }

    private String stringCell(DefaultTableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private int parseIntCell(DefaultTableModel model, int row, int column, String label) {
        String value = stringCell(model, row, column).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Row " + (row + 1) + " is missing " + label + ".");
        }
        return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
    }

    private double parseDoubleCell(DefaultTableModel model, int row, int column, String label) {
        String value = stringCell(model, row, column).trim().replace(",", "");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Row " + (row + 1) + " is missing " + label + ".");
        }
        return Double.parseDouble(value.replaceAll("[^0-9.-]", ""));
    }

    private String formatNumber(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private static class InvoiceProductDraft {
        boolean include;
        String barcode;
        String name;
        String size;
        double costPrice;
        double retailPrice;
        double xlPrice;
        double xxlPrice;
        int quantity;
        String category;
        String supplierId;
    }
}
