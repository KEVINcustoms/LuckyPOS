package com.nexatek;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class DashboardContainer extends JPanel {

    public DashboardContainer() {
        setLayout(new MigLayout("insets 0, fill", "[grow,fill]", "[grow,fill]"));
        setBackground(new Color(244, 247, 250));
        setMinimumSize(new Dimension(0, 0));

        JPanel motherPanel = new JPanel(new MigLayout("insets 2, fill", "[grow,fill]", "[grow,fill]"));
        motherPanel.setBackground(new Color(244, 247, 250));
        motherPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        motherPanel.setMinimumSize(new Dimension(0, 0));

        DashboardPanel dashboardPanel = new DashboardPanel();
        dashboardPanel.setMinimumSize(new Dimension(0, 0));
        motherPanel.add(dashboardPanel, "grow");
        add(motherPanel, "grow");
    }
}
