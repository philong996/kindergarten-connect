package service;

import dao.UserDAO;
import model.User;

/**
 * Service class for authentication and user management
 */
public class AuthService {
    private UserDAO userDAO;
    private User currentUser;
    private AuthorizationService authorizationService;
    
    public AuthService() {
        this.userDAO = new UserDAO();
        // Initialize authorization service after this service is created
    }
    
    /**
     * Initialize authorization service (called after AuthService is created)
     */
    public void initializeAuthorization() {
        this.authorizationService = new AuthorizationService(this);
    }
    
    /**
     * Get authorization service
     */
    public AuthorizationService getAuthorizationService() {
        if (authorizationService == null) {
            initializeAuthorization();
        }
        return authorizationService;
    }
    
    /**
     * Authenticate user login
     */
    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }
    
    /**
     * Check if current user is principal
     */
    public boolean isPrincipal() {
        return hasRole("PRINCIPAL");
    }
    
    /**
     * Check if current user is teacher
     */
    public boolean isTeacher() {
        return hasRole("TEACHER");
    }
    
    /**
     * Check if current user is parent
     */
    public boolean isParent() {
        return hasRole("PARENT");
    }
}
