/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.nexatek;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import static java.lang.Float.valueOf;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author mrrobot
 */
public class product extends javax.swing.JPanel {

    Connection conn;
    ResultSet rst;
    PreparedStatement pst;
    public product() {
        conn= connection.connect();
        initComponents();
        damaged.setVisible(false);
        Update_table();
        view_damaged();
        sendOutOfStockData();
         products_table.getTableHeader().setFont( new Font("segoe UI", Font.BOLD,18));
        products_table.getTableHeader().setOpaque(true);
        products_table.getTableHeader().setBackground(new Color(242,242,242));
        products_table.getTableHeader().setForeground(new Color(0,0,255)); 
    }

   private void  Update_table(){
        try{
    String sql = "select * from products";
    pst = conn.prepareStatement(sql);
    rst = pst.executeQuery();
    products_table.setModel(DbUtils.resultSetToTableModel(rst));
    }
    catch(Exception e){
        JOptionPane.showMessageDialog(null, e);
    }
    finally{
        try{
          
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex);
        }
    }
   }
   
    private void  view_damaged(){
        try{
    String sql = "select * from damagedproducts";
    pst = conn.prepareStatement(sql);
    rst = pst.executeQuery();
    damaged.setVisible(true);
    damaged.setModel(DbUtils.resultSetToTableModel(rst));
    }
    catch(Exception e){
        JOptionPane.showMessageDialog(null, e);
    }
    finally{
        try{
          
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex);
        }
    }
   }
   
    public void clear(){
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
    public void sendOutOfStockData() {
    String selectQuery = "SELECT * FROM products";
    try {
        pst = conn.prepareStatement(selectQuery);
        rst = pst.executeQuery();

        while (rst.next()) {
            String productid = rst.getString("productid");
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

            if ("0".equals(quantity)) { // Check if quantity is zero
                try {
                    String deleteQuery = "DELETE FROM products WHERE quantity = ?";
                    String insertQuery = "INSERT INTO out_of_stock (barcode, name, size, price, price2, price3, quantity, category, supplier_id, cost_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    pst = conn.prepareStatement(deleteQuery);
                    pst.setFloat(1, qty);
                    pst.execute();

                    pst = conn.prepareStatement(insertQuery);
                    pst.setString(1, barcode);
                    pst.setString(2, name);
                    pst.setString(3, size);
                    pst.setString(4, price);
                    pst.setString(5, price2);
                    pst.setString(6, price3);
                    pst.setString(7, quantity);
                    pst.setString(8, category);
                    pst.setString(9, supplier_id);
                    pst.setString(10, cost_price);
                    pst.execute();

                    JOptionPane.showMessageDialog(null, "Deleted and Inserted into out_of_stock");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
            }
        }
        // to update ma table here
    } catch (SQLException ex) {
        Logger.getLogger(product.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            // Close resources (result set, prepared statement) in a finally block
            if (rst != null) {
                rst.close();
            }
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException e) {
            // Handle exceptions during closing resources
            e.printStackTrace();
        }
    }
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        product_name = new javax.swing.JTextField();
        product_size = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        save = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        update = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        price = new javax.swing.JTextField();
        quantity = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        category = new javax.swing.JTextField();
        supplier_id = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        barcode = new javax.swing.JTextField();
        save_damage = new javax.swing.JButton();
        view_damage = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        price2 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        price3 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cost_price = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        products_table = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        search_bar = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        damaged = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel1.setText("Name:");

        product_name.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N

        product_size.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        product_size.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                product_sizeActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel2.setText("Product_size:");

        save.setBackground(new java.awt.Color(0, 153, 255));
        save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/save.png"))); // NOI18N
        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(0, 153, 51));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/search x30.png"))); // NOI18N
        jButton2.setText("Search");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        delete.setBackground(new java.awt.Color(204, 0, 0));
        delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/delete.png"))); // NOI18N
        delete.setText("Delete");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        update.setBackground(new java.awt.Color(255, 204, 102));
        update.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/update.png"))); // NOI18N
        update.setText("Update");
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel5.setText("Retail_P:");

        price.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N

        quantity.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        quantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantityActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel6.setText("Quantity:");

        jLabel7.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel7.setText("Category:");

        category.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        category.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryActionPerformed(evt);
            }
        });

        supplier_id.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        supplier_id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supplier_idActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel8.setText("Supplier ID:");

        jLabel9.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel9.setText("Bar_code:");

        barcode.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        barcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeActionPerformed(evt);
            }
        });

        save_damage.setBackground(new java.awt.Color(0, 153, 255));
        save_damage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/damage.png"))); // NOI18N
        save_damage.setText("Save As Damage");
        save_damage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_damageActionPerformed(evt);
            }
        });

        view_damage.setBackground(new java.awt.Color(0, 153, 51));
        view_damage.setText("View Damaged");
        view_damage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view_damageActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel10.setText("XL_P:");

        price2.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel11.setText("XXL_P:");

        price3.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel13.setText("Cost_P:");

        cost_price.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        cost_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cost_priceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(254, 254, 254))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(price2, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                                    .addComponent(price3)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(5, 5, 5)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(product_name)
                                    .addComponent(price))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(quantity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                            .addComponent(product_size, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(supplier_id)
                            .addComponent(category, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(185, 185, 185)
                                .addComponent(view_damage, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cost_price, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(18, 18, 18)
                                        .addComponent(barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(save_damage, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(product_size, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cost_price, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(supplier_id, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(category, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(view_damage, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(6, 6, 6))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(price2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(12, 12, 12))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(product_name, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(price, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(68, 68, 68)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(price3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(save_damage, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        products_table.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 0), 1, true));
        products_table.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        products_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Product Name", "Bar Code", "Price", "Qty", "SID"
            }
        ));
        products_table.setRowHeight(40);
        products_table.setSelectionBackground(new java.awt.Color(0, 153, 0));
        products_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                products_tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(products_table);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        search_bar.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        search_bar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                search_barKeyTyped(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel3.setText("Search ID:");

        jLabel4.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel4.setText("Product Info");

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(56, 56, 56)
                        .addComponent(search_bar, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(254, 254, 254)
                        .addComponent(jButton1)))
                .addContainerGap(355, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(search_bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(19, 19, 19))))
        );

        damaged.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(damaged);

        jLabel12.setFont(new java.awt.Font("Noto Sans", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 0, 0));
        jLabel12.setText("DAMAGES");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(445, 445, 445)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1043, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1045, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 891, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void product_sizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_product_sizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_product_sizeActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        // TODO add your handling code here:
        String qty=quantity.getText();
        String s_id=supplier_id.getText();
        float prc = Float.valueOf(price.getText());
        float prc2 = Float.valueOf(price2.getText());
        float prc3 = Float.valueOf(price3.getText());
        float costp = Float.valueOf(cost_price.getText());
        try{
            String sql= "Insert into products (barcode,name,size,price,price2,price3,quantity,category, supplier_id, cost_price) values(?,?,?,?,?,?,?,?,?,?)";
            pst= conn.prepareStatement(sql);
            pst.setString(1,barcode.getText().toLowerCase());
            pst.setString(2,product_name.getText().toLowerCase());
            pst.setString(3,product_size.getText().toLowerCase());
            //pst.setString(4, price.getText());
            pst.setFloat(4, prc);
            pst.setFloat(5, prc2);
            pst.setFloat(6, prc3);
            pst.setInt(7, Integer.parseInt(qty));
            pst.setString(8, category.getText());
            pst.setInt(9,Integer.valueOf(s_id));
            pst.setFloat(10, costp);
            pst.execute();
            JOptionPane.showMessageDialog(null,"Successful");
            products_table.setModel(DbUtils.resultSetToTableModel(rst));
        }
        catch(SQLException | HeadlessException e){
            JOptionPane.showMessageDialog(null,e);
        }
        Update_table();

        clear();  
    }//GEN-LAST:event_saveActionPerformed

    private void quantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quantityActionPerformed

    private void categoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_categoryActionPerformed

    private void supplier_idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supplier_idActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_supplier_idActionPerformed

    private void barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_barcodeActionPerformed

    private void save_damageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_damageActionPerformed
        // TODO add your handling code here:
         String qty=quantity.getText();
        String s_id=supplier_id.getText();
        float prc = Float.valueOf(price.getText());
        try{
            String sql= "Insert into damagedproducts (barcode,name,size,price,quantity,category, supplier_id) values(?,?,?,?,?,?,?)";
            pst= conn.prepareStatement(sql);
            pst.setString(1,barcode.getText());
            pst.setString(2,product_name.getText());
            pst.setString(3,product_size.getText());
            //pst.setString(4, price.getText());
            pst.setFloat(4, prc);
            pst.setInt(5, Integer.parseInt(qty));
            pst.setString(6, category.getText());
            pst.setInt(7,Integer.valueOf(s_id));
            pst.execute();
            JOptionPane.showMessageDialog(null,"Product Added to Damaged");
            products_table.setModel(DbUtils.resultSetToTableModel(rst));
        }
        catch(SQLException | HeadlessException e){
            JOptionPane.showMessageDialog(null,e);
        }
        Update_table();

        clear();  
    }//GEN-LAST:event_save_damageActionPerformed

    private void search_barKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_barKeyTyped
        // TODO add your handling code here:
        String searchCriteria = search_bar.getText().trim();

    try {
        // Construct the SQL query for searching
        String sql = "SELECT * FROM products WHERE barcode LIKE ? OR name LIKE ? OR category LIKE ?";
        pst = conn.prepareStatement(sql);

        // Set the parameters for the prepared statement
        pst.setString(1, "%" + searchCriteria + "%"); // Using "%" for partial matches
        pst.setString(2, "%" + searchCriteria + "%");
        pst.setString(3, "%" + searchCriteria + "%");
        

        // Execute the query and update the JTable
        rst = pst.executeQuery();
        products_table.setModel(DbUtils.resultSetToTableModel(rst));
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e);
    }
    }//GEN-LAST:event_search_barKeyTyped

    private void products_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_products_tableMouseClicked
        // TODO add your handling code here:
           try {
            DefaultTableModel t = (DefaultTableModel) products_table.getModel();
    int i = products_table.getSelectedRow();
//    BARCODE.setText(t.getValueAt(i, 0).toString());
    search_bar.setText(t.getValueAt(i, 0).toString());
    barcode.setText(t.getValueAt(i, 1).toString());
    product_name.setText(t.getValueAt(i, 2).toString());
    product_size.setText(t.getValueAt(i, 3).toString());
    price.setText(t.getValueAt(i, 4).toString());
    price2.setText(t.getValueAt(i, 5).toString());
    price3.setText(t.getValueAt(i, 6).toString());
    quantity.setText(t.getValueAt(i, 7).toString());
    category.setText(t.getValueAt(i, 8).toString());
    supplier_id.setText(t.getValueAt(i, 9).toString());
    cost_price.setText(t.getValueAt(i, 10).toString());
    
    
        } catch (Exception e) {
        }
    }//GEN-LAST:event_products_tableMouseClicked

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        // TODO add your handling code here:
        try{
      String sql= "Delete from products where productid=?";
      pst= conn.prepareStatement(sql);
      pst.setInt(1, Integer.valueOf(search_bar.getText())); 
      
      pst.execute();
      JOptionPane.showMessageDialog(null,"Deleted");
    }
    catch(Exception e){
JOptionPane.showMessageDialog(null,e);
}
     Update_table(); 
     clear();
      
    }//GEN-LAST:event_deleteActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        // TODO add your handling code here:
        int id_=Integer.valueOf(search_bar.getText());
        String qty=quantity.getText();
        String s_id=supplier_id.getText();
        float prc = Float.valueOf(price.getText());
        float prc2 = Float.valueOf(price2.getText());
        float prc3 = Float.valueOf(price3.getText());
        float costP = Float.valueOf(cost_price.getText());
        try{
       String name = product_name.getText();
       String Category = category.getText();
       String size= product_size.getText();
       String sql= "update products set name = '"+name+"', category ='"+Category+"',price='"+prc+"',price2='"+prc2+"',price3='"+prc3+"',quantity='"+qty+"', size='"+size+"',cost_price='"+costP+"' where productid='"+id_+"' ";          
       pst= conn.prepareStatement(sql);
       pst.execute();
       JOptionPane.showMessageDialog(null,"Changes Tracked Successfully");
      }
      catch(Exception e){
	JOptionPane.showMessageDialog(null,e);
      }
        Update_table();
        clear();
                  
    }//GEN-LAST:event_updateActionPerformed

    private void view_damageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view_damageActionPerformed
        // TODO add your handling code here:
        
        view_damaged();
        
    }//GEN-LAST:event_view_damageActionPerformed

    private void cost_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cost_priceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cost_priceActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField barcode;
    private javax.swing.JTextField category;
    private javax.swing.JTextField cost_price;
    private javax.swing.JTable damaged;
    private javax.swing.JButton delete;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField price;
    private javax.swing.JTextField price2;
    private javax.swing.JTextField price3;
    private javax.swing.JTextField product_name;
    private javax.swing.JTextField product_size;
    private javax.swing.JTable products_table;
    private javax.swing.JTextField quantity;
    private javax.swing.JButton save;
    private javax.swing.JButton save_damage;
    private javax.swing.JTextField search_bar;
    private javax.swing.JTextField supplier_id;
    private javax.swing.JButton update;
    private javax.swing.JButton view_damage;
    // End of variables declaration//GEN-END:variables
}
