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
        
        // Step 2: Create UI components (hook method)
        initializeComponents();
        
        // Step 3: Setup layout (hook method)
        setupLayout();
        
        // Step 4: Setup authorization-based permissions (hook method)
        setupPermissions();
        
        // Step 5: Setup event handlers (hook method)
        setupEventHandlers();
        
        // Step 6: Setup common window behavior
        setupCommonWindowBehavior();
        
        // Step 7: Load initial data if needed (hook method)
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
     * @return The preferred size for this window
     */
    protected abstract java.awt.Dimension getWindowSize();
    
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
