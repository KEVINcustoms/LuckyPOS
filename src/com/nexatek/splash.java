/*
 * Professional Splash Screen for Lucky Electricals POS
 */
package com.nexatek;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Professional Splash Screen
 * @author mrrobot
 */
public class splash extends javax.swing.JFrame {

    private float opacity = 0f;
    private Timer fadeTimer;
    private JLabel loadingLabel;
    private JProgressBar progressBar;
    private int progress = 0;
    private Timer progressTimer;
    private Timer rotationTimer;
    private double rotationAngle = 0;
    private JPanel rotatingLogoPanel;

    public splash() {
        initCustomComponents();
        startFadeIn();
        startProgressAnimation();
        startLogoRotation();
    }

    private void initCustomComponents() {
        setUndecorated(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 750, 480, 25, 25));
        
        // Set application icon for taskbar
        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png")).getImage());
        } catch (Exception e) {
            System.out.println("Could not load icon: " + e.getMessage());
        }
        
        // Main panel with background image and overlay
        JPanel mainPanel = new JPanel() {
            private Image bgImage;
            
            {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/com/nexatek/images/splash1.png"));
                    bgImage = icon.getImage();
                } catch (Exception e) {
                    bgImage = null;
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                
                // Draw background image
                if (bgImage != null) {
                    g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
                
                // Gradient overlay - darker at bottom for text readability
                GradientPaint overlay = new GradientPaint(
                    0, 0, new Color(0, 20, 40, 180),
                    0, getHeight(), new Color(0, 10, 25, 240)
                );
                g2d.setPaint(overlay);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Decorative top border - gradient gold line
                GradientPaint goldGradient = new GradientPaint(
                    0, 0, new Color(255, 193, 7),
                    getWidth(), 0, new Color(255, 152, 0)
                );
                g2d.setPaint(goldGradient);
                g2d.fillRect(0, 0, getWidth(), 4);
                
                // Decorative corner accents
                g2d.setColor(new Color(255, 193, 7, 100));
                g2d.setStroke(new BasicStroke(2));
                // Top left corner
                g2d.drawLine(20, 30, 20, 60);
                g2d.drawLine(20, 30, 50, 30);
                // Top right corner
                g2d.drawLine(getWidth() - 20, 30, getWidth() - 20, 60);
                g2d.drawLine(getWidth() - 20, 30, getWidth() - 50, 30);
                // Bottom left corner
                g2d.drawLine(20, getHeight() - 30, 20, getHeight() - 60);
                g2d.drawLine(20, getHeight() - 30, 50, getHeight() - 30);
                // Bottom right corner
                g2d.drawLine(getWidth() - 20, getHeight() - 30, getWidth() - 20, getHeight() - 60);
                g2d.drawLine(getWidth() - 20, getHeight() - 30, getWidth() - 50, getHeight() - 30);
                
                // Subtle grid pattern
                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.setStroke(new BasicStroke(1));
                for (int i = 0; i < getWidth(); i += 50) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 50) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(40, 50, 35, 50));

        // ========== TOP SECTION - Branding ==========
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        // Company icon with glow effect
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glow effect
                g2d.setColor(new Color(255, 193, 7, 40));
                g2d.fillOval(getWidth()/2 - 45, 5, 90, 90);
                g2d.setColor(new Color(255, 193, 7, 60));
                g2d.fillOval(getWidth()/2 - 40, 10, 80, 80);
                
                super.paintComponent(g);
            }
        };
        iconLabel.setText("\u26A1");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setForeground(new Color(255, 193, 7));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Main title with shadow
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                
                Font titleFont = new Font("Segoe UI", Font.BOLD, 36);
                g2d.setFont(titleFont);
                
                String title = "LUCKY ELECTRICALS";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(title)) / 2;
                int y = fm.getAscent() + 5;
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(title, x + 2, y + 2);
                
                // Main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(title, x, y);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setPreferredSize(new Dimension(650, 50));
        titlePanel.setMaximumSize(new Dimension(650, 50));
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle with elegant styling
        JLabel subtitleLabel = new JLabel("POINT OF SALE SYSTEM");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 193, 7));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Decorative line under subtitle
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int y = getHeight() / 2;
                
                // Gradient line
                GradientPaint lineGradient = new GradientPaint(
                    centerX - 100, y, new Color(255, 193, 7, 0),
                    centerX, y, new Color(255, 193, 7, 255)
                );
                g2d.setPaint(lineGradient);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(centerX - 100, y, centerX, y);
                
                lineGradient = new GradientPaint(
                    centerX, y, new Color(255, 193, 7, 255),
                    centerX + 100, y, new Color(255, 193, 7, 0)
                );
                g2d.setPaint(lineGradient);
                g2d.drawLine(centerX, y, centerX + 100, y);
                
                // Center diamond
                g2d.setColor(new Color(255, 193, 7));
                int[] xPoints = {centerX, centerX + 6, centerX, centerX - 6};
                int[] yPoints = {y - 6, y, y + 6, y};
                g2d.fillPolygon(xPoints, yPoints, 4);
            }
        };
        linePanel.setOpaque(false);
        linePanel.setPreferredSize(new Dimension(200, 20));
        linePanel.setMaximumSize(new Dimension(200, 20));
        linePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topPanel.add(iconLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(titlePanel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(subtitleLabel);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(linePanel);

        // ========== CENTER SECTION - Tagline ==========
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        JLabel taglineLabel = new JLabel("Your Trusted Business Partner");
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        taglineLabel.setForeground(new Color(180, 200, 220));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Features panel
        JPanel featuresPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        featuresPanel.setOpaque(false);
        
        String[] features = {"✓ Fast Transactions", "✓ Inventory Control", "✓ Reports & Analytics"};
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            featureLabel.setForeground(new Color(150, 180, 200));
            featuresPanel.add(featureLabel);
        }
        
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(taglineLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(featuresPanel);

        // ========== BOTTOM SECTION - Loading with Rotating Logo ==========
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        
        // Rotating logo panel above progress bar
        rotatingLogoPanel = new JPanel() {
            private Image logoImage;
            {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png"));
                    logoImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                } catch (Exception e) {
                    logoImage = null;
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (logoImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    
                    // Rotate around center
                    g2d.rotate(rotationAngle, centerX, centerY);
                    g2d.drawImage(logoImage, centerX - 25, centerY - 25, this);
                }
            }
        };
        rotatingLogoPanel.setOpaque(false);
        rotatingLogoPanel.setPreferredSize(new Dimension(60, 60));
        rotatingLogoPanel.setMaximumSize(new Dimension(60, 60));
        rotatingLogoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress bar with custom styling
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(400, 6));
        progressBar.setMaximumSize(new Dimension(400, 6));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(50, 70, 90));
        progressBar.setForeground(new Color(255, 193, 7));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Loading text
        loadingLabel = new JLabel("Initializing system...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loadingLabel.setForeground(new Color(180, 200, 220));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version and credits
        JPanel creditsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        creditsPanel.setOpaque(false);
        
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(120, 140, 160));
        
        JLabel separatorLabel = new JLabel("|");
        separatorLabel.setForeground(new Color(80, 100, 120));
        
        JLabel poweredLabel = new JLabel("Powered by Nexatek Group");
        poweredLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        poweredLabel.setForeground(new Color(120, 140, 160));
        
        creditsPanel.add(versionLabel);
        creditsPanel.add(separatorLabel);
        creditsPanel.add(poweredLabel);
        
        // Copyright
        JLabel copyrightLabel = new JLabel("\u00A9 2024 Lucky Electricals. All Rights Reserved.");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        copyrightLabel.setForeground(new Color(90, 110, 130));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        bottomPanel.add(rotatingLogoPanel);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(loadingLabel);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(creditsPanel);
        bottomPanel.add(Box.createVerticalStrut(3));
        bottomPanel.add(copyrightLabel);

        // Assemble main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }

    private void startFadeIn() {
        setOpacity(0f);
        fadeTimer = new Timer(20, e -> {
            opacity += 0.05f;
            if (opacity >= 1f) {
                opacity = 1f;
                fadeTimer.stop();
            }
            setOpacity(Math.min(1f, opacity));
        });
        fadeTimer.start();
    }
    
    private void startProgressAnimation() {
        String[] loadingMessages = {
            "Initializing system...",
            "Loading components...",
            "Connecting to database...",
            "Preparing interface...",
            "Almost ready..."
        };
        
        progressTimer = new Timer(80, e -> {
            progress++;
            if (progress <= 100) {
                progressBar.setValue(progress);
                
                // Update loading message
                int messageIndex = Math.min(progress / 20, loadingMessages.length - 1);
                loadingLabel.setText(loadingMessages[messageIndex]);
            }
        });
        progressTimer.start();
    }
    
    private void startLogoRotation() {
        // Rotate the logo continuously
        rotationTimer = new Timer(30, e -> {
            rotationAngle += 0.05;  // Rotation speed
            if (rotationAngle >= 2 * Math.PI) {
                rotationAngle = 0;
            }
            if (rotatingLogoPanel != null) {
                rotatingLogoPanel.repaint();
            }
        });
        rotationTimer.start();
    }

    public static void main(String args[]) {
        // Set look and feel with professional styling
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
            // Professional Button Styling
            UIManager.put("Button.background", new Color(52, 152, 219));
            UIManager.put("Button.foreground", new Color(20, 20, 20));
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("Button.focus", new Color(41, 128, 185));
            UIManager.put("Button.textForeground", new Color(20, 20, 20));
            
            UIManager.put("ToggleButton.background", new Color(52, 152, 219));
            UIManager.put("ToggleButton.foreground", new Color(20, 20, 20));
            UIManager.put("ToggleButton.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("ToggleButton.select", new Color(41, 128, 185));
            UIManager.put("ToggleButton.textForeground", new Color(20, 20, 20));
            
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", new Color(30, 30, 30));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
            
            UIManager.put("Label.foreground", new Color(44, 62, 80));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
            
            UIManager.put("Table.foreground", new Color(30, 30, 30));
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.background", new Color(52, 73, 94));
            
            UIManager.put("ComboBox.foreground", new Color(30, 30, 30));
            UIManager.put("ComboBox.background", Color.WHITE);
            
            UIManager.put("Panel.background", new Color(236, 240, 241));
            
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
        }
        
        final splash spl = new splash();
        
        java.awt.EventQueue.invokeLater(() -> {
            spl.setVisible(true);
        });
        
        // Wait for 8 seconds
        try {
            Thread.sleep(8000);
        } catch (Exception e) {}
        
        // Stop timers and close
        if (spl.progressTimer != null) spl.progressTimer.stop();
        spl.dispose();
        
        SwingUtilities.invokeLater(() -> {
            LOGIN login = new LOGIN();
            login.setVisible(true);
        });
    }
}
