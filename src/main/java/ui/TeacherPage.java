package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Teacher Page - Main window for Teacher users
 * Implements Template Method pattern via BaseAuthenticatedPage
 */
public class TeacherPage extends BaseAuthenticatedPage {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JButton logoutButton;
    
    public TeacherPage(AuthService authService) {
        super(authService);
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Teacher Page";
    }
    
    @Override
    protected Dimension getWindowSize() {
        return new Dimension(800, 600);
    }
    
    @Override
    protected void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Teacher Page - Coming Soon", SwingConstants.CENTER);
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
        // Teacher-specific permissions setup
        String roleDisplay = getCurrentUserRoleDisplay();
        titleLabel.setText("Chào mừng " + roleDisplay + " - Tính năng đang phát triển");
        
        // Teachers will have access to student management, attendance, etc.
        // This will be expanded when implementing teacher-specific features
    }
    
    @Override
    protected void setupEventHandlers() {
        logoutButton.addActionListener(event -> performLogout());
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isTeacher();
    }
}
