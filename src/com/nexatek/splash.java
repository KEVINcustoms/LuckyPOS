/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.nexatek;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author mrrobot
 */
public class splash extends javax.swing.JFrame {

    /**
     * Creates new form splash
     */
    public splash() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        progressbar = new javax.swing.JProgressBar();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Noto Serif Display SemiCondensed ExtraBold", 2, 14)); // NOI18N
        jLabel1.setText("powered by nexatech group");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 650, 230, 40));
        getContentPane().add(progressbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 610, 700, 20));

        jLabel4.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/splash1.png"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 1050, 580));

        jLabel2.setFont(new java.awt.Font("Noto Sans CJK KR Black", 2, 28)); // NOI18N
        jLabel2.setText("Lucky Electricals Management System");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 610, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

public static void main(String args[]) {
        final splash spl = new splash();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                spl.setVisible(true);
                //new splash().setVisible(true);
            }
        });
        
       LOGIN login = new   LOGIN();
        try{
            for(int i= 0; i<= 100;i++){
        Thread.sleep(80);
        spl.progressbar.setValue(i);
        }
            new splash().setVisible(false);
        }
        catch(Exception e){
        
        }
        new LOGIN().setVisible(false);
        login.setVisible(true);
        spl.dispose();
    }
// the code below is for the timer


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JProgressBar progressbar;
    // End of variables declaration//GEN-END:variables
}
