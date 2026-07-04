/*
 * Professional Login Page for Lucky Electricals POS
 */
package com.nexatek;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import net.miginfocom.swing.MigLayout;

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
        AppTheme.install();
        setUndecorated(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 560));
        setSize(980, 620);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set application icon for taskbar
        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/com/nexatek/images/necxtek logo.png")).getImage());
        } catch (Exception e) {
            System.out.println("Could not load icon: " + e.getMessage());
        }
        
        JPanel mainPanel = new JPanel(new MigLayout("insets 0, fill", "[46%,fill][54%,fill]", "[fill]"));
        mainPanel.putClientProperty(FlatClientProperties.STYLE, "background:#f6f7fb;");
        
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
        leftPanel.setLayout(new MigLayout("insets 42 34 42 34, fill, wrap", "[center]", "[][grow][]"));
        
        // Branding text on left panel
        JPanel brandingPanel = new JPanel();
        brandingPanel.setOpaque(false);
        brandingPanel.setLayout(new MigLayout("insets 0, wrap, align center", "[center]"));
        
        JLabel iconLabel = new JLabel("\u26A1");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(255, 193, 7));
        
        JLabel brandLabel = new JLabel("KEBZ PHONE SERVICE CENTRE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandLabel.setForeground(Color.WHITE);
        
        JLabel tagLabel = new JLabel("Point of Sale System");
        tagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagLabel.setForeground(new Color(255, 193, 7));
        
        brandingPanel.add(iconLabel);
        brandingPanel.add(brandLabel, "gapy 10 0");
        brandingPanel.add(tagLabel, "gapy 3 0");
        
        JLabel sideText = new JLabel("<html><div style='text-align:center;'>Secure sales, repairs, inventory and reporting<br>for your service centre.</div></html>");
        sideText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sideText.setForeground(new Color(205, 220, 235));

        leftPanel.add(brandingPanel, "growx");
        leftPanel.add(sideText, "growx, align center bottom");
        
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
        rightPanel.setLayout(new MigLayout("insets 30 24 30 24, fill", "[center]", "[center]"));
        
        // Form container
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new MigLayout("insets 30 36 28 36, fillx, wrap, width 380:420:460", "[fill]"));
        formContainer.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;"
                + "background:#FFFFFF;"
                + "border:1,1,1,1,#e5e7eb,,8;");
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +15;");
        welcomeLabel.setForeground(new Color(30, 45, 65));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue to your dashboard");
        subtitleLabel.putClientProperty(FlatClientProperties.STYLE, "font:+1;");
        subtitleLabel.setForeground(new Color(100, 120, 140));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(60, 75, 90));
        
        username = new JTextField();
        username.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        username.setBackground(Color.WHITE);
        username.setForeground(new Color(30, 45, 65));
        username.setCaretColor(new Color(40, 160, 90));
        username.putClientProperty(FlatClientProperties.STYLE, AppTheme.roundedFieldStyle());
        AppTheme.putTextFieldPlaceholder(username, "Enter your username");
        
        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(60, 75, 90));
        
        password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        password.setBackground(Color.WHITE);
        password.setForeground(new Color(30, 45, 65));
        password.setCaretColor(new Color(40, 160, 90));
        password.putClientProperty(FlatClientProperties.STYLE, AppTheme.roundedFieldStyle());
        AppTheme.putTextFieldPlaceholder(password, "Enter your password");
        installRevealButton(password);
        
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
        loginbtn.putClientProperty(FlatClientProperties.STYLE, AppTheme.primaryButtonStyle());
        loginbtn.setFocusPainted(false);
        loginbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        loginbtn.addActionListener(e -> performLogin());
        
        // Status label for errors
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(220, 53, 69));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Forgot password
        JLabel forgotLabel = new JLabel("Forgot username / password?");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(100, 120, 140));
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.setHorizontalAlignment(SwingConstants.CENTER);
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLabel.setForeground(Color.decode(AppTheme.ACCENT));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotLabel.setForeground(new Color(100, 120, 140));
            }
        });
        
        // Footer
        JLabel footerLabel = new JLabel("\u00A9 2024 KEBZ PHONE SERVICE CENTRE - Powered by Necxtek");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerLabel.setForeground(new Color(150, 160, 170));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to form
        formContainer.add(welcomeLabel);
        formContainer.add(subtitleLabel, "gapy 4 26");
        formContainer.add(userLabel);
        formContainer.add(username, "h 44!");
        formContainer.add(passLabel, "gapy 12 0");
        formContainer.add(password, "h 44!");
        formContainer.add(statusLabel, "h 18!, gapy 8 8");
        formContainer.add(loginbtn, "h 48!");
        formContainer.add(forgotLabel, "gapy 14 22");
        formContainer.add(footerLabel);
        
        rightPanel.add(formContainer);
        
        // Add panels to main
        mainPanel.add(leftPanel, "grow");
        mainPanel.add(rightPanel, "grow");
        
        setContentPane(mainPanel);
    }

    private void installRevealButton(JPasswordField txt) {
        FlatSVGIcon iconEye = new FlatSVGIcon("com/nexatek/resources/login_register/icon/eye.svg", 0.3f);
        FlatSVGIcon iconHide = new FlatSVGIcon("com/nexatek/resources/login_register/icon/hide.svg", 0.3f);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.putClientProperty(FlatClientProperties.STYLE, "margin:0,0,0,5;background:null;");
        JButton button = new JButton(iconEye);
        button.putClientProperty(FlatClientProperties.STYLE, AppTheme.iconButtonStyle());
        button.setToolTipText("Show password");

        button.addActionListener(new ActionListener() {
            private final char defaultEchoChar = txt.getEchoChar();
            private boolean show;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                show = !show;
                button.setIcon(show ? iconHide : iconEye);
                button.setToolTipText(show ? "Hide password" : "Show password");
                txt.setEchoChar(show ? (char) 0 : defaultEchoChar);
            }
        });
        toolBar.add(button);
        txt.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
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
                            count.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
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
        AppTheme.install();

        java.awt.EventQueue.invokeLater(() -> {
            new LOGIN().setVisible(true);
        });
    }
}
