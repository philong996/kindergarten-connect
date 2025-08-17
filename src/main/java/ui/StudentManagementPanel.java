package ui;

import model.Student;
import service.AuthService;
import service.AuthorizationService;
import service.StudentService;
import ui.components.*;
import util.AuthUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Student Management Panel - Refactored to use reusable UI components
 */
public class StudentManagementPanel extends JPanel {
    private StudentService studentService;
    private AuthService authService;
    private AuthorizationService authorizationService;
    
    // UI Components using the  component library
    private DataTable studentTable;
    private SearchPanel searchPanel;
    private FormBuilder formBuilder;
    private ButtonPanel buttonPanel;
    
    // Form field IDs
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DOB = "dob";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_CLASS_ID = "classId";
    
    private Student selectedStudent;
    
    public StudentManagementPanel(AuthService authService) {
        this.authService = authService;
        this.authorizationService = authService.getAuthorizationService();
        this.studentService = new StudentService();
        
        initializeComponents();
        setupLayout();
        setupPermissions();
        loadStudentData();
    }
    
    private void initializeComponents() {
        // Create search panel
        searchPanel = SearchPanel.createWithClear("Search by name:", this::searchStudents, this::loadStudentData);
        
        // Create data table
        String[] columnNames = {"ID", "Name", "Date of Birth", "Age", "Class", "Address"};
        studentTable = new DataTable(columnNames);
        studentTable.setRowSelectionHandler(this::onRowSelected);
        
        // Create form builder
        formBuilder = new FormBuilder("Student Information", 2);
        formBuilder.addTextField(FIELD_NAME, "Name", true)
                  .addDateField(FIELD_DOB, "Date of Birth", true)
                  .addNumberField(FIELD_CLASS_ID, "Class ID", true)
                  .addTextField(FIELD_ADDRESS, "Address", false);
        
        // Create button panel
        buttonPanel = ButtonPanel.createCrudPanel(
            e -> addStudent(),
            e -> updateStudent(), 
            e -> deleteStudent(),
            e -> clearForm()
        );
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top: Search panel
        add(searchPanel, BorderLayout.NORTH);
        
        // Center: Table
        add(studentTable, BorderLayout.CENTER);
        
        // Bottom: Form and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formBuilder.build(), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupPermissions() {
        // Check if user can manage students
        boolean canManageStudents = AuthUtil.canManageStudents(authService);
        
        // Enable/disable form and buttons based on permissions
        formBuilder.setEnabled(canManageStudents);
        buttonPanel.setButtonEnabled("Add", canManageStudents);
        
        if (!canManageStudents) {
            // Show tooltip explaining restrictions
            String tooltip = "You don't have permission to edit student information";
            for (String fieldId : new String[]{FIELD_NAME, FIELD_DOB, FIELD_ADDRESS, FIELD_CLASS_ID}) {
                FormField field = formBuilder.getField(fieldId);
                if (field != null) {
                    field.setFieldToolTip(tooltip);
                }
            }
        }
        
        // Set panel border with role information
        String userRole = AuthUtil.getRoleDisplayName(authService.getCurrentUser().getRole());
        setBorder(BorderFactory.createTitledBorder("Student Management - " + userRole));
    }
    
    private void onRowSelected(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            loadSelectedStudent(modelRowIndex);
            buttonPanel.setButtonEnabled("Update", AuthUtil.canManageStudents(authService));
            buttonPanel.setButtonEnabled("Delete", AuthUtil.canManageStudents(authService));
        } else {
            clearForm();
            buttonPanel.setButtonEnabled("Update", false);
            buttonPanel.setButtonEnabled("Delete", false);
        }
    }
    
    private void searchStudents(String searchText) {
        if (searchText.isEmpty()) {
            studentTable.clearFilter();
            return;
        }
        
        // Filter students by name using table filter
        studentTable.applyFilter(searchText, 1); // Filter by name column (index 1)
    }
    
    private void loadStudentData() {
        try {
            List<Student> students = studentService.getAllStudents();
            updateTable(students);
            searchPanel.clearSearchText();
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading student data: " + e.getMessage());
        }
    }
    
    private void updateTable(List<Student> students) {
        studentTable.clearRows();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Student student : students) {
            Object[] rowData = {
                student.getId(),
                student.getName(),
                student.getDob() != null ? student.getDob().format(formatter) : "",
                student.getAge(),
                student.getClassName() != null ? student.getClassName() : "Class " + student.getClassId(),
                student.getAddress()
            };
            studentTable.addRow(rowData);
        }
    }
    
    private void loadSelectedStudent(int row) {
        int studentId = (Integer) studentTable.getValueAt(row, 0);
        selectedStudent = studentService.getStudentById(studentId);
        
        if (selectedStudent != null) {
            formBuilder.setValue(FIELD_NAME, selectedStudent.getName());
            formBuilder.setValue(FIELD_DOB, selectedStudent.getDob().toString());
            formBuilder.setValue(FIELD_CLASS_ID, String.valueOf(selectedStudent.getClassId()));
            formBuilder.setValue(FIELD_ADDRESS, selectedStudent.getAddress());
        }
    }
    
    private void addStudent() {
        if (!AuthUtil.checkPermissionWithMessage(authService, AuthorizationService.PERM_CREATE_STUDENTS, "add students")) {
            return;
        }
        
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        try {
            Student student = createStudentFromForm();
            
            // Check if user can access the class
            if (!authorizationService.canAccessClass(student.getClassId())) {
                DialogFactory.showError(this, "You don't have permission to add students to this class.");
                return;
            }
            
            boolean success = studentService.addStudent(student);
            if (success) {
                DialogFactory.showSuccess(this, "Student added successfully!");
                loadStudentData();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to add student.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error adding student: " + e.getMessage());
        }
    }
    
    private void updateStudent() {
        if (!AuthUtil.checkPermissionWithMessage(authService, AuthorizationService.PERM_UPDATE_STUDENTS, "update students")) {
            return;
        }
        
        if (selectedStudent == null) {
            DialogFactory.showWarning(this, "Please select a student to update.");
            return;
        }
        
        if (!formBuilder.validateRequired()) {
            return;
        }
        
        if (!DialogFactory.showConfirmation(this, "Are you sure you want to update this student's information?")) {
            return;
        }
        
        try {
            Student updatedStudent = createStudentFromForm();
            updatedStudent.setId(selectedStudent.getId());
            
            boolean success = studentService.updateStudent(updatedStudent);
            if (success) {
                DialogFactory.showSuccess(this, "Student updated successfully!");
                loadStudentData();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to update student.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error updating student: " + e.getMessage());
        }
    }
    
    private void deleteStudent() {
        if (!AuthUtil.checkPermissionWithMessage(authService, AuthorizationService.PERM_UPDATE_STUDENTS, "delete students")) {
            return;
        }
        
        if (selectedStudent == null) {
            DialogFactory.showWarning(this, "Please select a student to delete.");
            return;
        }
        
        if (!DialogFactory.showDeleteConfirmation(this, selectedStudent.getName())) {
            return;
        }
        
        try {
            boolean success = studentService.deleteStudent(selectedStudent.getId());
            if (success) {
                DialogFactory.showSuccess(this, "Student deleted successfully!");
                loadStudentData();
                clearForm();
            } else {
                DialogFactory.showError(this, "Failed to delete student.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error deleting student: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        formBuilder.clearAll();
        selectedStudent = null;
        studentTable.clearSelection();
        buttonPanel.setButtonEnabled("Update", false);
        buttonPanel.setButtonEnabled("Delete", false);
    }
    
    private Student createStudentFromForm() throws Exception {
        String name = formBuilder.getValue(FIELD_NAME).trim();
        String dobStr = formBuilder.getValue(FIELD_DOB).trim();
        String address = formBuilder.getValue(FIELD_ADDRESS).trim();
        String classIdStr = formBuilder.getValue(FIELD_CLASS_ID).trim();
        
        // Validate and parse date
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format. Please use YYYY-MM-DD format.");
        }
        
        // Validate and parse class ID
        int classId;
        try {
            classId = Integer.parseInt(classIdStr);
        } catch (NumberFormatException e) {
            throw new Exception("Class ID must be a number.");
        }
        
        Student student = new Student();
        student.setName(name);
        student.setDob(dob);
        student.setAddress(address);
        student.setClassId(classId);
        
        return student;
    }
}
