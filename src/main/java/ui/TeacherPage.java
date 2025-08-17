package ui;

import service.AuthService;
import ui.components.HeaderPanel;
import ui.components.ButtonPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Teacher Page - Main window for Teacher users
 * Implements Template Method pattern via BaseAuthenticatedPage
 * Refactored to use reusable UI components
 */
public class TeacherPage extends BaseAuthenticatedPage {
    private HeaderPanel headerPanel;
    private JPanel mainPanel;
    private ButtonPanel buttonPanel;
    
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
        headerPanel = HeaderPanel.createDashboard("Teacher", authService.getCurrentUser().getUsername());
        
        mainPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Teacher features are under development", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(contentLabel, BorderLayout.CENTER);
        
        buttonPanel = new ButtonPanel();
        buttonPanel.addStyledButton("Logout", e -> performLogout(), ButtonPanel.ButtonStyle.SECONDARY);
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void setupPermissions() {
        // Teacher-specific permissions setup
        headerPanel.setStatus("Teacher access - Limited features available");
        
        // Teachers will have access to student management, attendance, etc.
        // This will be expanded when implementing teacher-specific features
    }
    
    @Override
    protected void setupEventHandlers() {
        // Event handlers are already set up in initializeComponents
        // The logout button handler is configured in buttonPanel
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isTeacher();
    }
}
