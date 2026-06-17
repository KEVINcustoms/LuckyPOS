package com.nexatek;

import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author mrrobot
 */
public class Home extends javax.swing.JFrame {

    private String user;
    JpanelLoader jpload = new JpanelLoader();

    Connection conn;
 

    // Variables for custom title bar dragging
    private int mouseX, mouseY;
    private boolean isMaximized = true;
    private java.awt.Rectangle normalBounds;
    
    public Home() {
        conn = connection.connect();
        
        // Make window undecorated for custom title bar
        setUndecorated(true);
        
        initComponents();
        
        // Set application icon for taskbar
        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png")).getImage());
        } catch (Exception e) {
            System.out.println("Could not load icon: " + e.getMessage());
        }
        
        // Setup custom title bar with logo and buttons
        setupCustomTitleBar();
        
        SwingUtilities.invokeLater(() -> {
            sales_admin sladmin = new sales_admin(counter.getText());
            jpload.jPanelLoader(panel_load, sladmin);
        });
        
        Timer timer = new Timer(10000, e->
        sendData());
        timer.start();

        setExtendedState(Home.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void setupCustomTitleBar() {
        // Transform jPanel3 into a custom title bar
        jPanel3.setLayout(new java.awt.BorderLayout(10, 0));
        jPanel3.setBackground(new java.awt.Color(25, 42, 65));
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 45));
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        // LEFT: Logo + Title
        javax.swing.JPanel leftPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        // Logo
        javax.swing.JLabel logoLabel = new javax.swing.JLabel();
        try {
            javax.swing.ImageIcon logoIcon = new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png"));
            java.awt.Image scaledLogo = logoIcon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
            logoLabel.setIcon(new javax.swing.ImageIcon(scaledLogo));
        } catch (Exception e) {
            logoLabel.setText("N");
            logoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
            logoLabel.setForeground(new java.awt.Color(255, 193, 7));
        }
        
        // Title
        jLabel1.setText("KEBZ PHONE SERVICE CENTRE");
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        jLabel1.setForeground(java.awt.Color.WHITE);
        
        leftPanel.add(logoLabel);
        leftPanel.add(jLabel1);
        
        // CENTER: Custom action buttons
        javax.swing.JPanel centerPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 0));
        centerPanel.setOpaque(false);
        
        java.awt.Font btnFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11);
        java.awt.Dimension btnSize = new java.awt.Dimension(90, 30);
        
        // Settings
        btnSettings = createTitleBarButton("Settings", new java.awt.Color(41, 128, 185), btnFont, btnSize);
        btnSettings.addActionListener(e -> btnSettingsActionPerformed());
        
        // Alerts
        btnNotifications = createTitleBarButton("Alerts", new java.awt.Color(230, 126, 34), btnFont, btnSize);
        btnNotifications.addActionListener(e -> btnNotificationsActionPerformed());
        
        // Backup
        btnBackup = createTitleBarButton("Backup", new java.awt.Color(142, 68, 173), btnFont, btnSize);
        btnBackup.addActionListener(e -> btnBackupActionPerformed());
        
        // Help
        btnHelp = createTitleBarButton("Help", new java.awt.Color(52, 73, 94), btnFont, btnSize);
        btnHelp.addActionListener(e -> btnHelpActionPerformed());
        
        // About
        btnAbout = createTitleBarButton("About", new java.awt.Color(52, 73, 94), btnFont, btnSize);
        btnAbout.addActionListener(e -> btnAboutActionPerformed());
        
        centerPanel.add(btnSettings);
        centerPanel.add(btnNotifications);
        centerPanel.add(btnBackup);
        centerPanel.add(btnHelp);
        centerPanel.add(btnAbout);
        
        // RIGHT: Window control buttons (minimize, maximize, close)
        javax.swing.JPanel controlPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        controlPanel.setOpaque(false);
        
        // Minimize button
        javax.swing.JButton btnMinimize = new javax.swing.JButton("_");
        btnMinimize.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        btnMinimize.setPreferredSize(new java.awt.Dimension(45, 35));
        btnMinimize.setForeground(java.awt.Color.WHITE);
        btnMinimize.setBackground(new java.awt.Color(25, 42, 65));
        btnMinimize.setBorderPainted(false);
        btnMinimize.setFocusPainted(false);
        btnMinimize.addActionListener(e -> setState(java.awt.Frame.ICONIFIED));
        btnMinimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnMinimize.setBackground(new java.awt.Color(60, 80, 100)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnMinimize.setBackground(new java.awt.Color(25, 42, 65)); }
        });
        
        // Maximize/Restore button
        javax.swing.JButton btnMaximize = new javax.swing.JButton("[ ]");
        btnMaximize.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnMaximize.setPreferredSize(new java.awt.Dimension(45, 35));
        btnMaximize.setForeground(java.awt.Color.WHITE);
        btnMaximize.setBackground(new java.awt.Color(25, 42, 65));
        btnMaximize.setBorderPainted(false);
        btnMaximize.setFocusPainted(false);
        btnMaximize.addActionListener(e -> {
            if (isMaximized) {
                // Restore
                if (normalBounds != null) {
                    setBounds(normalBounds);
                } else {
                    setSize(1200, 800);
                    setLocationRelativeTo(null);
                }
                isMaximized = false;
                btnMaximize.setText("[ ]");
            } else {
                // Maximize
                normalBounds = getBounds();
                setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
                isMaximized = true;
                btnMaximize.setText("[=]");
            }
        });
        btnMaximize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnMaximize.setBackground(new java.awt.Color(60, 80, 100)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnMaximize.setBackground(new java.awt.Color(25, 42, 65)); }
        });
        
        // Close button
        javax.swing.JButton btnClose = new javax.swing.JButton("X");
        btnClose.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnClose.setPreferredSize(new java.awt.Dimension(45, 35));
        btnClose.setForeground(java.awt.Color.WHITE);
        btnClose.setBackground(new java.awt.Color(25, 42, 65));
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> System.exit(0));
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnClose.setBackground(new java.awt.Color(220, 53, 69)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btnClose.setBackground(new java.awt.Color(25, 42, 65)); }
        });
        
        controlPanel.add(btnMinimize);
        controlPanel.add(btnMaximize);
        controlPanel.add(btnClose);
        
        // Add all panels to title bar
        jPanel3.removeAll();
        jPanel3.add(leftPanel, java.awt.BorderLayout.WEST);
        jPanel3.add(centerPanel, java.awt.BorderLayout.CENTER);
        jPanel3.add(controlPanel, java.awt.BorderLayout.EAST);
        
        // Make title bar draggable
        jPanel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        jPanel3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (!isMaximized) {
                    setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
                }
            }
        });
    }
    
    private javax.swing.JButton createTitleBarButton(String text, java.awt.Color bg, java.awt.Font font, java.awt.Dimension size) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(java.awt.Color.WHITE);
        btn.setPreferredSize(size);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }
    
    // Placeholder methods for button actions - to be implemented later
    private void btnSettingsActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this, "Settings - Coming Soon!", "Settings", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void btnNotificationsActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this, "Notifications - Coming Soon!", "Alerts", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void btnBackupActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this, "Backup - Coming Soon!", "Backup", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void btnHelpActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this, "Help & Documentation - Coming Soon!", "Help", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void btnAboutActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Lucky Electricals POS System\nVersion 1.0.0\n\nPowered by Nexatek Group\n© 2024 All Rights Reserved", 
            "About", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
  public void sendData() {
    String allDataQuery = "SELECT products.name AS name, products.productid AS productid, " +
                          "products.cost_price AS unit_cost_price, products.quantity AS quantity, " +
                          "sub_cost_price.quantity AS initQuantity, " +
                          "sub_cost_price.sub_costp AS total_cost_prices " +
                          "FROM products INNER JOIN sub_cost_price ON products.name = sub_cost_price.product_name";

    String sqlProfitInsert = "INSERT INTO profits (productid, productname, initialquantity, unitcost, totalcostprices, totalsales, stockquantity, profits, profit_date) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String sqlProfitUpdate = "UPDATE profits SET profits = profits + ?, totalsales = ?, stockquantity = ? " +
                             "WHERE productid = ? AND profit_date = ?";

    PreparedStatement pst = null;
    ResultSet rst = null;

    try {
        // Fetch product details with the join
        pst = conn.prepareStatement(allDataQuery);
        rst = pst.executeQuery();

        while (rst.next()) {
            String productName = rst.getString("name");
            int productId = rst.getInt("productid");
            int initialQuantity = rst.getInt("initQuantity");
            int stockQuantity = rst.getInt("quantity");
            double unitCostPrice = rst.getDouble("unit_cost_price");
            double totalCostPrice = rst.getDouble("total_cost_prices");

            // Fetch sales for the product for the current day
            String sqlNewSales = "SELECT paid_amount FROM solditems WHERE itemid = ? AND selldate = ?";
            LocalDate profitDate = LocalDate.now();
            try (PreparedStatement pstNewSales = conn.prepareStatement(sqlNewSales)) {
                pstNewSales.setInt(1, productId);
                pstNewSales.setDate(2, java.sql.Date.valueOf(profitDate));

                try (ResultSet rstNewSales = pstNewSales.executeQuery()) {
                    double newSales = 0;

                    // Process the results to get the exact sales for the day
                    while (rstNewSales.next()) {
                        newSales += rstNewSales.getDouble("paid_amount");
                    }

                    // Calculate profit for the day
                    double profitCalculation = newSales - totalCostPrice;

                    // Check if there's already an entry for this product today
                    String sqlCheckProfit = "SELECT productid, profits, totalsales FROM profits WHERE productid = ? AND profit_date = ?";
                    try (PreparedStatement pstCheckProfit = conn.prepareStatement(sqlCheckProfit)) {
                        pstCheckProfit.setInt(1, productId);
                        pstCheckProfit.setDate(2, java.sql.Date.valueOf(profitDate));

                        try (ResultSet rstCheckProfit = pstCheckProfit.executeQuery()) {

                            // If there's an entry for today, update the profits
                            if (rstCheckProfit.next()) {
                                double existingSales = rstCheckProfit.getDouble("totalsales");

                                // Check if there are new sales, only update if there are new sales
                                if (newSales > existingSales) {
                                    try (PreparedStatement pstUpdate = conn.prepareStatement(sqlProfitUpdate)) {
                                        pstUpdate.setDouble(1, profitCalculation); // Increment the profits
                                        pstUpdate.setDouble(2, newSales); // Update total sales
                                        pstUpdate.setInt(3, stockQuantity); // Update stock quantity
                                        pstUpdate.setInt(4, productId);
                                        pstUpdate.setDate(5, java.sql.Date.valueOf(profitDate));
                                        pstUpdate.executeUpdate();

                                        Logger.getLogger(counter.class.getName()).log(Level.INFO, "Updated profits for productid: {0} on date: {1}", new Object[]{productId, profitDate});
                                    }
                                }
                            }

                            // Separate logic: Insert a new entry only if there are new sales for today
                            if (newSales > 0 && profitDate.equals(LocalDate.now())) {
                                // Insert new profit data since there are new sales for today
                                try (PreparedStatement pstInsert = conn.prepareStatement(sqlProfitInsert)) {
                                    pstInsert.setInt(1, productId);
                                    pstInsert.setString(2, productName);
                                    pstInsert.setInt(3, initialQuantity);
                                    pstInsert.setDouble(4, unitCostPrice);
                                    pstInsert.setDouble(5, totalCostPrice);
                                    pstInsert.setDouble(6, newSales); // Using new sales here
                                    pstInsert.setInt(7, stockQuantity);
                                    pstInsert.setDouble(8, profitCalculation);
                                    pstInsert.setDate(9, java.sql.Date.valueOf(profitDate));
                                    pstInsert.executeUpdate();

                                    Logger.getLogger(counter.class.getName()).log(Level.INFO, "Inserted new profits for productid: {0} on date: {1}", new Object[]{productId, profitDate});
                                }
                            } else {
                                Logger.getLogger(counter.class.getName()).log(Level.INFO, "No new sales or not the current date for productid: {0}, skipping insertion.", productId);
                            }
                        }
                    }
                }
            }
        }
    } catch (SQLException ex) {
        Logger.getLogger(counter.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            if (rst != null) rst.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            Logger.getLogger(counter.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}

    
    /*
    public void sendData() {
    String allDataQuery ="SELECT products.name AS name, products.productid AS productid, " +
                         "products.cost_price AS unit_cost_price, sub_cost_price.quantity AS initQuantity, " +
                         "sub_cost_price.sub_costp AS total_cost_prices " +
                         "FROM products INNER JOIN sub_cost_price ON products.name = sub_cost_price.product_name";

    String sqlProfitInsert = "INSERT INTO profits (productid, productname, initialquantity, unitcost, totalcostprices, totalsales, stockquantity, profits, profit_date) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String sqlProfitUpdate = "UPDATE profits SET profits = profits + ?, totalsales = ?, stockquantity = ? " +
                             "WHERE productid = ? AND profit_date = ?";
    String sqlProfitDate = "select profit_date from profits";
   
    

    try {
        // Fetch product details with the join
        PreparedStatement pst = conn.prepareStatement(allDataQuery);
        ResultSet rst = pst.executeQuery();

        while (rst.next()) {
            String productName = rst.getString("name");
            int productId = rst.getInt("productid");
            int initialQuantity = rst.getInt("initQuantity");
            double unitCostPrice = rst.getDouble("unit_cost_price");
            double totalCostPrice = rst.getDouble("total_cost_prices");

            // Fetch total sales for the product
            String sqlTotalSales = "SELECT SUM(paid_amount) AS totalsales FROM solditems WHERE itemid = ?";
            PreparedStatement pstTotalSales = conn.prepareStatement(sqlTotalSales);
            pstTotalSales.setInt(1, productId);
            ResultSet rstTotalSales = pstTotalSales.executeQuery();

            double totalSales = 0;
            if (rstTotalSales.next()) {
                totalSales = rstTotalSales.getDouble("totalsales");
            }
            rstTotalSales.close(); 
            
            PreparedStatement pstProfitDate = conn.prepareStatement(sqlProfitDate);
            String profitDateFromDatabase = null;
            ResultSet rstProfitDate = pstProfitDate.executeQuery();
            while (rstProfitDate.next()) {
                profitDateFromDatabase = rstProfitDate.getString("profit_date");
            }
            pstProfitDate.close();
            rstProfitDate.close();

            // Calculate profit for the day
            double profitCalculation = totalSales - totalCostPrice;
            LocalDate profitDate = LocalDate.now();
            

            // Check if a profit entry already exists for the current day
            String sqlCheckProfit = "SELECT productid FROM profits WHERE productid = ? AND profit_date = ?";
            PreparedStatement pstCheckProfit = conn.prepareStatement(sqlCheckProfit);
            pstCheckProfit.setInt(1, productId);
            pstCheckProfit.setDate(2, java.sql.Date.valueOf(profitDate));
            ResultSet rstCheckProfit = pstCheckProfit.executeQuery();
            int foundProductId = 0;
            

            //Profit date exists
            while (rstCheckProfit.next()) {
                foundProductId = rstCheckProfit.getInt("productid");
            }
            
            pstCheckProfit.close();
            rstCheckProfit.close();
            
            // There was a profit already saved in the database
            if (foundProductId != 0) {
                 // && rstCheckProfit.getString("profit_date") == profitDate.toString()
                 System.out.println("profitDateFromDatabase : " + profitDateFromDatabase);
                 System.out.println("profitDate : " + profitDate.toString());
                if (profitDateFromDatabase.equals(profitDate.toString() )) {
                    // Update the existing record by adding the new profit to the existing one
                PreparedStatement pstUpdate = conn.prepareStatement(sqlProfitUpdate);
                pstUpdate.setDouble(1, profitCalculation);
                pstUpdate.setDouble(2, totalSales);
                pstUpdate.setInt(3, initialQuantity);
                pstUpdate.setInt(4, productId);
                pstUpdate.setDate(5, java.sql.Date.valueOf(profitDate));
                pstUpdate.executeUpdate();

                Logger.getLogger(counter.class.getName()).log(Level.INFO, "Updated profits for productid: {0} on date: {1}", new Object[]{productId, profitDate});
                }
                
            } 
            //There was a profit already saved in the database
            else {
                    // Insert a new record for the current day
                PreparedStatement pstInsert = conn.prepareStatement(sqlProfitInsert);
                pstInsert.setInt(1, productId);
                pstInsert.setString(2, productName);
                pstInsert.setInt(3, initialQuantity);
                pstInsert.setDouble(4, unitCostPrice);
                pstInsert.setDouble(5, totalCostPrice);
                pstInsert.setDouble(6, totalSales);
                pstInsert.setInt(7, initialQuantity);
                pstInsert.setDouble(8, profitCalculation);
                pstInsert.setDate(9, java.sql.Date.valueOf(profitDate));
                pstInsert.executeUpdate();

                Logger.getLogger(counter.class.getName()).log(Level.INFO, "Inserted profits for productid: {0} on date: {1}", new Object[]{productId, profitDate});
            }
                
                // && rstCheckProfit.getString("profit_date") != profitDate.toString()
                
                

            // Close the result sets and statements for each loop iteration
            rstTotalSales.close();
            pstTotalSales.close();
            
        }

        // Close the initial prepared statement and result set
        rst.close();
        pst.close();
    } catch (SQLException ex) {
        Logger.getLogger(counter.class.getName()).log(Level.SEVERE, null, ex);
    }
}
*/
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        home_bnt_grp = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        poweroff = new javax.swing.JButton();
        counter = new javax.swing.JLabel();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        panel_load = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jToggleButton2.setBackground(new java.awt.Color(255, 204, 102));
        home_bnt_grp.add(jToggleButton2);
        jToggleButton2.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/emp.png"))); // NOI18N
        jToggleButton2.setText("Employee");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jToggleButton3.setBackground(new java.awt.Color(0, 153, 255));
        home_bnt_grp.add(jToggleButton3);
        jToggleButton3.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/customer.png"))); // NOI18N
        jToggleButton3.setText("Technicians");
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton4.setBackground(new java.awt.Color(204, 0, 0));
        home_bnt_grp.add(jToggleButton4);
        jToggleButton4.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/product.png"))); // NOI18N
        jToggleButton4.setText("Product");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jToggleButton5.setBackground(new java.awt.Color(0, 153, 51));
        home_bnt_grp.add(jToggleButton5);
        jToggleButton5.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/sales_menu.png"))); // NOI18N
        jToggleButton5.setText("Sales");
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        jToggleButton6.setBackground(new java.awt.Color(255, 204, 102));
        home_bnt_grp.add(jToggleButton6);
        jToggleButton6.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/invo.png"))); // NOI18N
        jToggleButton6.setText("Invoice");
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        jToggleButton7.setBackground(new java.awt.Color(0, 153, 255));
        home_bnt_grp.add(jToggleButton7);
        jToggleButton7.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/reports.png"))); // NOI18N
        jToggleButton7.setText("Reports");
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        jToggleButton8.setBackground(new java.awt.Color(255, 204, 102));
        home_bnt_grp.add(jToggleButton8);
        jToggleButton8.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/customer.png"))); // NOI18N
        jToggleButton8.setText("Customers");
        jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton8ActionPerformed(evt);
            }
        });

        jToggleButton9.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/icons8-exit-50.png"))); // NOI18N
        jToggleButton9.setText("LOGOUT");
        jToggleButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton9ActionPerformed(evt);
            }
        });

        jToggleButton10.setBackground(new java.awt.Color(204, 0, 0));
        home_bnt_grp.add(jToggleButton10);
        jToggleButton10.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/product.png"))); // NOI18N
        jToggleButton10.setText("OUT OF STOCK");
        jToggleButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton10ActionPerformed(evt);
            }
        });

        poweroff.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        poweroff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/icons8-power-off-48.png"))); // NOI18N
        poweroff.setText("Power Off");
        poweroff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                poweroffActionPerformed(evt);
            }
        });

        counter.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jToggleButton11.setBackground(new java.awt.Color(0, 153, 51));
        home_bnt_grp.add(jToggleButton11);
        jToggleButton11.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/supplier.png"))); // NOI18N
        jToggleButton11.setText("Expenditures");
        jToggleButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton11ActionPerformed(evt);
            }
        });

        jToggleButton1.setBackground(new java.awt.Color(0, 153, 51));
        home_bnt_grp.add(jToggleButton1);
        jToggleButton1.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/supplier.png"))); // NOI18N
        jToggleButton1.setText("Supplier");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton12.setBackground(new java.awt.Color(153, 76, 0));
        home_bnt_grp.add(jToggleButton12);
        jToggleButton12.setFont(new java.awt.Font("Cantarell", 1, 17)); // NOI18N
        jToggleButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/supplier.png"))); // NOI18N
        jToggleButton12.setText("Phone Repair");
        jToggleButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(poweroff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(counter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jToggleButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(counter, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55)
                .addComponent(poweroff, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panel_load.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panel_loadLayout = new javax.swing.GroupLayout(panel_load);
        panel_load.setLayout(panel_loadLayout);
        panel_loadLayout.setHorizontalGroup(
            panel_loadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_loadLayout.setVerticalGroup(
            panel_loadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(0, 153, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        jLabel1.setText("KEBZ PHONE SERVICE CENTRE");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 17, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel_load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel_load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        supplier sp = new supplier();
        jpload.jPanelLoader(panel_load, sp);
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
        employee emp = new employee();
        jpload.jPanelLoader(panel_load, emp);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed

        // TODO add your handling code here:
        technicians tech = new technicians();
        jpload.jPanelLoader(panel_load, tech);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
// TODO add your handling code here:
        product sp = new product();
        jpload.jPanelLoader(panel_load, sp);
        Toolkit.getDefaultToolkit().beep();
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed

        sales_admin sales = new sales_admin(counter.getText());
        //this.setVisible(true);
        jpload.jPanelLoader(panel_load, sales);
    }//GEN-LAST:event_jToggleButton5ActionPerformed


    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        // TODO add your handling code here:
        invoices inv = new invoices();
        jpload.jPanelLoader(panel_load, inv);
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        // TODO add your handling code here:
        reports report = new reports();
        jpload.jPanelLoader(panel_load, report);
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton8ActionPerformed
        // TODO add your handling code here:
        customer cus = new customer();
        jpload.jPanelLoader(panel_load, cus);
    }//GEN-LAST:event_jToggleButton8ActionPerformed

    private void jToggleButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton10ActionPerformed
        out_of_stock stock = new out_of_stock();
        jpload.jPanelLoader(panel_load, stock);
    }//GEN-LAST:event_jToggleButton10ActionPerformed

    private void jToggleButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton9ActionPerformed
        this.setVisible(false);
        LOGIN login = new LOGIN();
        login.setVisible(true);
    }//GEN-LAST:event_jToggleButton9ActionPerformed

    private void poweroffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_poweroffActionPerformed
        System.exit(0);
    }//GEN-LAST:event_poweroffActionPerformed

    private void jToggleButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton11ActionPerformed
        expenditures expend = new expenditures();
        jpload.jPanelLoader(panel_load, expend);
    }//GEN-LAST:event_jToggleButton11ActionPerformed

    private void jToggleButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton12ActionPerformed
        PhoneRepair phoneRepair = new PhoneRepair();
        jpload.jPanelLoader(panel_load, phoneRepair);
    }//GEN-LAST:event_jToggleButton12ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel counter;
    private javax.swing.ButtonGroup home_bnt_grp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JPanel panel_load;
    private javax.swing.JButton poweroff;
    // End of variables declaration//GEN-END:variables
    
    // Custom title bar buttons
    private javax.swing.JButton btnSettings;
    private javax.swing.JButton btnNotifications;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnBackup;
}
