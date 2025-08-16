package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login window for the application
 */
public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private AuthService authService;
    
    public LoginWindow() {
        this.authService = new AuthService();
        this.authService.initializeAuthorization(); // Initialize authorization service
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Student Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("Kindergarten Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 30, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(titleLabel, gbc);
        
        // Username label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0; // Prevent horizontal expansion for label
        gbc.fill = GridBagConstraints.NONE; // No fill for label
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 5, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0; // Allow horizontal expansion for field
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontal space for field
        mainPanel.add(usernameField, gbc);

        // Password label and field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(5, 20, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0; // Prevent horizontal expansion for label
        gbc.fill = GridBagConstraints.NONE; // No fill for label
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0; // Allow horizontal expansion for field
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontal space for field
        mainPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);
        
        // Add sample credentials info
        JLabel infoLabel = new JLabel("<html><center>Sample Credentials:<br/>admin/admin123 (Principal)<br/>teacher1/teacher123 (Teacher)<br/>parent1/parent123 (Parent)</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; // Span across two columns
        gbc.insets = new Insets(10, 20, 10, 20); // Adjust spacing
        gbc.anchor = GridBagConstraints.CENTER; // Center the info panel
        gbc.fill = GridBagConstraints.NONE; // Prevent stretching
        mainPanel.add(infoLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Allow Enter key to trigger login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password.", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Attempt login
        try {
            if (authService.login(username, password)) {
                // Login successful
                JOptionPane.showMessageDialog(this, 
                    "Login successful! Welcome, " + authService.getCurrentUser().getRole().toLowerCase() + ".", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Open main application window based on role
                openMainWindow();
                
                // Close login window
                dispose();
                
            } else {
                // Login failed
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password.", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Clear password field
                passwordField.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openMainWindow() {
        String role = authService.getCurrentUser().getRole();
        
        switch (role) {
            case "PRINCIPAL":
                new PrincipalPage(authService).setVisible(true);
                break;
            case "TEACHER":
                new TeacherPage(authService).setVisible(true);
                break;
            case "PARENT":
                new ParentPage(authService).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, 
                    "Unknown user role: " + role, 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginWindow().setVisible(true);
            }
        });
    }
}
