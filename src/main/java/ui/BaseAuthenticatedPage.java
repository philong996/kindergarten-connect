package ui;

import service.AuthService;
import service.AuthorizationService;
import util.AuthUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Abstract base class for all authenticated pages implementing Template Method pattern
 * for consistent authorization handling across the application.
 */
public abstract class BaseAuthenticatedPage extends JFrame {
    protected AuthService authService;
    protected AuthorizationService authorizationService;
    
    /**
     * Constructor implementing Template Method pattern
     * @param authService The authentication service
     */
    public BaseAuthenticatedPage(AuthService authService) {
        if (authService == null || !authService.isLoggedIn()) {
            throw new IllegalStateException("Valid authenticated AuthService required");
        }
        
        this.authService = authService;
        this.authorizationService = authService.getAuthorizationService();
        
        // Template method - defines the algorithm structure
        initializePage();
    }
    
    /**
     * Template method defining the initialization algorithm
     * Final to prevent overriding - this is the template structure
     */
    private final void initializePage() {
        // Step 1: Initialize basic window properties
        initializeWindowProperties();
        
        // Step 2: Create and set menu bar
        setJMenuBar(createStandardMenuBar());
        
        // Step 3: Create UI components (hook method)
        initializeComponents();
        
        // Step 4: Setup layout (hook method)
        setupLayout();
        
        // Step 5: Setup authorization-based permissions (hook method)
        setupPermissions();
        
        // Step 6: Setup event handlers (hook method)
        setupEventHandlers();
        
        // Step 7: Setup common window behavior
        setupCommonWindowBehavior();
        
        // Step 8: Load initial data if needed (hook method)
        loadInitialData();
    }
    
    /**
     * Initialize basic window properties - common to all pages
     */
    private void initializeWindowProperties() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set window title using hook method
        setTitle(getPageTitle());
        
        // Set window size using hook method
        setSize(getWindowSize());
    }
    
    /**
     * Setup common window behavior including logout confirmation
     */
    private void setupCommonWindowBehavior() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    /**
     * Handle window closing with logout confirmation
     */
    protected void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }
    
    /**
     * Perform logout and return to login window
     */
    protected final void performLogout() {
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
    
    /**
     * Get display name for current user's role
     */
    protected final String getCurrentUserRoleDisplay() {
        return AuthUtil.getRoleDisplayName(authService.getCurrentUser().getRole());
    }
    
    /**
     * Check if current user has specific permission with error message
     */
    protected final boolean checkPermissionWithMessage(String permission, String action) {
        return AuthUtil.checkPermissionWithMessage(authService, permission, action);
    }
    
    /**
     * Create standardized menu bar for all pages
     */
    protected JMenuBar createStandardMenuBar() {
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
        
        // Allow subclasses to add custom menus
        customizeMenuBar(menuBar);
        
        return menuBar;
    }
    
    /**
     * Show standard about dialog
     */
    protected void showAbout() {
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
    
    // ===============================
    // HOOK METHODS - TO BE IMPLEMENTED BY SUBCLASSES
    // ===============================
    
    /**
     * Hook method: Get the page title
     * @return The title for this page
     */
    protected abstract String getPageTitle();
    
    /**
     * Hook method: Get the window size
     * Default implementation provides a standard size that can be overridden
     * @return The preferred size for this window
     */
    protected java.awt.Dimension getWindowSize() {
        return new java.awt.Dimension(1200, 800);
    }
    
    /**
     * Hook method: Initialize UI components
     * Subclasses should create their UI components here
     */
    protected abstract void initializeComponents();
    
    /**
     * Hook method: Setup the layout
     * Subclasses should arrange their UI components here
     */
    protected abstract void setupLayout();
    
    /**
     * Hook method: Setup permissions based on user role
     * Subclasses should configure UI based on authorization here
     */
    protected abstract void setupPermissions();
    
    /**
     * Hook method: Setup event handlers
     * Subclasses should wire up their event listeners here
     */
    protected abstract void setupEventHandlers();
    
    /**
     * Hook method: Load initial data
     * Subclasses can override to load data after UI setup
     * Default implementation does nothing
     */
    protected void loadInitialData() {
        // Default: no data loading
    }
    
    /**
     * Hook method: Customize menu bar
     * Subclasses can override to add role-specific menu items
     * Default implementation does nothing
     */
    protected void customizeMenuBar(JMenuBar menuBar) {
        // Default: no customization
    }
    
    /**
     * Hook method: Validate user role for this page
     * Subclasses can override to restrict access to specific roles
     * Default implementation allows all authenticated users
     * @return true if user can access this page
     */
    protected boolean validateUserRole() {
        return authService.isLoggedIn();
    }
    
    /**
     * Factory method for creating role-specific pages
     */
    public static JFrame createPageForRole(AuthService authService) {
        if (!authService.isLoggedIn()) {
            throw new IllegalStateException("User must be logged in");
        }
        
        String role = authService.getCurrentUser().getRole();
        switch (role) {
            case "PRINCIPAL":
                return new PrincipalPage(authService);
            case "TEACHER":
                return new TeacherPage(authService);
            case "PARENT":
                return new ParentPage(authService);
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
    }
}
