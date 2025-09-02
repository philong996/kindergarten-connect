package ui.pages;

import service.AuthService;
import ui.components.HeaderPanel;
import ui.panels.ClassManagementPanel;
import ui.panels.StudentManagementPanel;
import ui.panels.UserManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Principal Page - Main window for Principal users
 * Implements Template Method pattern via BaseAuthenticatedPage
 * Refactored to use reusable UI components
 */
public class PrincipalPage extends BaseAuthenticatedPage {
    private JTabbedPane tabbedPane;
    private HeaderPanel headerPanel;
    private JPanel statusPanel;
    
    public PrincipalPage(AuthService authService) {
        super(authService);
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Principal Page";
    }
    
    @Override
    protected void initializeComponents() {
        tabbedPane = new JTabbedPane();
        headerPanel = HeaderPanel.createDashboard("Principal", authService.getCurrentUser().getUsername());
        statusPanel = createStatusPanel();
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
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
        
        // Update header to show current status
        headerPanel.setStatus("All permissions granted - Full access");
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

    private void createTabs() {
        // Student Management Tab - using the new refactored panel
        StudentManagementPanel studentPanel = new StudentManagementPanel(authService);
        tabbedPane.addTab("Student Management", studentPanel);
        
        // Class Management Tab - new panel for managing classes
        ClassManagementPanel classPanel = new ClassManagementPanel(authService);
        tabbedPane.addTab("Class Management", classPanel);
        
        // User Management Tab (for Principal only)
        UserManagementPanel userPanel = new UserManagementPanel(authService);
        tabbedPane.addTab("User Management", userPanel);
        
        // Reports Tab
        JPanel reportsPanel = new JPanel();
        reportsPanel.add(new JLabel("Reports - Coming Soon"));
        tabbedPane.addTab("Reports", reportsPanel);
        
        // Settings Tab
        JPanel settingsPanel = new JPanel();
        settingsPanel.add(new JLabel("Settings - Coming Soon"));
        tabbedPane.addTab("Settings", settingsPanel);
        
        // Add change listener to refresh data when tabs are switched
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTitle = tabbedPane.getTitleAt(selectedIndex);
            
            // Refresh Class Management panel when it's selected
            // This ensures the teacher dropdown shows newly created teachers
            if ("Class Management".equals(selectedTitle)) {
                classPanel.refreshData();
            }
            // Refresh User Management panel when it's selected
            else if ("User Management".equals(selectedTitle)) {
                userPanel.refreshData();
            }
        });
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel);
        
        return statusPanel;
    }
}