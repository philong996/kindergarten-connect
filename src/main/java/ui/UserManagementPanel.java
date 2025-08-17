package ui;

import service.UserService;
import service.AuthService;
import model.User;
import ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User Management Panel - Refactored to use reusable UI components
 */
public class UserManagementPanel extends JPanel {
    private UserService userService;
    
    // UI Components using the new component library
    private DataTable userTable;
    private SearchPanel searchPanel;
    private FormBuilder formBuilder;
    private ButtonPanel buttonPanel;
    
    // Form field IDs
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_SCHOOL_ID = "schoolId";
    
    private User selectedUser;

    public UserManagementPanel(AuthService authService) {
        this.userService = new UserService();
        
        initializeComponents();
        setupLayout();
        setupPermissions();
        loadUsers();
    }

    private void initializeComponents() {
        // Create search panel
        searchPanel = SearchPanel.createWithClear("Search users:", this::searchUsers, this::loadUsers);
        
        // Create data table
        String[] columnNames = {"ID", "Username", "Role", "School ID", "Created At"};
        userTable = new DataTable(columnNames);
        userTable.setRowSelectionHandler(this::onRowSelected);

        // Create form builder
        formBuilder = new FormBuilder("User Information", 2);
        formBuilder.addTextField(FIELD_USERNAME, "Username", true)
                  .addPasswordField(FIELD_PASSWORD, "Password", true)
                  .addComboBox(FIELD_ROLE, "Role", new String[]{"TEACHER", "PARENT"}, true)
                  .addNumberField(FIELD_SCHOOL_ID, "School ID", true);
        
        // Set default school ID
        formBuilder.setValue(FIELD_SCHOOL_ID, "1");

        // Create button panel
        buttonPanel = ButtonPanel.createCrudPanel(
            e -> addUser(),
            e -> updateUser(),
            e -> deleteUser(),
            e -> clearForm()
        );
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top: Search panel
        add(searchPanel, BorderLayout.NORTH);

        // Center: Table
        add(userTable, BorderLayout.CENTER);

        // Bottom: Form and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formBuilder.build(), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupPermissions() {
        // Only principals can manage users - this should be checked at a higher level
        setBorder(BorderFactory.createTitledBorder("User Management - Principal Only"));
    }

    private void onRowSelected(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            loadUserToForm(modelRowIndex);
            buttonPanel.setButtonEnabled("Update", true);
            buttonPanel.setButtonEnabled("Delete", true);
        } else {
            clearForm();
            buttonPanel.setButtonEnabled("Update", false);
            buttonPanel.setButtonEnabled("Delete", false);
        }
    }
    
    private void searchUsers(String searchText) {
        if (searchText.isEmpty()) {
            userTable.clearFilter();
            return;
        }
        
        // Filter users by username
        userTable.applyFilter(searchText, 1); // Filter by username column (index 1)
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            updateTable(users);
            searchPanel.clearSearchText();
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading users: " + e.getMessage());
        }
    }
    
    private void updateTable(List<User> users) {
        userTable.clearRows();
        
        for (User user : users) {
            Object[] rowData = {
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getSchoolId(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
            };
            userTable.addRow(rowData);
        }
    }

    private void loadUserToForm(int row) {
        int userId = (Integer) userTable.getValueAt(row, 0);
        selectedUser = userService.getUserById(userId);
        
        if (selectedUser != null) {
            formBuilder.setValue(FIELD_USERNAME, selectedUser.getUsername());
            formBuilder.setValue(FIELD_PASSWORD, ""); // Don't show existing password
            formBuilder.setValue(FIELD_ROLE, selectedUser.getRole());
            formBuilder.setValue(FIELD_SCHOOL_ID, String.valueOf(selectedUser.getSchoolId()));
        }
    }

    private void addUser() {
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        try {
            User user = createUserFromForm();
            
            boolean success = userService.createUser(user);
            if (success) {
                DialogFactory.showSuccess(this, "User added successfully!");
                loadUsers();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to add user. Username might already exist.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error adding user: " + e.getMessage());
        }
    }

    private void updateUser() {
        if (selectedUser == null) {
            DialogFactory.showWarning(this, "Please select a user to update.");
            return;
        }
        
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        if (!DialogFactory.showConfirmation(this, "Are you sure you want to update this user?")) {
            return;
        }
        
        try {
            User updatedUser = createUserFromForm();
            updatedUser.setId(selectedUser.getId());
            
            boolean success = userService.updateUser(updatedUser);
            if (success) {
                DialogFactory.showSuccess(this, "User updated successfully!");
                loadUsers();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to update user.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error updating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        if (selectedUser == null) {
            DialogFactory.showWarning(this, "Please select a user to delete.");
            return;
        }
        
        if (!DialogFactory.showDeleteConfirmation(this, selectedUser.getUsername())) {
            return;
        }
        
        try {
            boolean success = userService.deleteUser(selectedUser.getId());
            if (success) {
                DialogFactory.showSuccess(this, "User deleted successfully!");
                loadUsers();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to delete user.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error deleting user: " + e.getMessage());
        }
    }

    private void clearForm() {
        formBuilder.clearAll();
        formBuilder.setValue(FIELD_SCHOOL_ID, "1"); // Reset to default
        selectedUser = null;
        userTable.clearSelection();
        buttonPanel.setButtonEnabled("Update", false);
        buttonPanel.setButtonEnabled("Delete", false);
    }
    
    private User createUserFromForm() throws Exception {
        String username = formBuilder.getValue(FIELD_USERNAME).trim();
        String password = formBuilder.getValue(FIELD_PASSWORD).trim();
        String role = formBuilder.getValue(FIELD_ROLE).trim();
        String schoolIdStr = formBuilder.getValue(FIELD_SCHOOL_ID).trim();
        
        if (username.isEmpty() || password.isEmpty() || role.isEmpty() || schoolIdStr.isEmpty()) {
            throw new Exception("All fields are required.");
        }
        
        int schoolId;
        try {
            schoolId = Integer.parseInt(schoolIdStr);
        } catch (NumberFormatException e) {
            throw new Exception("School ID must be a number.");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In real app, this should be hashed
        user.setRole(role);
        user.setSchoolId(schoolId);
        
        return user;
    }
}
