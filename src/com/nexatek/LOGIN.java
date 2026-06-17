/*
 * Professional Login Page for Lucky Electricals POS
 */
package com.nexatek;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Professional Login Page
 * @author engmartin
 */
public class LOGIN extends javax.swing.JFrame {

    Connection conn;
    ResultSet rst;
    PreparedStatement pst;
    
    private JTextField username;
    private JPasswordField password;
    private JButton loginbtn;
    private JLabel statusLabel;

    public LOGIN() {
        conn = connection.connect();
        initCustomComponents();
    }

    private void initCustomComponents() {
        setUndecorated(true);  // Remove title bar/navbar
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set application icon for taskbar
        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png")).getImage());
        } catch (Exception e) {
            System.out.println("Could not load icon: " + e.getMessage());
        }
        
        // Main container
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        // ============ LEFT PANEL - Branding ============
        JPanel leftPanel = new JPanel() {
            private Image bgImage;
            {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/com/nexatek/images/login.png"));
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
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(15, 32, 65),
                    0, getHeight(), new Color(25, 55, 95)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw background image if available
                if (bgImage != null) {
                    int imgWidth = 280;
                    int imgHeight = 280;
                    int x = (getWidth() - imgWidth) / 2;
                    int y = (getHeight() - imgHeight) / 2 + 20;
                    g2d.drawImage(bgImage, x, y, imgWidth, imgHeight, this);
                }
                
                // Decorative elements
                g2d.setColor(new Color(255, 193, 7, 60));
                g2d.setStroke(new BasicStroke(2));
                // Corner accents
                g2d.drawLine(30, 30, 30, 70);
                g2d.drawLine(30, 30, 70, 30);
                g2d.drawLine(getWidth()-30, 30, getWidth()-30, 70);
                g2d.drawLine(getWidth()-30, 30, getWidth()-70, 30);
                g2d.drawLine(30, getHeight()-30, 30, getHeight()-70);
                g2d.drawLine(30, getHeight()-30, 70, getHeight()-30);
                g2d.drawLine(getWidth()-30, getHeight()-30, getWidth()-30, getHeight()-70);
                g2d.drawLine(getWidth()-30, getHeight()-30, getWidth()-70, getHeight()-30);
                
                // Top accent bar
                GradientPaint goldGradient = new GradientPaint(
                    0, 0, new Color(255, 193, 7),
                    getWidth(), 0, new Color(255, 152, 0)
                );
                g2d.setPaint(goldGradient);
                g2d.fillRect(0, 0, getWidth(), 4);
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        // Branding text on left panel
        JPanel brandingPanel = new JPanel();
        brandingPanel.setOpaque(false);
        brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.Y_AXIS));
        brandingPanel.setBorder(new EmptyBorder(40, 30, 0, 30));
        
        JLabel iconLabel = new JLabel("\u26A1");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(255, 193, 7));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel brandLabel = new JLabel("LUCKY ELECTRICALS");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel tagLabel = new JLabel("Point of Sale System");
        tagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagLabel.setForeground(new Color(255, 193, 7));
        tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        brandingPanel.add(iconLabel);
        brandingPanel.add(Box.createVerticalStrut(10));
        brandingPanel.add(brandLabel);
        brandingPanel.add(Box.createVerticalStrut(5));
        brandingPanel.add(tagLabel);
        
        leftPanel.add(brandingPanel);
        
        // ============ RIGHT PANEL - Login Form ============
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Light gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(236, 240, 243)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setLayout(new GridBagLayout());
        
        // Form container
        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(20, 60, 20, 60));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(30, 45, 65));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue to your dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 120, 140));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(60, 75, 90));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        username = new JTextField();
        username.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        username.setPreferredSize(new Dimension(320, 45));
        username.setMaximumSize(new Dimension(320, 45));
        username.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        username.setBackground(Color.WHITE);
        username.setForeground(new Color(30, 45, 65));
        username.setCaretColor(new Color(40, 160, 90));
        
        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(60, 75, 90));
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        password.setPreferredSize(new Dimension(320, 45));
        password.setMaximumSize(new Dimension(320, 45));
        password.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        password.setBackground(Color.WHITE);
        password.setForeground(new Color(30, 45, 65));
        password.setCaretColor(new Color(40, 160, 90));
        
        // Add Enter key listener to password field
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        
        // Login button
        loginbtn = new JButton("SIGN IN");
        loginbtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginbtn.setForeground(Color.WHITE);
        loginbtn.setBackground(new Color(40, 160, 90));
        loginbtn.setPreferredSize(new Dimension(320, 50));
        loginbtn.setMaximumSize(new Dimension(320, 50));
        loginbtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginbtn.setFocusPainted(false);
        loginbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginbtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Hover effect
        loginbtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginbtn.setBackground(new Color(35, 140, 80));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginbtn.setBackground(new Color(40, 160, 90));
            }
        });
        
        loginbtn.addActionListener(e -> performLogin());
        
        // Status label for errors
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(220, 53, 69));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Forgot password
        JLabel forgotLabel = new JLabel("Forgot username / password?");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(100, 120, 140));
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLabel.setForeground(new Color(40, 160, 90));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotLabel.setForeground(new Color(100, 120, 140));
            }
        });
        
        // Footer
        JLabel footerLabel = new JLabel("\u00A9 2024 Lucky Electricals - Powered by Nexatek");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerLabel.setForeground(new Color(150, 160, 170));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to form
        formContainer.add(welcomeLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(subtitleLabel);
        formContainer.add(Box.createVerticalStrut(35));
        formContainer.add(userLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(username);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(passLabel);
        formContainer.add(Box.createVerticalStrut(8));
        formContainer.add(password);
        formContainer.add(Box.createVerticalStrut(10));
        formContainer.add(statusLabel);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(loginbtn);
        formContainer.add(Box.createVerticalStrut(20));
        formContainer.add(forgotLabel);
        formContainer.add(Box.createVerticalStrut(40));
        formContainer.add(footerLabel);
        
        rightPanel.add(formContainer);
        
        // Add panels to main
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        
        setContentPane(mainPanel);
    }
    
    private void performLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword()).trim();
        
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }
        
        if (conn == null) {
            statusLabel.setText("Database connection failed. Please check the database settings.");
            return;
        }
        
        statusLabel.setText(" ");
        
        String[][] loginQueries = {
            {"SELECT username, password FROM employeestbl WHERE LOWER(username) = LOWER(?) AND password = ?", "employee"},
            {"SELECT username, password FROM users WHERE LOWER(username) = LOWER(?) AND password = ?", "employee"},
            {"SELECT username, password FROM users WHERE LOWER(user_name) = LOWER(?) AND password = ?", "employee"},
            {"SELECT username, password FROM users WHERE LOWER(username) = LOWER(?) AND user_password = ?", "employee"},
            {"SELECT username, password FROM users WHERE LOWER(user_name) = LOWER(?) AND user_password = ?", "employee"},
            {"SELECT username, password FROM users WHERE LOWER(username) = LOWER(?) AND pass = ?", "employee"},
            {"SELECT username, password FROM administrators WHERE LOWER(username) = LOWER(?) AND password = ?", "admin"}
        };
        
        try {
            for (String[] entry : loginQueries) {
                String sql = entry[0];
                String role = entry[1];
                
                try {
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, user);
                    pst.setString(2, pass);
                    rst = pst.executeQuery();
                    
                    if (rst.next()) {
                        String loggedUser = rst.getString("username");
                        if (loggedUser == null || loggedUser.isEmpty()) {
                            loggedUser = rst.getString("user_name");
                        }
                        
                        rst.close();
                        pst.close();
                        rst = null;
                        pst = null;
                        
                        JOptionPane.showMessageDialog(this, "Welcome " + loggedUser + "!");
                        dispose();
                        
                        if ("admin".equals(role)) {
                            Home e = new Home();
                            e.pack();
                            e.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
                            e.counter.setText(loggedUser);
                            e.setLocationRelativeTo(null);
                            e.setVisible(true);
                        } else {
                            counter count = new counter();
                            count.pack();
                            count.counter.setText(loggedUser);
                            count.setLocationRelativeTo(null);
                            count.setVisible(true);
                        }
                        return;
                    }
                } catch (SQLException ex) {
                    // Try the next possible schema variation
                    if (rst != null) {
                        try { rst.close(); } catch (Exception ignored) {}
                        rst = null;
                    }
                    if (pst != null) {
                        try { pst.close(); } catch (Exception ignored) {}
                        pst = null;
                    }
                }
            }
            
            // Invalid credentials
            statusLabel.setText("Invalid username or password");
            password.setText("");
            
        } catch (Exception e) {
            statusLabel.setText("Login error: " + e.getMessage());
        } finally {
            try {
                if (rst != null && !rst.isClosed()) {
                    rst.close();
                }
                if (pst != null && !pst.isClosed()) {
                    pst.close();
                }
            } catch (Exception e) {}
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Use default
        }

        java.awt.EventQueue.invokeLater(() -> {
            new LOGIN().setVisible(true);
        });
    }
}
