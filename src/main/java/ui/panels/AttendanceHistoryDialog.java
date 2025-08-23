package ui.panels;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import dao.AttendanceDAO.AttendanceStats;
import dao.StudentDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog for viewing attendance history
 */
public class AttendanceHistoryDialog extends JDialog {
    private AttendanceService attendanceService;
    private StudentDAO studentDAO;
    private int classId;
    
    // Components
    private JComboBox<StudentItem> studentCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton searchButton;
    private JButton closeButton;
    
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;
    
    private JLabel statsLabel;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public AttendanceHistoryDialog(JFrame parent, int classId, AttendanceService attendanceService) {
        super(parent, "Attendance History", true);
        this.classId = classId;
        this.attendanceService = attendanceService;
        this.studentDAO = new StudentDAO();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadStudents();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Student selection
        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(200, 25));
        
        // Date range selection
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1); // Default to last month
        
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "dd/MM/yyyy");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setValue(java.sql.Date.valueOf(startDate));
        
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "dd/MM/yyyy");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setValue(java.sql.Date.valueOf(endDate));
        
        searchButton = new JButton("Search");
        closeButton = new JButton("Close");
        
        // History table
        String[] columnNames = {"Date", "Status", "Check-in Time", "Late Arrival", "Excuse Reason"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableScrollPane = new JScrollPane(historyTable);
        
        // Stats label
        statsLabel = new JLabel("Select a student and date range to view statistics", JLabel.CENTER);
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Student selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        searchPanel.add(new JLabel("Student:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        searchPanel.add(studentCombo, gbc);
        
        // Date range
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        searchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(startDateSpinner, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(endDateSpinner, gbc);
        
        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchButton, gbc);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(statsLabel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        
        // Main layout
        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        searchButton.addActionListener(e -> searchHistory());
        closeButton.addActionListener(e -> dispose());
        
        // Auto-search when student selection changes
        studentCombo.addActionListener(e -> {
            if (studentCombo.getSelectedItem() != null) {
                searchHistory();
            }
        });
    }
    
    private void loadStudents() {
        try {
            List<Student> students = studentDAO.findByClassId(classId);
            
            // Add "All Students" option
            studentCombo.addItem(new StudentItem(0, "All Students"));
            
            // Add individual students
            for (Student student : students) {
                studentCombo.addItem(new StudentItem(student.getId(), student.getName()));
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading students: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchHistory() {
        try {
            StudentItem selectedStudent = (StudentItem) studentCombo.getSelectedItem();
            if (selectedStudent == null) return;
            
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            LocalDate start = startDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            if (start.isAfter(end)) {
                JOptionPane.showMessageDialog(this,
                    "Start date cannot be after end date.",
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            List<Attendance> attendanceHistory;
            if (selectedStudent.getId() == 0) {
                // All students - get attendance by date range for the class
                attendanceHistory = new java.util.ArrayList<>();
                LocalDate current = start;
                while (!current.isAfter(end)) {
                    List<Attendance> dailyAttendance = attendanceService.getClassAttendance(classId, current);
                    attendanceHistory.addAll(dailyAttendance);
                    current = current.plusDays(1);
                }
            } else {
                // Specific student
                attendanceHistory = attendanceService.getAttendanceHistory(selectedStudent.getId(), start, end);
            }
            
            // Populate table
            for (Attendance attendance : attendanceHistory) {
                Object[] row = {
                    attendance.getDate().format(dateFormatter),
                    attendance.getStatus(),
                    attendance.getCheckInTime() != null ? 
                        attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                    attendance.getLateArrivalTime() != null ? 
                        attendance.getLateArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                    attendance.getExcuseReason() != null ? attendance.getExcuseReason() : ""
                };
                tableModel.addRow(row);
            }
            
            // Update statistics
            updateStatistics(selectedStudent.getId(), start, end);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching attendance history: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics(int studentId, LocalDate startDate, LocalDate endDate) {
        try {
            if (studentId == 0) {
                // All students statistics
                statsLabel.setText("Statistics not available for all students view");
                return;
            }
            
            AttendanceStats stats = attendanceService.getAttendanceStats(studentId, startDate, endDate);
            
            String statsText = String.format(
                "Period: %s to %s | Total Days: %d | Present: %d (%.1f%%) | Absent: %d | Late: %d (%.1f%%)",
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                stats.getTotalDays(),
                stats.getPresentDays(),
                stats.getAttendanceRate(),
                stats.getAbsentDays(),
                stats.getLateDays(),
                stats.getLateRate()
            );
            
            statsLabel.setText(statsText);
            
            // Color coding based on attendance rate
            if (stats.getAttendanceRate() >= 90) {
                statsLabel.setForeground(Color.GREEN.darker());
            } else if (stats.getAttendanceRate() >= 75) {
                statsLabel.setForeground(Color.ORANGE.darker());
            } else {
                statsLabel.setForeground(Color.RED);
            }
            
        } catch (Exception e) {
            statsLabel.setText("Error calculating statistics");
            statsLabel.setForeground(Color.RED);
        }
    }
    
    // Helper class for student combo box items
    private static class StudentItem {
        private final int id;
        private final String name;
        
        public StudentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            StudentItem that = (StudentItem) obj;
            return id == that.id;
        }
        
        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }
}
