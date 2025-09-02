package ui.panels;

import model.Class;
import service.AuthService;
import service.ClassService;
import ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Class Management Panel - Manages kindergarten classes with teacher assignment and enrollment tracking
 */
public class ClassManagementPanel extends JPanel {
    private ClassService classService;
    private AuthService authService;
    
    // UI Components using the component library
    private DataTable classTable;
    private SearchPanel searchPanel;
    private FormBuilder formBuilder;
    private ButtonPanel buttonPanel;
    private JPanel statisticsPanel;
    
    // Form field IDs
    private static final String FIELD_NAME = "name";
    private static final String FIELD_GRADE_LEVEL = "gradeLevel";
    private static final String FIELD_CAPACITY = "capacity";
    private static final String FIELD_TEACHER = "teacher";
    
    private Class selectedClass;
    
    public ClassManagementPanel(AuthService authService) {
        this.authService = authService;
        this.classService = new ClassService();
        
        initializeComponents();
        setupLayout();
        setupPermissions();
        loadClassData();
    }
    
    /**
     * Public method to refresh all data - useful when panel becomes visible
     * and data might have changed in other parts of the application
     */
    public void refreshData() {
        loadClassData();
        loadAvailableTeachers();
    }
    
    private void initializeComponents() {
        // Create search panel
        searchPanel = SearchPanel.createWithClear("Search classes:", this::searchClasses, this::loadClassData);
        
        // Create data table
        String[] columnNames = {"ID", "Class Name", "Grade Level", "Teacher", "Enrollment", "Capacity", "Available Spots", "Utilization %"};
        classTable = new DataTable(columnNames);
        classTable.setRowSelectionHandler(this::onRowSelected);
        
        // Create form builder
        formBuilder = new FormBuilder("Class Information", 2);
        formBuilder.addTextField(FIELD_NAME, "Class Name", true)
                  .addComboBox(FIELD_GRADE_LEVEL, "Grade Level", ClassService.GRADE_LEVELS, true)
                  .addNumberField(FIELD_CAPACITY, "Capacity", true)
                  .addComboBox(FIELD_TEACHER, "Teacher", new String[]{"None"}, false);
        
        // Set default values
        formBuilder.setValue(FIELD_CAPACITY, String.valueOf(ClassService.DEFAULT_CAPACITY));
        
        // Create button panel
        buttonPanel = ButtonPanel.createCrudPanel(
            e -> addClass(),
            e -> updateClass(),
            e -> deleteClass(),
            e -> clearForm()
        );
        
        // Add custom buttons for teacher management
        buttonPanel.addButton("Assign Teacher", e -> assignTeacher());
        buttonPanel.addButton("Remove Teacher", e -> removeTeacher());
        buttonPanel.addButton("View Students", e -> viewStudents());
        buttonPanel.addButton("Refresh", e -> refreshData());
        
        // Create statistics panel
        statisticsPanel = createStatisticsPanel();
        
        // Load available teachers
        loadAvailableTeachers();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top: Search panel and statistics
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(statisticsPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Table
        add(classTable, BorderLayout.CENTER);
        
        // Bottom: Form and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formBuilder.build(), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupPermissions() {
        // Only principals can manage classes
        String userRole = authService.getCurrentUser().getRole();
        boolean canManageClasses = "PRINCIPAL".equals(userRole);
        
        // Enable/disable form and buttons based on permissions
        formBuilder.setEnabled(canManageClasses);
        buttonPanel.setButtonEnabled("Add", canManageClasses);
        buttonPanel.setButtonEnabled("Update", canManageClasses && selectedClass != null);
        buttonPanel.setButtonEnabled("Delete", canManageClasses && selectedClass != null);
        buttonPanel.setButtonEnabled("Assign Teacher", canManageClasses && selectedClass != null);
        buttonPanel.setButtonEnabled("Remove Teacher", canManageClasses && selectedClass != null);
        
        if (!canManageClasses) {
            // Show tooltip explaining restrictions
            String tooltip = "Only principals can manage classes";
            for (String fieldId : new String[]{FIELD_NAME, FIELD_GRADE_LEVEL, FIELD_CAPACITY, FIELD_TEACHER}) {
                FormField field = formBuilder.getField(fieldId);
                if (field != null) {
                    field.setFieldToolTip(tooltip);
                }
            }
        }
        
        // Set panel border with role information
        setBorder(BorderFactory.createTitledBorder("Class Management - " + userRole));
    }
    
    private void onRowSelected(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            loadSelectedClass(modelRowIndex);
            boolean canManage = "PRINCIPAL".equals(authService.getCurrentUser().getRole());
            buttonPanel.setButtonEnabled("Update", canManage);
            buttonPanel.setButtonEnabled("Delete", canManage);
            buttonPanel.setButtonEnabled("Assign Teacher", canManage);
            buttonPanel.setButtonEnabled("Remove Teacher", canManage);
            buttonPanel.setButtonEnabled("View Students", true); // Anyone can view
        } else {
            clearForm();
            buttonPanel.setButtonEnabled("Update", false);
            buttonPanel.setButtonEnabled("Delete", false);
            buttonPanel.setButtonEnabled("Assign Teacher", false);
            buttonPanel.setButtonEnabled("Remove Teacher", false);
            buttonPanel.setButtonEnabled("View Students", false);
        }
    }
    
    private void searchClasses(String searchText) {
        if (searchText.isEmpty()) {
            classTable.clearFilter();
            return;
        }
        
        // Filter classes by name using table filter
        classTable.applyFilter(searchText, 1); // Filter by class name column (index 1)
    }
    
    private void loadClassData() {
        try {
            List<Class> classes = classService.getAllClasses();
            updateTable(classes);
            updateStatistics();
            searchPanel.clearSearchText();
            // Refresh teacher list as class assignments may have changed
            loadAvailableTeachers();
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading class data: " + e.getMessage());
        }
    }
    
    private void updateTable(List<Class> classes) {
        classTable.clearRows();
        
        for (Class clazz : classes) {
            Object[] rowData = {
                clazz.getId(),
                clazz.getName(),
                clazz.getGradeLevel(),
                clazz.getTeacherName() != null ? clazz.getTeacherName() : "Unassigned",
                clazz.getCurrentEnrollment(),
                clazz.getCapacity(),
                clazz.getAvailableSpots(),
                String.format("%.1f%%", clazz.getCapacityUtilization())
            };
            classTable.addRow(rowData);
        }
    }
    
    private void loadSelectedClass(int row) {
        int classId = (Integer) classTable.getValueAt(row, 0);
        selectedClass = classService.getClassById(classId);
        
        if (selectedClass != null) {
            formBuilder.setValue(FIELD_NAME, selectedClass.getName());
            formBuilder.setValue(FIELD_GRADE_LEVEL, selectedClass.getGradeLevel());
            formBuilder.setValue(FIELD_CAPACITY, String.valueOf(selectedClass.getCapacity()));
            
            // Update teacher combo box selection
            if (selectedClass.getTeacherName() != null) {
                // Find the teacher in the combo box (with or without "(Assigned)" suffix)
                FormField teacherField = formBuilder.getField(FIELD_TEACHER);
                if (teacherField != null && teacherField.getInputComponent() instanceof JComboBox) {
                    @SuppressWarnings("unchecked")
                    JComboBox<String> comboBox = (JComboBox<String>) teacherField.getInputComponent();
                    
                    // Look for the teacher name with or without "(Assigned)" suffix
                    String teacherName = selectedClass.getTeacherName();
                    boolean found = false;
                    
                    for (int i = 0; i < comboBox.getItemCount(); i++) {
                        String item = comboBox.getItemAt(i);
                        if (item.equals(teacherName) || item.equals(teacherName + " (Assigned)")) {
                            comboBox.setSelectedIndex(i);
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        // Fallback to setting the value directly
                        formBuilder.setValue(FIELD_TEACHER, teacherName);
                    }
                }
            } else {
                formBuilder.setValue(FIELD_TEACHER, "None");
            }
        }
    }
    
    private void addClass() {
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        try {
            Class clazz = createClassFromForm();
            clazz.setSchoolId(authService.getCurrentUser().getSchoolId()); // Set school ID from current user
            
            boolean success = classService.addClass(clazz);
            if (success) {
                DialogFactory.showSuccess(this, "Class added successfully!");
                loadClassData();
                loadAvailableTeachers(); // Refresh teacher list
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to add class.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error adding class: " + e.getMessage());
        }
    }
    
    private void updateClass() {
        if (selectedClass == null) {
            DialogFactory.showWarning(this, "Please select a class to update.");
            return;
        }
        
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        if (!DialogFactory.showConfirmation(this, "Are you sure you want to update this class?")) {
            return;
        }
        
        try {
            Class updatedClass = createClassFromForm();
            updatedClass.setId(selectedClass.getId());
            updatedClass.setSchoolId(selectedClass.getSchoolId());
            
            boolean success = classService.updateClass(updatedClass);
            if (success) {
                DialogFactory.showSuccess(this, "Class updated successfully!");
                loadClassData();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to update class.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error updating class: " + e.getMessage());
        }
    }
    
    private void deleteClass() {
        if (selectedClass == null) {
            DialogFactory.showWarning(this, "Please select a class to delete.");
            return;
        }
        
        if (!DialogFactory.showDeleteConfirmation(this, selectedClass.getName())) {
            return;
        }
        
        try {
            boolean success = classService.deleteClass(selectedClass.getId());
            if (success) {
                DialogFactory.showSuccess(this, "Class deleted successfully!");
                loadClassData();
                loadAvailableTeachers(); // Refresh teacher list
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to delete class.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error deleting class: " + e.getMessage());
        }
    }
    
    private void assignTeacher() {
        if (selectedClass == null) {
            DialogFactory.showWarning(this, "Please select a class first.");
            return;
        }
        
        List<Object[]> availableTeachers = classService.getAvailableTeachers();
        if (availableTeachers.isEmpty()) {
            DialogFactory.showWarning(this, "No available teachers to assign.");
            return;
        }
        
        // Create teacher selection dialog
        String[] teacherNames = availableTeachers.stream()
                .map(teacher -> (String) teacher[1])
                .toArray(String[]::new);
        
        String selectedTeacher = (String) JOptionPane.showInputDialog(
                this,
                "Select a teacher to assign:",
                "Assign Teacher",
                JOptionPane.QUESTION_MESSAGE,
                null,
                teacherNames,
                teacherNames[0]
        );
        
        if (selectedTeacher != null) {
            // Find teacher ID
            Integer teacherId = availableTeachers.stream()
                    .filter(teacher -> selectedTeacher.equals(teacher[1]))
                    .map(teacher -> (Integer) teacher[0])
                    .findFirst()
                    .orElse(null);
            
            if (teacherId != null) {
                try {
                    boolean success = classService.assignTeacher(selectedClass.getId(), teacherId);
                    if (success) {
                        DialogFactory.showSuccess(this, "Teacher assigned successfully!");
                        loadClassData();
                        loadAvailableTeachers();
                        clearForm();
                    } else {
                        DialogFactory.showError(this, "Failed to assign teacher.");
                    }
                } catch (Exception e) {
                    DialogFactory.showError(this, "Error assigning teacher: " + e.getMessage());
                }
            }
        }
    }
    
    private void removeTeacher() {
        if (selectedClass == null) {
            DialogFactory.showWarning(this, "Please select a class first.");
            return;
        }
        
        if (selectedClass.getTeacherId() == null) {
            DialogFactory.showWarning(this, "This class doesn't have an assigned teacher.");
            return;
        }
        
        if (!DialogFactory.showConfirmation(this, 
                "Are you sure you want to remove " + selectedClass.getTeacherName() + " from this class?")) {
            return;
        }
        
        try {
            boolean success = classService.removeTeacher(selectedClass.getId());
            if (success) {
                DialogFactory.showSuccess(this, "Teacher removed successfully!");
                loadClassData();
                loadAvailableTeachers();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to remove teacher.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error removing teacher: " + e.getMessage());
        }
    }
    
    private void viewStudents() {
        if (selectedClass == null) {
            DialogFactory.showWarning(this, "Please select a class first.");
            return;
        }
        
        // Create and show student list dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Students in " + selectedClass.getName(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        // TODO: Create a StudentListPanel to show students in this class
        // For now, show a simple message
        JLabel messageLabel = new JLabel("<html><center>Student list view will be implemented<br>" +
                                        "Current enrollment: " + selectedClass.getCurrentEnrollment() + " students<br>" +
                                        "Available spots: " + selectedClass.getAvailableSpots() + "</center></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void clearForm() {
        formBuilder.clearAll();
        formBuilder.setValue(FIELD_CAPACITY, String.valueOf(ClassService.DEFAULT_CAPACITY));
        formBuilder.setValue(FIELD_TEACHER, "None");
        selectedClass = null;
        classTable.clearSelection();
        buttonPanel.setButtonEnabled("Update", false);
        buttonPanel.setButtonEnabled("Delete", false);
        buttonPanel.setButtonEnabled("Assign Teacher", false);
        buttonPanel.setButtonEnabled("Remove Teacher", false);
        buttonPanel.setButtonEnabled("View Students", false);
    }
    
    private Class createClassFromForm() throws Exception {
        String name = formBuilder.getValue(FIELD_NAME).trim();
        String gradeLevel = formBuilder.getValue(FIELD_GRADE_LEVEL).trim();
        String capacityStr = formBuilder.getValue(FIELD_CAPACITY).trim();
        String teacherName = formBuilder.getValue(FIELD_TEACHER).trim();
        
        if (name.isEmpty() || gradeLevel.isEmpty() || capacityStr.isEmpty()) {
            throw new Exception("Name, grade level, and capacity are required.");
        }
        
        // Validate and parse capacity
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            throw new Exception("Capacity must be a number.");
        }
        
        Class clazz = new Class();
        clazz.setName(name);
        clazz.setGradeLevel(gradeLevel);
        clazz.setCapacity(capacity);
        
        // Handle teacher assignment
        if (!"None".equals(teacherName)) {
            // Remove "(Assigned)" suffix if present
            String cleanTeacherName = teacherName.split(" \\(")[0].trim();
            
            // Check if this teacher is already assigned to another class (if adding new class)
            if (selectedClass == null && teacherName.contains("(Assigned)")) {
                throw new Exception("Selected teacher is already assigned to another class. Please choose an available teacher.");
            }
            
            List<Object[]> allTeachers = classService.getAllTeachers();
            Integer teacherId = allTeachers.stream()
                    .filter(teacher -> cleanTeacherName.equals(teacher[1]))
                    .map(teacher -> (Integer) teacher[0])
                    .findFirst()
                    .orElse(null);
            clazz.setTeacherId(teacherId);
        }
        
        return clazz;
    }
    
    private void loadAvailableTeachers() {
        try {
            // Get all teachers for showing current assignments, but mark availability
            List<Object[]> allTeachers = classService.getAllTeachers();
            List<Object[]> availableTeachers = classService.getAvailableTeachers();
            
            // Create teacher options with availability status
            String[] teacherOptions = new String[allTeachers.size() + 1];
            teacherOptions[0] = "None";
            
            for (int i = 0; i < allTeachers.size(); i++) {
                String teacherName = (String) allTeachers.get(i)[1];
                Integer teacherId = (Integer) allTeachers.get(i)[0];
                
                // Check if teacher is available
                boolean isAvailable = availableTeachers.stream()
                    .anyMatch(teacher -> teacherId.equals(teacher[0]));
                
                if (isAvailable) {
                    teacherOptions[i + 1] = teacherName;
                } else {
                    teacherOptions[i + 1] = teacherName + " (Assigned)";
                }
            }
            
            // Update the teacher combo box by accessing the input component directly
            FormField teacherField = formBuilder.getField(FIELD_TEACHER);
            if (teacherField != null && teacherField.getInputComponent() instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) teacherField.getInputComponent();
                
                // Remember current selection
                String currentSelection = (String) comboBox.getSelectedItem();
                
                comboBox.removeAllItems();
                for (String option : teacherOptions) {
                    comboBox.addItem(option);
                }
                
                // Restore selection if it still exists
                if (currentSelection != null) {
                    for (int i = 0; i < comboBox.getItemCount(); i++) {
                        String item = comboBox.getItemAt(i);
                        if (item.startsWith(currentSelection.split(" \\(")[0])) {
                            comboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading teachers: " + e.getMessage());
        }
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Class Statistics"));
        
        // Will be updated in updateStatistics()
        panel.add(new JLabel("Loading statistics..."));
        
        return panel;
    }
    
    private void updateStatistics() {
        try {
            ClassService.ClassStatistics stats = classService.getClassStatistics();
            
            statisticsPanel.removeAll();
            statisticsPanel.add(new JLabel(String.format(
                "Total Classes: %d | With Teachers: %d | Total Capacity: %d | Total Enrollment: %d | Utilization: %.1f%% | Available Spots: %d",
                stats.getTotalClasses(),
                stats.getClassesWithTeachers(),
                stats.getTotalCapacity(),
                stats.getTotalEnrollment(),
                stats.getCapacityUtilization(),
                stats.getAvailableSpots()
            )));
            
            statisticsPanel.revalidate();
            statisticsPanel.repaint();
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
}
