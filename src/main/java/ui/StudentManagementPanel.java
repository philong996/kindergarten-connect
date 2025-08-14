package ui;

import model.Student;
import service.AuthService;
import service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student Management Panel - shows how to use database models in Swing
 */
public class StudentManagementPanel extends JPanel {
    private StudentService studentService;
    private AuthService authService;
    
    // UI Components
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField dobField;
    private JTextField addressField;
    private JTextField classIdField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    
    private Student selectedStudent;
    
    public StudentManagementPanel(AuthService authService) {
        this.authService = authService;
        this.studentService = new StudentService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadStudentData();
    }
    
    private void initializeComponents() {
        // Create table model
        String[] columnNames = {"ID", "Name", "Date of Birth", "Age", "Class", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create form fields
        nameField = new JTextField(20);
        dobField = new JTextField(20);
        dobField.setToolTipText("Format: YYYY-MM-DD (e.g., 2019-05-15)");
        addressField = new JTextField(20);
        classIdField = new JTextField(20);
        classIdField.setToolTipText("Enter class ID number");
        
        // Create buttons
        addButton = new JButton("Add Student");
        updateButton = new JButton("Update Student");
        deleteButton = new JButton("Delete Student");
        refreshButton = new JButton("Refresh");
        
        // Search components
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        
        // Initially disable update/delete buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));
        
        // Form panel
        JPanel formPanel = createFormPanel();
        
        // Add components to main panel
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nameField, gbc);
        
        // Date of birth field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Date of Birth:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(dobField, gbc);
        
        // Class ID field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Class ID:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(classIdField, gbc);
        
        // Address field
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(addressField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }
    
    private void setupEventHandlers() {
        // Table selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    loadSelectedStudent(selectedRow);
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    clearForm();
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });
        
        // Button listeners
        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        refreshButton.addActionListener(e -> loadStudentData());
        searchButton.addActionListener(e -> searchStudents());
        
        // Search field enter key
        searchField.addActionListener(e -> searchStudents());
    }
    
    private void loadStudentData() {
        try {
            List<Student> students = studentService.getAllStudents();
            updateTable(students);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading student data: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(List<Student> students) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add student data to table
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
            tableModel.addRow(rowData);
        }
    }
    
    private void loadSelectedStudent(int row) {
        int studentId = (Integer) tableModel.getValueAt(row, 0);
        selectedStudent = studentService.getStudentById(studentId);
        
        if (selectedStudent != null) {
            nameField.setText(selectedStudent.getName());
            dobField.setText(selectedStudent.getDob().toString());
            classIdField.setText(String.valueOf(selectedStudent.getClassId()));
            addressField.setText(selectedStudent.getAddress());
        }
    }
    
    private void addStudent() {
        try {
            Student student = createStudentFromForm();
            if (studentService.addStudent(student)) {
                JOptionPane.showMessageDialog(this, 
                    "Student added successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudentData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to add student.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStudent() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a student to update.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Student updatedStudent = createStudentFromForm();
            updatedStudent.setId(selectedStudent.getId());
            
            if (studentService.updateStudent(updatedStudent)) {
                JOptionPane.showMessageDialog(this, 
                    "Student updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudentData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to update student.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteStudent() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a student to delete.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete student: " + selectedStudent.getName() + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (studentService.deleteStudent(selectedStudent.getId())) {
                    JOptionPane.showMessageDialog(this, 
                        "Student deleted successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudentData();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to delete student.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchStudents() {
        String searchTerm = searchField.getText().trim();
        try {
            List<Student> students = studentService.searchStudents(searchTerm);
            updateTable(students);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching students: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Student createStudentFromForm() {
        String name = nameField.getText().trim();
        String dobText = dobField.getText().trim();
        String classIdText = classIdField.getText().trim();
        String address = addressField.getText().trim();
        
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Student name is required");
        }
        
        if (dobText.isEmpty()) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        
        if (classIdText.isEmpty()) {
            throw new IllegalArgumentException("Class ID is required");
        }
        
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD (e.g., 2019-05-15)");
        }
        
        int classId;
        try {
            classId = Integer.parseInt(classIdText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Class ID must be a number");
        }
        
        return new Student(name, dob, classId, address);
    }
    
    private void clearForm() {
        nameField.setText("");
        dobField.setText("");
        classIdField.setText("");
        addressField.setText("");
        selectedStudent = null;
        studentTable.clearSelection();
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
}
