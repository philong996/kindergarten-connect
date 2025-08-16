package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Teacher Page placeholder
 */
public class TeacherPage extends JFrame {
    private AuthService authService;
    
    public TeacherPage(AuthService authService) {
        this.authService = authService;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("Teacher Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Teacher Page - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authService.logout();
            dispose();
            new LoginWindow().setVisible(true);
        });
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(logoutButton, BorderLayout.SOUTH);
        
        add(panel);
    }
}
