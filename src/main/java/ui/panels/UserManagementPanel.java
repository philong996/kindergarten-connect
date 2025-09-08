package ui.panels;

import service.UserService;
import service.AuthService;
import service.StudentService;
import model.User;
import model.Student;
import ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User Management Panel - Refactored to use reusable UI components
 */
public class UserManagementPanel extends JPanel {
    private UserService userService;
    private StudentService studentService;
    
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
    private JPanel studentFieldPanel; // Panel to show/hide student selection for parents
    private JComboBox<String> studentComboBox; // Dropdown for selecting students
    private JLabel selectedStudentLabel;
    private Student selectedStudent; // Currently selected student for linking
    private List<Student> availableStudents; // List of students that can be assigned

    public UserManagementPanel(AuthService authService) {
        this.userService = new UserService();
        this.studentService = new StudentService();
        
        initializeComponents();
        setupLayout();
        setupPermissions();
        loadUsers();
    }
    
    /**
     * Public method to refresh all data - useful when panel becomes visible
     */
    public void refreshData() {
        loadUsers();
        loadAvailableSchools();
        loadAvailableStudents();
    }

    private void initializeComponents() {
        // Make this panel transparent
        setOpaque(false);
        searchPanel = SearchPanel.createWithClear("Search users:", this::searchUsers, this::loadUsers);
        searchPanel.setOpaque(false);
        
        // Create data table
        String[] columnNames = {"ID", "Username", "Role", "School", "Child Name", "Created At"};
        userTable = new DataTable(columnNames);
        userTable.setRowSelectionHandler(this::onRowSelected);
        userTable.setOpaque(false);

        // Create form builder
        formBuilder = new FormBuilder("User Information", 2);
        formBuilder.addTextField(FIELD_USERNAME, "Username", true)
                  .addPasswordField(FIELD_PASSWORD, "Password", true)
                  .addComboBox(FIELD_ROLE, "Role", new String[]{"TEACHER", "PARENT"}, true)
                  .addComboBox(FIELD_SCHOOL_ID, "School", new String[]{"Loading..."}, true);
        
        // Add role change listener to show/hide student search
        FormField roleField = formBuilder.getField(FIELD_ROLE);
        if (roleField != null && roleField.getInputComponent() instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> roleCombo = (JComboBox<String>) roleField.getInputComponent();
            roleCombo.addActionListener(e -> updateStudentFieldVisibility());
        }
        
        // Create student search panel (initially hidden)
        createStudentSearchPanel();
        
        // Load available schools
        loadAvailableSchools();

        // Create button panel
        buttonPanel = ButtonPanel.createCrudPanel(
            e -> addUser(),
            e -> updateUser(),
            e -> deleteUser(),
            e -> clearForm()
        );
    }
    
    private void createStudentSearchPanel() {
        studentFieldPanel = new JPanel(new GridBagLayout());
        studentFieldPanel.setBorder(BorderFactory.createTitledBorder("Student Assignment (for Parents)"));
        studentFieldPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Student selection label
        JLabel selectLabel = new JLabel("Select Student:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        studentFieldPanel.add(selectLabel, gbc);
        
        // Student dropdown
        studentComboBox = new JComboBox<>();
        studentComboBox.addItem("-- Select a student --");
        studentComboBox.addActionListener(e -> onStudentSelected());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        studentFieldPanel.add(studentComboBox, gbc);
        
        // Selected student info label
        selectedStudentLabel = new JLabel("No student selected");
        selectedStudentLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        studentFieldPanel.add(selectedStudentLabel, gbc);
        
        // Initially hide the panel
        studentFieldPanel.setVisible(false);
        
        // Load available students
        loadAvailableStudents();
    }
    
    private void onStudentSelected() {
        int selectedIndex = studentComboBox.getSelectedIndex();
        if (selectedIndex <= 0 || availableStudents == null || selectedIndex > availableStudents.size()) {
            // No selection or invalid selection
            selectedStudent = null;
            selectedStudentLabel.setText("No student selected");
            selectedStudentLabel.setForeground(Color.GRAY);
        } else {
            // Valid selection (index - 1 because first item is "-- Select a student --")
            selectedStudent = availableStudents.get(selectedIndex - 1);
            selectedStudentLabel.setText("Selected: " + selectedStudent.getName() + 
                " (ID: " + selectedStudent.getId() + ", Class: " + selectedStudent.getClassName() + ")");
            selectedStudentLabel.setForeground(Color.BLUE);
        }
    }
    
    private void loadAvailableStudents() {
        try {
            // Get all students who don't have parent assignments yet
            availableStudents = studentService.getStudentsWithoutParents();
            
            // Clear and populate combo box
            studentComboBox.removeAllItems();
            studentComboBox.addItem("-- Select a student --");
            
            if (availableStudents != null) {
                for (Student student : availableStudents) {
                    String displayText = student.getName() + " (ID: " + student.getId();
                    if (student.getClassName() != null) {
                        displayText += ", Class: " + student.getClassName();
                    }
                    displayText += ")";
                    studentComboBox.addItem(displayText);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading available students: " + e.getMessage());
            // Add a default message
            studentComboBox.removeAllItems();
            studentComboBox.addItem("Error loading students");
        }
    }
    
    private void updateStudentFieldVisibility() {
        FormField roleField = formBuilder.getField(FIELD_ROLE);
        if (roleField != null && roleField.getInputComponent() instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> roleCombo = (JComboBox<String>) roleField.getInputComponent();
            String selectedRole = (String) roleCombo.getSelectedItem();
            boolean showStudentField = "PARENT".equals(selectedRole);
            
            studentFieldPanel.setVisible(showStudentField);
            if (!showStudentField) {
                // Clear student selection when not showing
                selectedStudent = null;
                selectedStudentLabel.setText("No student selected");
                if (studentComboBox != null) {
                    studentComboBox.setSelectedIndex(0);
                }
            }
            
            // Refresh layout
            revalidate();
            repaint();
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top: Search panel
        add(searchPanel, BorderLayout.NORTH);

        // Center: Table
        add(userTable, BorderLayout.CENTER);

        // Bottom: Form and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Create a panel that contains both the main form and student search
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.add(formBuilder.build(), BorderLayout.NORTH);
        formPanel.add(studentFieldPanel, BorderLayout.CENTER);
        formPanel.setOpaque(false);
        
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setOpaque(false);
        
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
            List<User> users = userService.getAllUsersWithSchoolNames();
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
                user.getSchoolName() != null ? user.getSchoolName() : "Unknown School",
                user.getChildName() != null ? user.getChildName() : (user.isParent() ? "No Child Assigned" : "N/A"),
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
            
            // Set school by finding the matching school name
            try {
                List<Object[]> schools = userService.getAllSchools();
                String targetSchoolName = null;
                
                // Find the school name for the user's school ID
                for (Object[] school : schools) {
                    if (((Integer) school[0]).equals(selectedUser.getSchoolId())) {
                        targetSchoolName = (String) school[1];
                        break;
                    }
                }
                
                if (targetSchoolName != null) {
                    formBuilder.setValue(FIELD_SCHOOL_ID, targetSchoolName);
                }
            } catch (Exception e) {
                System.err.println("Error setting school in form: " + e.getMessage());
            }
        }
    }

    private void addUser() {
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        try {
            User user = createUserFromForm();
            
            boolean success;
            if ("PARENT".equals(user.getRole())) {
                // Check if a student is selected
                if (selectedStudent == null) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Warning", "Please search and select a student for the parent.", CustomMessageDialog.Type.INFO);
                    // DialogFactory.showError(this, "Please search and select a student for the parent.");
                    return;
                }
                
                // Create parent user with student link
                success = userService.createParentUserWithStudent(user, selectedStudent.getId());
                if (success) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Success", "Parent user created and linked to student: " + selectedStudent.getName(), CustomMessageDialog.Type.SUCCESS);
                    // DialogFactory.showSuccess(this, "Parent user created and linked to student: " + selectedStudent.getName());
                } else {
                    DialogFactory.showError(this, "Failed to create parent user or link to student.");
                }
            } else {
                // Create regular user (teacher)
                success = userService.createUser(user);
                if (success) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Success", "User added successfully!", CustomMessageDialog.Type.SUCCESS);
                    // DialogFactory.showSuccess(this, "User added successfully!");
                } else {
                    DialogFactory.showError(this, "Failed to add user. Username might already exist.");
                }
            }
            
            if (success) {
                loadUsers();
                clearForm();
            }
        } catch (Exception e) {
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Error", "Error adding user: " + e.getMessage(), CustomMessageDialog.Type.ERROR);
            // DialogFactory.showError(this, "Error adding user: " + e.getMessage());
        }
    }

    private void updateUser() {
        if (selectedUser == null) {
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Warning", "Please select a user to update.", CustomMessageDialog.Type.INFO);
            // DialogFactory.showWarning(this, "Please select a user to update.");
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
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Success", "User updated successfully!", CustomMessageDialog.Type.SUCCESS);
                // DialogFactory.showSuccess(this, "User updated successfully!");
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
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Warning", "Please select a user to delete.", CustomMessageDialog.Type.INFO);
            // DialogFactory.showWarning(this, "Please select a user to delete.");
            return;
        }
        
        if (!DialogFactory.showDeleteConfirmation(this, selectedUser.getUsername())) {
            return;
        }
        
        try {
            boolean success = userService.deleteUser(selectedUser.getId());
            if (success) {
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "Success", "User deleted successfully!", CustomMessageDialog.Type.SUCCESS);
                // DialogFactory.showSuccess(this, "User deleted successfully!");
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
        // Reset to first school in the list
        FormField schoolField = formBuilder.getField(FIELD_SCHOOL_ID);
        if (schoolField != null && schoolField.getInputComponent() instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) schoolField.getInputComponent();
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
        }
        
        // Clear student selection
        selectedStudent = null;
        if (studentComboBox != null) {
            studentComboBox.setSelectedIndex(0);
        }
        if (selectedStudentLabel != null) {
            selectedStudentLabel.setText("No student selected");
            selectedStudentLabel.setForeground(Color.GRAY);
        }
        
        // Hide student field panel
        updateStudentFieldVisibility();
        
        selectedUser = null;
        userTable.clearSelection();
        buttonPanel.setButtonEnabled("Update", false);
        buttonPanel.setButtonEnabled("Delete", false);
    }
    
    private void loadAvailableSchools() {
        try {
            List<Object[]> schools = userService.getAllSchools();
            
            // Create school options
            String[] schoolOptions = new String[schools.size()];
            for (int i = 0; i < schools.size(); i++) {
                schoolOptions[i] = (String) schools.get(i)[1]; // School name
            }
            
            // Update the school combo box
            FormField schoolField = formBuilder.getField(FIELD_SCHOOL_ID);
            if (schoolField != null && schoolField.getInputComponent() instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) schoolField.getInputComponent();
                comboBox.removeAllItems();
                for (String option : schoolOptions) {
                    comboBox.addItem(option);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading schools: " + e.getMessage());
        }
    }
    
    private User createUserFromForm() throws Exception {
        String username = formBuilder.getValue(FIELD_USERNAME).trim();
        String password = formBuilder.getValue(FIELD_PASSWORD).trim();
        String role = formBuilder.getValue(FIELD_ROLE).trim();
        String schoolName = formBuilder.getValue(FIELD_SCHOOL_ID).trim();
        
        if (username.isEmpty() || password.isEmpty() || role.isEmpty() || schoolName.isEmpty()) {
            throw new Exception("All fields are required.");
        }
        
        // Find school ID by name
        int schoolId = -1;
        try {
            List<Object[]> schools = userService.getAllSchools();
            for (Object[] school : schools) {
                if (schoolName.equals(school[1])) {
                    schoolId = (Integer) school[0];
                    break;
                }
            }
            
            if (schoolId == -1) {
                throw new Exception("Selected school not found.");
            }
        } catch (Exception e) {
            throw new Exception("Error finding school: " + e.getMessage());
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In real app, this should be hashed
        user.setRole(role);
        user.setSchoolId(schoolId);
        
        return user;
    }
}
