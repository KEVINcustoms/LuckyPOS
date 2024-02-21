/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.nexatek;

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
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;


/**
 *
 * @author mrrobot
 */
public class sales_admin extends javax.swing.JPanel {
 Connection conn;
 ResultSet rst;
 PreparedStatement pst;
 public Double Stcok_qty = 0.0;
 static String customer_name;
  static String telephone_number;
  static Float cash;
 
     public sales_admin() {
        conn= connection.connect();
        initComponents();
        items.getTableHeader().setFont( new Font("segoe UI", Font.BOLD,18));
        items.getTableHeader().setOpaque(true);
        items.getTableHeader().setBackground(new Color(242,242,242));
        items.getTableHeader().setForeground(new Color(0,0,255)); 
        
        
         DefaultTableModel model = new DefaultTableModel();
        items.setModel(model);
          model.addColumn("IID");
        model.addColumn("BARCODE");
        model.addColumn("NAME");
        model.addColumn("QUANTITY");
        model.addColumn("UNIT-PRICE");
        model.addColumn("SUB-TOTAL");
        currentdate();
        calculateTotal();
        InvoiceNumbers();   
        Update_table();
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

public void InvoiceNumbers() {
        String sql = "SELECT MAX(invoice_number) FROM invoice_details";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                int highestInvoiceNumber = rs.getInt(1);
                highestInvoiceNumber++;
                invoice_no.setText(String.valueOf(highestInvoiceNumber));
            } else {
                // Handle the case where there are no records in the table
                invoice_no.setText("No records");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
private void updateCombo(){
          String sql = "select * from products where barcode = '"+barcode.getText()+"'";
          try{
          pst = conn.prepareStatement(sql);
          rst = pst.executeQuery();
          while(rst.next()){
          selectcombo.addItem(rst.getString("price"));
          selectcombo.addItem(rst.getString("price2"));
          selectcombo.addItem(rst.getString("price3"));
          }
          }catch(Exception e){
          JOptionPane.showMessageDialog(null, "An error occurred!");
          }
}

 public void clear(){
        name.setText("");
        size.setText("");
        price.setText("");
        quantity.setText("");
        barcode.setText("");
        id.setText("");
        selectcombo.removeAllItems();
    }

// calculating the Grand total
 public double calculateTotal() {
    double total = 0.0;
    DefaultTableModel model = (DefaultTableModel) items.getModel();
    int rowCount = model.getRowCount();

    for (int i = 0; i < rowCount; i++) {
        // Assuming sub_total is stored as a String in the table
        String subTotalStr = (String) model.getValueAt(i, 5); // 5 is the index of sub_total column

        // Parse the String to double and add it to the total
        try {
            double subTotal = Double.parseDouble(subTotalStr);
            total += subTotal;
            total_amount.setText(String.valueOf(total));
        } catch (NumberFormatException e) {
            // Handle the case where the sub_total value is not a valid double
            e.printStackTrace(); // Or log the error
        }
    }

    return total;
}
 
 //code to print receipt in relation with invoice_number
// public void print_receipt(){
//     try{
// HashMap para = new HashMap();
// para.put("invo_para", quantity.getText());
// ReportView r = new ReportView("C:\\Users\\KEVINcustoms\\Desktop\\lucky_electricals\\nexatek_point_of_sale_java\\src\\reports\\inidreport2.jasper",para);
// r.setVisible(true);
//     }catch(Exception e){
//     
//     }
// }
 public void view_receipt(){
 try{
     JasperDesign jdesign = JRXmlLoader.load("src\\reports\\receipt2.jrxml");
//     JasperDesign jdesign = JRXmlLoader.load("src\\reports\\receipt2.jrxml");
 String query = "select * from solditems where invoice_number = '"+invoice_no.getText()+"'";
 JRDesignQuery updateQuery = new JRDesignQuery();
 updateQuery.setText(query);
 
 jdesign.setQuery(updateQuery);
 
 JasperReport jreport = JasperCompileManager.compileReport(jdesign);
 JasperPrint jprint = JasperFillManager.fillReport(jreport,null,conn);
 
 JasperViewer.viewReport(jprint);
 }   catch (JRException ex) {
         Logger.getLogger(sales_admin.class.getName()).log(Level.SEVERE, null, ex);
     }
 }
 public void stkup(){
 //get all table products id and sell quantity
 DefaultTableModel dt = (DefaultTableModel) items.getModel();
 int rc = dt.getRowCount();
 for(int i = 0; i<rc; i++){
 String bcode = dt.getValueAt(i, 1).toString(); //barcode
 String sell_qty = dt.getValueAt(i, 3).toString();//quantity
     try {
         Statement s = connection.connect().createStatement();
         ResultSet rs = s.executeQuery("select quantity from products where barcode = '"+bcode+"'");
         if(rs.next()){
         Stcok_qty = Double.valueOf(rs.getString("quantity"));
         }
     } catch (SQLException e) {
         System.out.println(e);
     }
     Double st_qty = Stcok_qty;
     Double sel_qty = Double.valueOf(sell_qty);
     Double new_qty = st_qty - sel_qty;
     String nqty = String.valueOf(new_qty);
     try{
     Statement ss = connection.connect().createStatement();
     ss.executeUpdate("update products set quantity = '"+nqty+"' where barcode = '"+bcode+"'");
     }
     catch(Exception e){
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
                    date.setText(" "+year + "/" + (month + 1) + "/" + day);

                    //time
                    int second = cal.get(Calendar.SECOND);
                    int minute = cal.get(Calendar.MINUTE);
                    int hour = cal.get(Calendar.HOUR);
                    time.setText(" "+"0" + hour + ":" + (minute) + ":" + second);
                    //TIME.setEditable(false);
                    //DATE.setEditable(false);
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(LOGIN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    } 
                }
            }
        };
        clock.start();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        invoice_no = new javax.swing.JLabel();
        selectcombo = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        stock_qty = new javax.swing.JLabel();
        counter = new javax.swing.JTextField();
        date = new javax.swing.JTextField();
        time = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        size = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        price = new javax.swing.JTextField();
        quantity = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        barcode = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        items = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        total_amount = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        change = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        products_table = new javax.swing.JTable();
        finish = new javax.swing.JButton();
        add_to_cart = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        removeall = new javax.swing.JButton();
        cashpaid = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel1.setText("RECEIPT NO:");

        invoice_no.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N

        selectcombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Prices" }));
        selectcombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectcomboActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel2.setText("Stock Qty");

        stock_qty.setBackground(new java.awt.Color(0, 0, 0));
        stock_qty.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N

        counter.setFont(new java.awt.Font("Noto Sans CJK SC Black", 1, 18)); // NOI18N
        counter.setText("mm-001");

        date.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N

        time.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        time.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(invoice_no, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(selectcombo, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(counter, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stock_qty, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(time, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(invoice_no, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(selectcombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(counter)
                            .addComponent(date)
                            .addComponent(time))))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stock_qty, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(jLabel2)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel3.setText("Bar Code");

        jLabel4.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel4.setText("Product Name");

        jLabel5.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel5.setText("Unit Price");

        jLabel6.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel6.setText("Quantity");

        name.setFont(new java.awt.Font("Noto Sans", 1, 23)); // NOI18N
        name.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));
        name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameKeyTyped(evt);
            }
        });

        size.setEditable(false);
        size.setFont(new java.awt.Font("Noto Sans", 1, 23)); // NOI18N
        size.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));

        jLabel12.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel12.setText("Product Size");

        price.setEditable(false);
        price.setFont(new java.awt.Font("Noto Sans", 1, 23)); // NOI18N
        price.setText("00.0");
        price.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));

        quantity.setFont(new java.awt.Font("Noto Sans", 1, 23)); // NOI18N
        quantity.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));
        quantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                quantityKeyTyped(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Noto Sans", 1, 14)); // NOI18N
        jLabel7.setText("Product_id");

        id.setEditable(false);
        id.setFont(new java.awt.Font("Noto Sans", 1, 23)); // NOI18N
        id.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));

        barcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeActionPerformed(evt);
            }
        });
        barcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                barcodeKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(price, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                    .addComponent(barcode))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(42, 42, 42)
                        .addComponent(size, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(31, 31, 31)
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(36, 36, 36)
                        .addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(id)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(quantity)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(id, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(size, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(price, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        items.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 0), 1, true));
        items.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "INID", "NAME", "BARCODE", "QTY", "UNIT PRICE", "TOTAL PRICE"
            }
        ));
        items.setRowHeight(40);
        items.setSelectionBackground(new java.awt.Color(51, 255, 51));
        items.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(items);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1039, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel10.setText("Total Amount");

        total_amount.setEditable(false);
        total_amount.setFont(new java.awt.Font("Cantarell", 1, 20)); // NOI18N
        total_amount.setText("00.00");
        total_amount.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        total_amount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                total_amountActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        jLabel11.setText("Balance/Due");

        change.setEditable(false);
        change.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N
        change.setText("00.00");
        change.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        change.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(total_amount)
                    .addComponent(change, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(total_amount, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(change, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        products_table.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 0), 1, true));
        products_table.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        products_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Product Name", "Bar Code", "Size", "Pirce", "XL Price", "XXL Price", "QTY", "SID"
            }
        ));
        products_table.setRowHeight(40);
        products_table.setSelectionBackground(new java.awt.Color(0, 153, 0));
        products_table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                products_tableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(products_table);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 666, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        finish.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        finish.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/img/icons8-samsung-pay-56.png"))); // NOI18N
        finish.setText("Pay");
        finish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishActionPerformed(evt);
            }
        });

        add_to_cart.setBackground(new java.awt.Color(0, 102, 0));
        add_to_cart.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        add_to_cart.setForeground(new java.awt.Color(255, 255, 255));
        add_to_cart.setText("Add to cart");
        add_to_cart.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 0, 0), 1, true));
        add_to_cart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_to_cartActionPerformed(evt);
            }
        });

        remove.setBackground(new java.awt.Color(255, 51, 102));
        remove.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        remove.setForeground(new java.awt.Color(255, 255, 255));
        remove.setText("Remove");
        remove.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 255), 1, true));
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        removeall.setBackground(new java.awt.Color(153, 0, 0));
        removeall.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        removeall.setForeground(new java.awt.Color(255, 255, 255));
        removeall.setText("Remove all");
        removeall.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 0), 1, true));
        removeall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeallActionPerformed(evt);
            }
        });

        cashpaid.setBackground(new java.awt.Color(0, 153, 255));
        cashpaid.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        cashpaid.setText("       Paid Amount");
        cashpaid.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 0), 2, true));
        cashpaid.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cashpaidMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGap(502, 502, 502)
                            .addComponent(cashpaid, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(144, 144, 144)
                            .addComponent(finish, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(add_to_cart, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(remove, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(removeall, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(remove, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(removeall, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cashpaid, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(add_to_cart, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(finish, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 4, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeallActionPerformed
        // TODO add your handling code here:
        if (items.getRowCount() > 0) {
        // Remove all rows
        DefaultTableModel model = (DefaultTableModel) items.getModel();
        model.setRowCount(0);
    } else {
        // Display a message if the table is already empty
        JOptionPane.showMessageDialog(this, "The table is already empty.", "Empty Table", JOptionPane.INFORMATION_MESSAGE);
    }
        clear();
    }//GEN-LAST:event_removeallActionPerformed

    private void total_amountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_total_amountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_total_amountActionPerformed

    private void changeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_changeActionPerformed

    private void nameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameKeyTyped
        // TODO add your handling code here:
         String searchCriteria = name.getText().trim().toLowerCase();

    try {
    // Construct the SQL query for searching
    String sql = "SELECT * FROM products WHERE name LIKE ?";
    pst = conn.prepareStatement(sql);
    // Set the parameters for the prepared statement
    pst.setString(1, "%" + searchCriteria + "%"); // Using "%" for partial matches
    rst = pst.executeQuery();       

       products_table.setModel(DbUtils.resultSetToTableModel(rst));
    
    
} catch (SQLException e) {
    JOptionPane.showMessageDialog(null, e);
} 
  
    }//GEN-LAST:event_nameKeyTyped

    private void add_to_cartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_to_cartActionPerformed
        // TODO add your handling code here: // Retrieve data from text fields
        int stqty = Integer.parseInt(stock_qty.getText());
    int selqty = Integer.parseInt(quantity.getText());
    if(selqty > 0){    
if(selqty<stqty || selqty == stqty){
    String itemId = id.getText();
    String itemBarcode = barcode.getText();
    String itemName = name.getText();
    String itemSize = size.getText();
    String itemPrice = price.getText();
    String itemQuantity = quantity.getText();
    
//    calculating the subtotal
    Float q=Float.valueOf(itemQuantity);
    Float p=Float.valueOf(itemPrice);
    Float t=q*p;
    String sub_total =String.valueOf(t);
    
    
// Create the rowData array with the updated itemName
String itemNameWithSize = itemName + " (" + itemSize + ")";
String[] rowData = {itemId, itemBarcode, itemNameWithSize, itemQuantity, itemPrice, sub_total};
//    String[] rowData = {itemId, itemBarcode, itemName, itemQuantity, itemPrice, sub_total};

    // Add the data to the 'items' table
    DefaultTableModel model = (DefaultTableModel) items.getModel();
    model.addRow(rowData);
    calculateTotal();
    clear();}
    else{
    JOptionPane.showMessageDialog(null, "The quantity you have inserted is bigger or the product is out of stock");
    }
  }
else{
JOptionPane.showMessageDialog(null, "The value you have inserted is invalid please check the value and try again");
}
    }//GEN-LAST:event_add_to_cartActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        // TODO add your handling code here:
        int selectedRowIndex = items.getSelectedRow();
    // Check if a row is selected
    if (selectedRowIndex != -1) {
        // Remove the selected row
        DefaultTableModel model = (DefaultTableModel) items.getModel();
        model.removeRow(selectedRowIndex);
    } else {
        // Display a message if no row is selected
        JOptionPane.showMessageDialog(this, "Please select a row to remove.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
    }
    clear();
    }//GEN-LAST:event_removeActionPerformed

    private void finishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishActionPerformed
        // TODO add your handling code here:
 Float totalamount = Float.valueOf(total_amount.getText());
String Status = null;

if (cash == 0.0) {
    Status = "UnPaid";
} else if (totalamount > cash) {
    Status = "Partial";
} else if (totalamount <= cash) {
    Status = "Paid";
}

LocalDate currentDate = LocalDate.now();

        DefaultTableModel model = (DefaultTableModel) items.getModel();
    int rowCount = model.getRowCount();
    
    // Iterate through the rows and insert data into 'solditems' table
   try {
    String sql = "INSERT INTO solditems (invoice_number, itemId, name, quantity, itemPrice, total,customer_name, customer_phone,sold_by,time,status,selldate) VALUES (?, ?, ?, ?, ?, ?,?,?,?,?,?,?)";
    String invoiceDetailsSql = "INSERT INTO invoice_details (invoice_number) VALUES (?)";
    String customerSql="insert into customers(customer_name,phone_number,invoice_number) values(?,?,?)";
    
    pst = conn.prepareStatement(sql);
    PreparedStatement invoiceDetailsPst = conn.prepareStatement(invoiceDetailsSql);
    PreparedStatement customerPst=conn.prepareStatement(customerSql);

    pst.setString(1,invoice_no.getText());
    pst.setInt(1, Integer.valueOf(invoice_no.getText()));
    invoiceDetailsPst.setInt(1, Integer.valueOf(invoice_no.getText()));
    customerPst.setString(1, customer_name);
    customerPst.setString(2, telephone_number);
    customerPst.setInt(3, Integer.valueOf(invoice_no.getText()));


    for (int i = 0; i < rowCount; i++) {
        pst.setString(2, model.getValueAt(i, 0).toString()); // itemId
        pst.setString(3, model.getValueAt(i, 2).toString()); // itemName
        pst.setInt(4, Integer.parseInt(model.getValueAt(i, 3).toString())); // itemQuantity
        pst.setFloat(5, Float.parseFloat(model.getValueAt(i, 4).toString())); // itemPrice
        pst.setFloat(6,Float.valueOf(total_amount.getText()));
        pst.setString(7, customer_name);
        pst.setString(8, telephone_number);
        pst.setString(9, counter.getText());
        pst.setString(10, time.getText());
        pst.setString(11, Status);
        pst.setObject(12, currentDate);
        pst.executeUpdate();
    }
view_receipt();
stkup();
    invoiceDetailsPst.executeUpdate();
    customerPst.executeUpdate();
    JOptionPane.showMessageDialog(null, "Transaction successful");
    cash = null;
    customer_name = null;
    telephone_number = null;
    model.setRowCount(0);
    total_amount.setText("");
    change.setText("");

} catch (SQLException e) {
    JOptionPane.showMessageDialog(null, e);
}

       InvoiceNumbers(); 
    }//GEN-LAST:event_finishActionPerformed

    private void quantityKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantityKeyTyped
        // TODO add your handling code here:    
       /*  try{
        
        Float qty;
        if(quantity.getText()==""){
            qty=0f;
        }
        else{
            Float price_=Float.valueOf(price.getText());
            qty=Float.valueOf(quantity.getText());
            Float amnt= qty * price_;
            String amount=String.valueOf(amnt);
            total_amount.setText(amount);
        }
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
    }//GEN-LAST:event_quantityKeyTyped
   */
    }
    private void itemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemsMouseClicked
        // TODO add your handling code here:
        try {
            DefaultTableModel t = (DefaultTableModel) products_table.getModel();
            int i = products_table.getSelectedRow();
            id.setText(t.getValueAt(i, 0).toString());
            barcode.setText(t.getValueAt(i, 1).toString());
            name.setText(t.getValueAt(i, 2).toString());
            size.setText(t.getValueAt(i, 3).toString());
            price.setText(t.getValueAt(i, 4).toString());
            quantity.setText("1");
            //quantity.setText(t.getValueAt(i, 5).toString());

        } catch (Exception e) {
        }
    }//GEN-LAST:event_itemsMouseClicked

    private void products_tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_products_tableMouseClicked
      
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
        
        // Add the prices to the selectcombo
        selectcombo.removeAllItems();
        selectcombo.addItem(t.getValueAt(i, 4).toString()); // price
        selectcombo.addItem(t.getValueAt(i, 5).toString()); // price2
        selectcombo.addItem(t.getValueAt(i, 6).toString()); // price3
    } catch (Exception e) {
        e.printStackTrace();
    }
    }//GEN-LAST:event_products_tableMouseClicked

    private void selectcomboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectcomboActionPerformed
        // TODO add your handling code here:
       price.setText(selectcombo.getSelectedItem().toString());
    }//GEN-LAST:event_selectcomboActionPerformed

    private void cashpaidMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cashpaidMouseClicked
        // TODO add your handling code here:
        customer_name =JOptionPane.showInputDialog("Customer's Name: ");
        telephone_number =JOptionPane.showInputDialog("Phone Number: ");
         cash=Float.valueOf(JOptionPane.showInputDialog("Enter Cash Brought!"));
        Float total_=Float.valueOf(total_amount.getText());
        change.setText(String.valueOf(cash-total_));
    }//GEN-LAST:event_cashpaidMouseClicked

    private void timeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_timeActionPerformed

    private void barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_barcodeActionPerformed

    private void barcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barcodeKeyPressed
if(evt.getKeyCode() == KeyEvent.VK_ENTER)
     {
     String brcode = barcode.getText().toLowerCase();
         try {
             Statement s = connection.connect().createStatement();
             ResultSet rs = s.executeQuery("select * from products where barcode = '"+brcode+"'");  
             
             if(rs.next()){
                  String ProductName = rs.getString("name");
             String retail_price = rs.getString("price");
             String productSize = rs.getString("size");
             String quantity = rs.getString("quantity");
             String prodId = rs.getString("productid");
             stock_qty.setText(quantity);
             price.setText(retail_price);
             name.setText(ProductName);
             size.setText(productSize);
             id.setText(prodId);
             updateCombo();
             }
             else{
            
             JOptionPane.showMessageDialog(this, "Barcode not Found");
             
             }
         } catch (Exception e) {
         }
     }        // TODO add your handling code here:
    }//GEN-LAST:event_barcodeKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_to_cart;
    private javax.swing.JTextField barcode;
    private javax.swing.JLabel cashpaid;
    private javax.swing.JTextField change;
    private javax.swing.JTextField counter;
    private javax.swing.JTextField date;
    private javax.swing.JButton finish;
    private javax.swing.JTextField id;
    private javax.swing.JLabel invoice_no;
    private javax.swing.JTable items;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField name;
    private javax.swing.JTextField price;
    private javax.swing.JTable products_table;
    private javax.swing.JTextField quantity;
    private javax.swing.JButton remove;
    private javax.swing.JButton removeall;
    private javax.swing.JComboBox<String> selectcombo;
    private javax.swing.JTextField size;
    private javax.swing.JLabel stock_qty;
    private javax.swing.JTextField time;
    private javax.swing.JTextField total_amount;
    // End of variables declaration//GEN-END:variables
}
