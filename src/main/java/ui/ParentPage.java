package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Parent Page - Main window for Parent users
 * Implements Template Method pattern via BaseAuthenticatedPage
 */
public class ParentPage extends BaseAuthenticatedPage {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JButton logoutButton;
    
    public ParentPage(AuthService authService) {
        super(authService);
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Parent Page";
    }
    
    @Override
    protected Dimension getWindowSize() {
        return new Dimension(800, 600);
    }
    
    @Override
    protected void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Parent Page - Coming Soon", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        logoutButton = new JButton("Logout");
    }
    
    @Override
    protected void setupLayout() {
        mainPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(logoutButton, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    @Override
    protected void setupPermissions() {
        // Parent-specific permissions setup
        // For now, parents have basic access to view content
        String roleDisplay = getCurrentUserRoleDisplay();
        titleLabel.setText("Chào mừng " + roleDisplay + " - Tính năng đang phát triển");
    }
    
    @Override
    protected void setupEventHandlers() {
        logoutButton.addActionListener(event -> performLogout());
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isParent();
    }
}
