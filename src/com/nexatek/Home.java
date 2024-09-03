/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.nexatek;

import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    ResultSet rst;
    PreparedStatement pst;
    PreparedStatement pstInsert;
    PreparedStatement pstInsertUpdate;
    ResultSet rstInsertUpdate;

    public Home() {
        conn = connection.connect();
        initComponents();
        SwingUtilities.invokeLater(() -> {
            sales_admin sladmin = new sales_admin(counter.getText());
            jpload.jPanelLoader(panel_load, sladmin);
        });
        
        Timer timer = new Timer(10000, e->
        sendData());
        timer.start();

        setExtendedState(Home.MAXIMIZED_BOTH);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }
    
    public void sendData() {
    String sqlProfit1 = "SELECT products.name AS name, products.productid AS productid, " +
                        "products.cost_price AS unit_cost_price, sub_cost_price.quantity AS initQuantity, " +
                        "sub_cost_price.sub_costp AS total_cost_prices " +
                        "FROM products INNER JOIN sub_cost_price ON products.name = sub_cost_price.product_name";

    String sqlProfitInsert = "INSERT INTO profits (productid, productname, initialquantity, unitcost, totalcostprices, totalsales, stockquantity, profits, profit_date) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String sqlProfitUpdate = "UPDATE profits SET profits = profits + ?, totalsales = ?, stockquantity = ? " +
                             "WHERE productid = ? AND profit_date = ?";

    try {
        // Fetch product details with the join
        PreparedStatement pst = conn.prepareStatement(sqlProfit1);
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

            // Calculate profit for the day
            double profitCalculation = totalSales - totalCostPrice;
            LocalDate profitDate = LocalDate.now();

            // Check if a profit entry already exists for the current day
            String sqlCheckProfit = "SELECT productid FROM profits WHERE productid = ? AND profit_date = ?";
            PreparedStatement pstCheckProfit = conn.prepareStatement(sqlCheckProfit);
            pstCheckProfit.setInt(1, productId);
            pstCheckProfit.setDate(2, java.sql.Date.valueOf(profitDate));
            ResultSet rstCheckProfit = pstCheckProfit.executeQuery();

            if (rstCheckProfit.next()) {
                if(rst.getInt("productid") == rstCheckProfit.getInt("productid") && rstCheckProfit.getString("profit_date") == profitDate.toString()){
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
                
            } else {
                if(rst.getInt("productid") != rstCheckProfit.getInt("productid") && rstCheckProfit.getString("profit_date") != profitDate.toString()){
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
                
            }

            // Close the result sets and statements for each loop iteration
            rstTotalSales.close();
            pstTotalSales.close();
            rstCheckProfit.close();
            pstCheckProfit.close();
        }

        // Close the initial prepared statement and result set
        rst.close();
        pst.close();
    } catch (SQLException ex) {
        Logger.getLogger(counter.class.getName()).log(Level.SEVERE, null, ex);
    }
}

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
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGap(0, 692, Short.MAX_VALUE)
        );
        panel_loadLayout.setVerticalGroup(
            panel_loadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(0, 153, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        jLabel1.setText("LUCKY ELECTRICALS");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 894, Short.MAX_VALUE)
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
}
