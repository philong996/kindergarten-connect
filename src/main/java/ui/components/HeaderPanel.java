package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable header panel component for consistent page headers
 * throughout the application
 */
public class HeaderPanel extends JPanel {
    private JLabel titleLabel;
    private JLabel userInfoLabel;
    private JLabel statusLabel;
    
    public HeaderPanel(String title) {
        this(title, null, null);
    }
    
    public HeaderPanel(String title, String userInfo) {
        this(title, userInfo, null);
    }
    
    public HeaderPanel(String title, String userInfo, String status) {
        initializeComponents(title, userInfo, status);
        setupLayout();
        setupStyling();
    }
    
    private void initializeComponents(String title, String userInfo, String status) {
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        if (userInfo != null) {
            userInfoLabel = new JLabel(userInfo);
            userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            userInfoLabel.setForeground(Color.WHITE);
        }
        
        if (status != null) {
            statusLabel = new JLabel(status);
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            statusLabel.setForeground(Color.LIGHT_GRAY);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Title on the left
        add(titleLabel, BorderLayout.WEST);
        
        // User info and status on the right
        if (userInfoLabel != null || statusLabel != null) {
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            
            if (userInfoLabel != null) {
                rightPanel.add(userInfoLabel, BorderLayout.NORTH);
            }
            
            if (statusLabel != null) {
                rightPanel.add(statusLabel, BorderLayout.SOUTH);
            }
            
            add(rightPanel, BorderLayout.EAST);
        }
    }
    
    private void setupStyling() {
        setBackground(new Color(52, 152, 219)); // Primary blue color
        setOpaque(true);
    }
    
    /**
     * Update the title text
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    /**
     * Update the user info text
     */
    public void setUserInfo(String userInfo) {
        if (userInfoLabel == null) {
            userInfoLabel = new JLabel();
            userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            userInfoLabel.setForeground(Color.WHITE);
            
            // Refresh layout
            removeAll();
            setupLayout();
            revalidate();
            repaint();
        }
        userInfoLabel.setText(userInfo);
    }
    
    /**
     * Update the status text
     */
    public void setStatus(String status) {
        if (statusLabel == null) {
            statusLabel = new JLabel();
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            statusLabel.setForeground(Color.LIGHT_GRAY);
            
            // Refresh layout
            removeAll();
            setupLayout();
            revalidate();
            repaint();
        }
        statusLabel.setText(status);
    }
    
    /**
     * Set the background color of the header
     */
    public void setHeaderColor(Color color) {
        setBackground(color);
    }
    
    /**
     * Set the text color for title and user info
     */
    public void setTextColor(Color color) {
        titleLabel.setForeground(color);
        if (userInfoLabel != null) {
            userInfoLabel.setForeground(color);
        }
    }
    
    /**
     * Get the title label for direct styling if needed
     */
    public JLabel getTitleLabel() {
        return titleLabel;
    }
    
    /**
     * Get the user info label for direct styling if needed
     */
    public JLabel getUserInfoLabel() {
        return userInfoLabel;
    }
    
    /**
     * Get the status label for direct styling if needed
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }
    
    /**
     * Create a simple header with just a title
     */
    public static HeaderPanel createSimple(String title) {
        return new HeaderPanel(title);
    }
    
    /**
     * Create a header with title and user info
     */
    public static HeaderPanel createWithUser(String title, String userInfo) {
        return new HeaderPanel(title, userInfo);
    }
    
    /**
     * Create a full header with title, user info, and status
     */
    public static HeaderPanel createFull(String title, String userInfo, String status) {
        return new HeaderPanel(title, userInfo, status);
    }
    
    /**
     * Create a dashboard header with role-based styling
     */
    public static HeaderPanel createDashboard(String role, String username) {
        String title = role + " Dashboard";
        String userInfo = "Welcome " + role + " - " + username;
        
        HeaderPanel header = new HeaderPanel(title, userInfo);
        
        // Set role-specific colors
        switch (role.toUpperCase()) {
            case "PRINCIPAL":
                header.setHeaderColor(new Color(142, 68, 173)); // Purple
                break;
            case "TEACHER":
                header.setHeaderColor(new Color(39, 174, 96)); // Green
                break;
            case "PARENT":
                header.setHeaderColor(new Color(230, 126, 34)); // Orange
                break;
            default:
                header.setHeaderColor(new Color(52, 152, 219)); // Default blue
                break;
        }
        
        return header;
    }
}
