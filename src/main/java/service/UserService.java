package service;

import dao.UserDAO;
import dao.TeacherDAO;
import dao.ParentDAO;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user management operations with role validation
 */
public class UserService {
    private final UserDAO userDAO;
    private final TeacherDAO teacherDAO;
    private final ParentDAO parentDAO;
    
    // Valid user roles
    private static final String[] VALID_ROLES = {"PRINCIPAL", "TEACHER", "PARENT"};
    
    public UserService() {
        this.userDAO = new UserDAO();
        this.teacherDAO = new TeacherDAO();
        this.parentDAO = new ParentDAO();
    }
    
    /**
     * Create a new user with role validation
     */
    public boolean createUser(User user) throws SQLException {
        // Validate role
        if (!isValidRole(user.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        
        // Check if username already exists
        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Store password directly without hashing
        // Password validation is done in validateUser method
        
        // Create user in database
        boolean userCreated = userDAO.create(user);
        
        if (userCreated) {
            // Get the created user to get the ID
            User createdUser = userDAO.findByUsername(user.getUsername());
            if (createdUser != null) {
                // Create role-specific entries
                if ("TEACHER".equals(user.getRole())) {
                    teacherDAO.createTeacherProfile(createdUser.getId());
                } else if ("PARENT".equals(user.getRole())) {
                    parentDAO.createParentProfile(createdUser.getId());
                }
            }
        }
        
        return userCreated;
    }
    
    /**
     * Update an existing user with role validation
     * Note: Role changes are not allowed for security reasons
     */
    public boolean updateUser(User user) throws SQLException {
        // Check if user exists
        User existingUser = userDAO.findById(user.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }
        
        // Prevent role changes for security
        if (!existingUser.getRole().equals(user.getRole())) {
            throw new IllegalArgumentException("Role changes are not allowed. Current role: " + existingUser.getRole());
        }
        
        // Check if username is taken by another user
        User userWithSameUsername = userDAO.findByUsername(user.getUsername());
        if (userWithSameUsername != null && userWithSameUsername.getId() != user.getId()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // If password is provided, use it directly
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            // Password validation is done in validateUser method
        } else {
            // Keep existing password
            user.setPassword(existingUser.getPassword());
        }
        
        return userDAO.update(user);
    }
    
    /**
     * Delete a user and associated role-specific data
     */
    public boolean deleteUser(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        // Don't allow deletion of principal users
        if ("PRINCIPAL".equals(user.getRole())) {
            throw new IllegalArgumentException("Cannot delete principal users");
        }
        
        // Delete role-specific data first
        if ("TEACHER".equals(user.getRole())) {
            teacherDAO.deleteTeacherProfile(userId);
        } else if ("PARENT".equals(user.getRole())) {
            parentDAO.deleteParentProfile(userId);
        }
        
        // Delete the user
        return userDAO.delete(userId);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        return userDAO.findByRole(role);
    }
    
    /**
     * Search users by username or role
     */
    public List<User> searchUsers(String searchTerm) {
        List<User> allUsers = userDAO.findAll();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allUsers;
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        return allUsers.stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(lowerSearchTerm) ||
                    user.getRole().toLowerCase().contains(lowerSearchTerm)
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int id) {
        return userDAO.findById(id);
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
    
    /**
     * Validate if a role is valid
     */
    public boolean isValidRole(String role) {
        if (role == null) return false;
        
        for (String validRole : VALID_ROLES) {
            if (validRole.equals(role.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all valid roles
     */
    public String[] getValidRoles() {
        return VALID_ROLES.clone();
    }
    
    /**
     * Validate user data
     */
    public void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (user.getUsername().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        
        if (!isValidRole(user.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        
        if (user.getSchoolId() <= 0) {
            throw new IllegalArgumentException("Valid school ID is required");
        }
    }
}
