package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Principal Page - Main window for Principal users
 * Implements Template Method pattern via BaseAuthenticatedPage
 */
public class PrincipalPage extends BaseAuthenticatedPage {
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private JPanel headerPanel;
    private JPanel statusPanel;
    
    public PrincipalPage(AuthService authService) {
        super(authService);
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Principal Page";
    }
    
    @Override
    protected Dimension getWindowSize() {
        return new Dimension(1000, 700);
    }
    
    @Override
    protected void initializeComponents() {
        tabbedPane = new JTabbedPane();
        menuBar = createMenuBar();
        headerPanel = createHeaderPanel();
        statusPanel = createStatusPanel();
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        // Set menu bar
        setJMenuBar(menuBar);
        
        // Add header panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane with different management panels
        createTabs();
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add status bar
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void setupPermissions() {
        // Principal has full access - no restrictions needed
        // All tabs and features should be available
        String roleDisplay = getCurrentUserRoleDisplay();
        
        // Update header to show role
        JLabel userLabel = new JLabel("Chào mừng " + roleDisplay + " - " + authService.getCurrentUser().getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(userLabel, BorderLayout.EAST);
    }
    
    @Override
    protected void setupEventHandlers() {
        // Event handlers are set up in createMenuBar() and createTabs()
        // Override window closing to use base class behavior
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isPrincipal();
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        logoutItem.addActionListener(e -> performLogout());
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
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
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
