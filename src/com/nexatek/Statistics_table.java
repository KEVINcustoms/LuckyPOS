/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.nexatek;

import java.awt.Component;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author KEVINcustoms
 */
public class Statistics_table extends javax.swing.JFrame {
Connection conn;
    PreparedStatement pst;
    ResultSet rst;
    DefaultTableModel model;
    /**
     * Creates new form Statistics_table
     */
    public Statistics_table() {
        conn = connection.connect();
        initComponents();
        profitsCalculation();
        addProfits();
        
    }
    
    
       public void profitsCalculation() {
        model = new DefaultTableModel();
        model.addColumn("Product Id");
        model.addColumn("Product Name");
        model.addColumn("Initial Quantity");
        model.addColumn("Unit Cost Price");
        model.addColumn("Total Cost Prices");
        model.addColumn("Total Sales");
        model.addColumn("Stock Quantity");
        model.addColumn("Profits");

        // For the profits calculations
        //String sqlprofit1 = "SELECT name,productid FROM products";
        String sqlprofit1 = "select products.name as name,products.productid as productid,products.cost_price as unit_cost_price,sub_cost_price.quantity as initQuantity,sub_cost_price.sub_costp as total_cost_prices from products inner join sub_cost_price on products.name = sub_cost_price.product_name";
        try {
            pst = conn.prepareStatement(sqlprofit1);
            rst = pst.executeQuery();
            while (rst.next()) {
                String productName = rst.getString("name");
                int productId = rst.getInt("productid");
                int initialQuantity = rst.getInt("initQuantity");
                double total_cost_price = rst.getDouble("total_cost_prices");
                double unitCostPrice = rst.getDouble("unit_cost_price");
                

                //Now use the productNames to get all the results matching the productNames
                String sqlproductDetails = "SELECT cost_price, quantity FROM products WHERE name = ?";
                
                //Am now retrieving everything matching the productid of products table from solditems table
                String sqlproductDetails2 = "SELECT sum(paid_amount) as totalsales from solditems where itemid = ?";
                
                /*//Am now retrieving the quantity from the sub_cost_price table
                String sqlproductDetails3 = "SELECT quantity from sub_cost_price where product_name = ?";*/
                
                    
                PreparedStatement pstProductDetails = conn.prepareStatement(sqlproductDetails);
                pstProductDetails.setString(1, productName);
                
                // This is the prepared statement to deal with String sqlproductDetails2
                    PreparedStatement pstProductDetails2 = conn.prepareStatement(sqlproductDetails2);
                    pstProductDetails2.setInt(1, productId);
                    
                // These are now the resultsets to handle the data
                ResultSet rstProductDetails = pstProductDetails.executeQuery();
                    ResultSet rstProductDetails2 = pstProductDetails2.executeQuery();
                    //ResultSet rstProductDetails3 = pstProductDetails3.executeQuery();
                if(rstProductDetails2.next()){
                double TotalSales = rstProductDetails2.getDouble("totalsales");
                
                }
                /*if(rstProductDetails3.next()){
                int quantit = rstProductDetails3.getInt("quantity");
                }*/
                if (rstProductDetails.next()) {
                    double cost_prices = rstProductDetails.getDouble("cost_price");
                    int quantities = rstProductDetails.getInt("quantity");
                    double profitCalculation = (rstProductDetails2.getDouble("totalsales"))- total_cost_price;
                    Vector<Object> row = new Vector<>();
                    row.add(productId);
                    row.add(productName);
                    row.add(initialQuantity);
                    row.add(unitCostPrice);
                    row.add(total_cost_price);
                    row.add(rstProductDetails2.getDouble("totalsales"));
                    row.add(quantities);
                    row.add(profitCalculation);
                    model.addRow(row);

                    // Log the retrieved data
                    Logger.getLogger(Statistics_table.class.getName()).log(Level.INFO, "Product: {0}, Cost Price: {1}, Quantity: {2}", new Object[]{productName, cost_prices, quantities});
                }
                rstProductDetails.close();
                pstProductDetails.close();
            }
            rst.close();
            pst.close();
        } catch (SQLException ex) {
            Logger.getLogger(counter.class.getName()).log(Level.SEVERE, null, ex);
        }

        statisticstbl.setModel(model);
        statisticstbl.setDefaultRenderer(Object.class, new CustomCellRenderer());

        // Revalidate and repaint the frame to show the updated UI
        revalidate();
        repaint();
    }

    static class CustomCellRenderer extends DefaultTableCellRenderer {
        public CustomCellRenderer() {
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table2, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table2, value, isSelected, hasFocus, row, column);
            if (column == 7) {
                // Price column
                double profit = (Double) value;
                if (profit <=0) {
                    setBackground(Color.RED);
                } else {
                    setBackground(Color.GREEN);
                }
            } else if (column == 6) {
                // Quantity Column
                int quantity = (Integer) value;
                if (quantity > 50) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                }
            } else {
                setBackground(Color.WHITE);
            }
            if (isSelected) {
                setBackground(table2.getSelectionBackground());
                setForeground(table2.getSelectionForeground());
            } else {
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
    public void addProfits(){
        double sum = 0;
        model = (DefaultTableModel) statisticstbl.getModel();
        for(int row=1;row<model.getRowCount();row++){
        //starting from 1 to skip the header row
        Object value = model.getValueAt(row, 7);
        if(value instanceof Number){
            double numericValue = ((Number)value).doubleValue();
            if(numericValue>=0){
                sum += numericValue;
                totalprofits.setText(String.valueOf(sum));
            }
        }else{
        try{
            double numericValue=Double.parseDouble(value.toString());
            if(numericValue>=0){
            sum +=numericValue;
            totalprofits.setText(String.valueOf(sum));
            }
        }catch(NumberFormatException e){
        //Handle or long invalid number format
        }
        }
        }
        
    }
    
    public void sendData(){
    DefaultTableModel model = (DefaultTableModel) statisticstbl.getModel();
    int rowCount = model.getRowCount();
    try {
        String sql = "insert into profits(productid,productname,initialquantity,unitcost,totalcostprices,totalsales,stockquantity,profits,date) values(?,?,?,?,?,?,?,?)";

        pst = conn.prepareStatement(sql);
        
        for (int i = 0; i < rowCount; i++){
        int productid = Integer.parseInt(model.getValueAt(i, 0).toString());
        String productname = model.getValueAt(i, 1).toString();
        int initialquantity = Integer.parseInt(model.getValueAt(i, 2).toString());
        Float unitcost = Float.valueOf(model.getValueAt(i, 3).toString());
        float totalcostprices = Float.valueOf(model.getValueAt(i, 4).toString());
        float totalsales = Float.valueOf(model.getValueAt(i, 5).toString());
        int stockquantity = Integer.parseInt(model.getValueAt(i, 6).toString());
        float profits = Float.valueOf(model.getValueAt(i, 7).toString());
        pst.setInt(1, productid);
        pst.setString(2, productname);
        pst.setInt(3, initialquantity);
        pst.setFloat(4, unitcost);
        pst.setFloat(5, totalcostprices);
        pst.setFloat(6, totalsales);
        pst.setInt(7, stockquantity);
        pst.setFloat(8, profits);
        pst.executeUpdate(); 
    }  
        JOptionPane.showMessageDialog(null, "Data inserted sucessfully");   
    } catch (SQLException ex) {
        Logger.getLogger(Statistics_table.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    
    
    
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        statisticstbl = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        totalprofits = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("STATISTICS TABLE FOR LUCKY ELECTRICALS");

        statisticstbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(statisticstbl);

        jLabel2.setText("TOTAL PROFITS MADE");

        totalprofits.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N

        jButton1.setText("test button");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalprofits, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(33, 33, 33)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1020, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(totalprofits, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        sendData();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        java.awt.EventQueue.invokeLater(() -> new Statistics_table().setVisible(true));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable statisticstbl;
    private javax.swing.JLabel totalprofits;
    // End of variables declaration//GEN-END:variables

}