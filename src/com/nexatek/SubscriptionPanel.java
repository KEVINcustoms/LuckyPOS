package com.nexatek;

import com.raven.model.Model_Data;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class SubscriptionPanel extends javax.swing.JPanel {

    public SubscriptionPanel() {
        initComponents();
        panelPricing.setPrice("LITE", 29000);
        panelPricing.addItem(new Model_Data(true, "Sales and invoice management"));
        panelPricing.addItem(new Model_Data(true, "Product and supplier records"));
        panelPricing.addItem(new Model_Data(true, "Customer list and basic reports"));
        panelPricing.addItem(new Model_Data(true, "Single active cashier"));
        panelPricing.addItem(new Model_Data(false, "Advanced analytics dashboard"));
        panelPricing.addEventBuy(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(SubscriptionPanel.this, "Buy LITE");
            }
        });

        panelPricing1.setPrice("PRO", 99000);
        panelPricing1.addItem(new Model_Data(true, "Everything in Lite"));
        panelPricing1.addItem(new Model_Data(true, "Phone repair workflow"));
        panelPricing1.addItem(new Model_Data(true, "Dashboard charts and stock health"));
        panelPricing1.addItem(new Model_Data(true, "Out-of-stock alerts"));
        panelPricing1.addItem(new Model_Data(true, "Up to 3 active users"));
        panelPricing1.addEventBuy(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(SubscriptionPanel.this, "Buy PRO");
            }
        });

        panelPricing2.setPrice("BUSINESS", 199000);
        panelPricing2.addItem(new Model_Data(true, "Everything in Pro"));
        panelPricing2.addItem(new Model_Data(true, "Unlimited users"));
        panelPricing2.addItem(new Model_Data(true, "Multi-counter operations"));
        panelPricing2.addItem(new Model_Data(true, "Backup and restore assistance"));
        panelPricing2.addItem(new Model_Data(true, "Priority support"));
        panelPricing2.addEventBuy(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(SubscriptionPanel.this, "Buy BUSINESS");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        panelPricing = new com.raven.component.PanelPricing();
        panelPricing1 = new com.raven.component.PanelPricing();
        panelPricing2 = new com.raven.component.PanelPricing();

        panelPricing1.setColor1(new java.awt.Color(196, 104, 250));
        panelPricing1.setColor2(new java.awt.Color(104, 22, 181));
        panelPricing2.setColor1(new java.awt.Color(41, 128, 185));
        panelPricing2.setColor2(new java.awt.Color(25, 42, 65));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(panelPricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(panelPricing1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(panelPricing2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelPricing2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelPricing1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelPricing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(115, Short.MAX_VALUE))
        );
    }

    private com.raven.component.PanelPricing panelPricing;
    private com.raven.component.PanelPricing panelPricing1;
    private com.raven.component.PanelPricing panelPricing2;
}
