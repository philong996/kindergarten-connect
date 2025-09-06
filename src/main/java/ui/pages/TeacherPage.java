package ui.pages;

import service.AuthService;
import service.StudentService;
import model.Student;
import ui.components.HeaderPanel;
import ui.panels.PhysicalDevelopmentPanel;
import ui.panels.AttendancePanel;
import ui.panels.AttendanceHistoryPanel;
import ui.panels.PostsPanel;
import ui.panels.ChatPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Teacher Page - Main window for Teacher users
 * Implements Template Method pattern via BaseAuthenticatedPage
 * Refactored to use reusable UI components
 */
public class TeacherPage extends BaseAuthenticatedPage {
    private HeaderPanel headerPanel;
    private JPanel mainPanel;
    
    // Tab constants for better maintainability
    private static final String TAB_ATTENDANCE = "Daily Attendance";
    private static final String TAB_ATTENDANCE_HISTORY = "Attendance History";
    private static final String TAB_POSTS = "Class Posts";
    private static final String TAB_MESSAGES = "Messages";
    private static final String TAB_PHYSICAL_DEVELOPMENT = "Physical Development";
    
    public TeacherPage(AuthService authService) {
        super(authService);
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Teacher Page";
    }
    
    @Override
    protected void initializeComponents() {
        headerPanel = HeaderPanel.createDashboard("Teacher", authService.getCurrentUser().getUsername());
        
        // Create tabbed pane for teacher features
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Attendance tab
        JPanel attendanceTab = createAttendanceTab();
        tabbedPane.addTab(TAB_ATTENDANCE, attendanceTab);
        
        // Attendance History tab
        JPanel attendanceHistoryTab = createAttendanceHistoryTab();
        tabbedPane.addTab(TAB_ATTENDANCE_HISTORY, attendanceHistoryTab);
        
        // Posts tab
        JPanel postsTab = createPostsTab();
        tabbedPane.addTab(TAB_POSTS, postsTab);
        
        // Messages tab
        JPanel messagesTab = createMessagesTab();
        tabbedPane.addTab(TAB_MESSAGES, messagesTab);
        
        // Physical Development tab
        JPanel physicalDevPanel = createPhysicalDevelopmentTab();
        tabbedPane.addTab(TAB_PHYSICAL_DEVELOPMENT, physicalDevPanel);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createAttendanceTab() {
        // For attendance, we need to get the teacher's class ID
        // For now, let's use classId = 1 as a default
        // In a real application, this would come from the logged-in teacher's data
        int classId = 1; // This should be retrieved from the teacher's profile
        int teacherId = authService.getCurrentUser().getId();
        
        AttendancePanel attendancePanel = new AttendancePanel(classId, teacherId);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(attendancePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAttendanceHistoryTab() {
        // For attendance history, we need to get the teacher's class ID
        int classId = 1; // This should be retrieved from the teacher's profile
        
        AttendanceHistoryPanel historyPanel = new AttendanceHistoryPanel(classId);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(historyPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPostsTab() {
        int currentUserId = authService.getCurrentUser().getId();
        String currentUserRole = authService.getCurrentUser().getRole();
        
        PostsPanel postsPanel = new PostsPanel(currentUserId, currentUserRole, authService);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(postsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMessagesTab() {
        int currentUserId = authService.getCurrentUser().getId();
        String currentUserRole = authService.getCurrentUser().getRole();
        
        ChatPanel chatPanel = new ChatPanel(currentUserId, currentUserRole, authService);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chatPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPhysicalDevelopmentTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Equal split
        
        // Left panel: Student list table
        JPanel leftPanel = createStudentListPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // Right panel: Student details and physical development
        JPanel rightPanel = createStudentDetailsPanel();
        splitPane.setRightComponent(rightPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStudentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Students"));
        
        // Create student table
        String[] columnNames = {"ID", "Name", "Age", "Class"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Load student data
        loadStudentTableData(tableModel);
        
        // Add selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
                    String studentName = (String) tableModel.getValueAt(selectedRow, 1);
                    System.out.println("Student selected: ID=" + studentId + ", Name=" + studentName);
                    showStudentDetails(studentId, studentName);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStudentDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        
        // Initial placeholder
        JLabel placeholderLabel = new JLabel("Select a student to view details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadStudentTableData(DefaultTableModel tableModel) {
        try {
            StudentService studentService = new StudentService();
            List<Student> students = studentService.getAllStudents();
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Add student data
            for (Student student : students) {
                Object[] rowData = {
                    student.getId(),
                    student.getName(),
                    student.getAge(),
                    student.getClassName() != null ? student.getClassName() : "Class " + student.getClassId()
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showStudentDetails(int studentId, String studentName) {
        // System.out.println("showStudentDetails called for: ID=" + studentId + ", Name=" + studentName);
        try {
            StudentService studentService = new StudentService();
            Student student = studentService.getStudentById(studentId);
            
            if (student == null) {
                JOptionPane.showMessageDialog(this, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create detailed student information panel
            JPanel detailsPanel = new JPanel(new BorderLayout());
            detailsPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
            
            // Student information section
            JPanel infoPanel = createStudentInfoPanel(student);
            detailsPanel.add(infoPanel, BorderLayout.NORTH);
            
            // Physical development section
            JPanel physicalPanel = new JPanel(new BorderLayout());
            physicalPanel.setBorder(BorderFactory.createTitledBorder("Physical Development"));
            
            // Create physical development panel
            // Get gender from student data - convert from MALE/FEMALE to boolean (true for boy)
            boolean isBoy = student.isMale(); // Use the new helper method
            PhysicalDevelopmentPanel physicalDevPanel = new PhysicalDevelopmentPanel(
                studentId, studentName, isBoy, authService.getCurrentUser().getId(), true
            );
            // System.out.println("Physical development panel created for student: " + studentName);
            physicalPanel.add(physicalDevPanel, BorderLayout.CENTER);
            
            detailsPanel.add(physicalPanel, BorderLayout.CENTER);
            
            // Update the right panel
            Component[] components = mainPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) comp;
                    
                    // Find Physical Development tab by title (more robust than hardcoded index)
                    int physicalDevTabIndex = findTabIndex(tabbedPane, TAB_PHYSICAL_DEVELOPMENT);
                    if (physicalDevTabIndex != -1) {
                        tabbedPane.setSelectedIndex(physicalDevTabIndex);
                        Component tabComponent = tabbedPane.getComponentAt(physicalDevTabIndex);
                    if (tabComponent instanceof JPanel) {
                            JPanel tabPanel = (JPanel) tabComponent;
                            Component[] tabComponents = tabPanel.getComponents();
                            if (tabComponents.length > 0 && tabComponents[0] instanceof JSplitPane) {
                                JSplitPane splitPane = (JSplitPane) tabComponents[0];
                                splitPane.setRightComponent(detailsPanel);
                                splitPane.revalidate();
                                splitPane.repaint();
                                // System.out.println("UI updated with student details panel");
                            } else {
                                System.out.println("Could not find split pane in tab components");
                            }
                        }
                    } else {
                        System.out.println("Physical Development tab not found");
                    }
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading student details: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createStudentInfoPanel(Student student) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Student ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        JLabel idLabel = new JLabel(String.valueOf(student.getId()));
        idLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(idLabel, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JLabel nameLabel = new JLabel(student.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(nameLabel, gbc);
        
        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(student.getDob() != null ? student.getDob().toString() : "N/A"), gbc);
        
        // Age
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(student.getAge())), gbc);
        
        // Class
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Class:"), gbc);
        gbc.gridx = 1;
        String className = student.getClassName() != null ? student.getClassName() : "Class " + student.getClassId();
        panel.add(new JLabel(className), gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(student.getAddress() != null ? student.getAddress() : "N/A"), gbc);
        
        return panel;
    }
    
    /**
     * Helper method to find tab index by title
     */
    private int findTabIndex(JTabbedPane tabbedPane, String title) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (title.equals(tabbedPane.getTitleAt(i))) {
                return i;
            }
        }
        return -1; // Tab not found
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void setupPermissions() {
        // Teacher-specific permissions setup
        headerPanel.setStatus("Teacher access - Limited features available");
        
        // Teachers will have access to student management, attendance, etc.
        // This will be expanded when implementing teacher-specific features
    }
    
    @Override
    protected void setupEventHandlers() {
        // Event handlers are already set up in the tab components
        // Menu bar event handlers are handled by the base class
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isTeacher();
    }
}
