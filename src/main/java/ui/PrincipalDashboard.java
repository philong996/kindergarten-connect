package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Principal Dashboard - Main window for Principal users
 */
public class PrincipalDashboard extends JFrame {
    private AuthService authService;
    private JTabbedPane tabbedPane;
    
    public PrincipalDashboard(AuthService authService) {
        this.authService = authService;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Kindergarten Management System - Principal Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane with different management panels
        createTabs();
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(52, 152, 219));
        
        JLabel titleLabel = new JLabel("Principal Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("Welcome, " + authService.getCurrentUser().getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void createTabs() {
        // Student Management Tab
        StudentManagementPanel studentPanel = new StudentManagementPanel(authService);
        tabbedPane.addTab("Student Management", studentPanel);
        
        // User Management Tab (for Principal only)
        JPanel userPanel = new JPanel();
        userPanel.add(new JLabel("User Management - Coming Soon"));
        tabbedPane.addTab("User Management", userPanel);
        
        // Reports Tab
        JPanel reportsPanel = new JPanel();
        reportsPanel.add(new JLabel("Reports - Coming Soon"));
        tabbedPane.addTab("Reports", reportsPanel);
        
        // Settings Tab
        JPanel settingsPanel = new JPanel();
        settingsPanel.add(new JLabel("Settings - Coming Soon"));
        tabbedPane.addTab("Settings", settingsPanel);
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel);
        
        return statusPanel;
    }
    
    private void setupEventHandlers() {
        // Window closing event
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = JOptionPane.showConfirmDialog(
                    PrincipalDashboard.this,
                    "Are you sure you want to exit?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginWindow().setVisible(true);
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(
            this,
            "Kindergarten Management System\n" +
            "Version 1.0\n" +
            "Developed for educational purposes\n\n" +
            "Features:\n" +
            "- Student Management\n" +
            "- User Authentication\n" +
            "- Role-based Access Control",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
