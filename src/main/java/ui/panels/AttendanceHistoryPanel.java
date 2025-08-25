package ui.panels;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import dao.AttendanceDAO.AttendanceStats;
import dao.StudentDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for viewing attendance history and statistics
 */
public class AttendanceHistoryPanel extends JPanel {
    private AttendanceService attendanceService;
    private StudentDAO studentDAO;
    private int classId;
    
    // Components
    private JComboBox<StudentItem> studentCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton searchButton;
    private JButton refreshButton;
    
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;
    
    private JLabel statsLabel;
    private JLabel titleLabel;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public AttendanceHistoryPanel(int classId, AttendanceService attendanceService) {
        this.classId = classId;
        this.attendanceService = attendanceService;
        this.studentDAO = new StudentDAO();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadStudents();
    }
    
    // Convenience constructor that creates its own AttendanceService
    public AttendanceHistoryPanel(int classId) {
        this(classId, new AttendanceService());
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Attendance History & Statistics"));
        
        // Title
        titleLabel = new JLabel("Attendance History", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
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
        refreshButton = new JButton("Refresh");
        
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
        historyTable.setRowHeight(25);
        
        // Set up custom renderer for status column
        setupTableColumns();
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Status
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Check-in
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Late arrival
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Excuse reason
        
        tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Stats label
        statsLabel = new JLabel("Select a student and date range to view statistics", JLabel.CENTER);
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupTableColumns() {
        // Set up custom renderer for status column (column index 1)
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
    }
    
    private void layoutComponents() {
        // Top panel with title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search criteria panel
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
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        searchPanel.add(startDateSpinner, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        searchPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        searchPanel.add(endDateSpinner, gbc);
        
        // Buttons
        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        searchPanel.add(searchButton, gbc);
        gbc.gridx = 5;
        searchPanel.add(refreshButton, gbc);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(statsLabel, BorderLayout.CENTER);
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        searchButton.addActionListener(e -> searchHistory());
        refreshButton.addActionListener(e -> {
            loadStudents();
            clearResults();
        });
        
        // Auto-search when student selection changes
        studentCombo.addActionListener(e -> {
            if (studentCombo.getSelectedItem() != null) {
                searchHistory();
            }
        });
    }
    
    private void loadStudents() {
        try {
            studentCombo.removeAllItems();
            
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
    
    private void clearResults() {
        tableModel.setRowCount(0);
        statsLabel.setText("Select a student and date range to view statistics");
        statsLabel.setForeground(Color.BLACK);
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
                    // Only add records that have actual attendance data (not default absent)
                    for (Attendance attendance : dailyAttendance) {
                        if (attendance.getId() > 0) { // Only saved records
                            attendanceHistory.add(attendance);
                        }
                    }
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
            
            // Update title to show search results
            if (selectedStudent.getId() == 0) {
                titleLabel.setText("Attendance History - All Students (" + attendanceHistory.size() + " records)");
            } else {
                titleLabel.setText("Attendance History - " + selectedStudent.getName() + " (" + attendanceHistory.size() + " records)");
            }
            
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
                // All students statistics - calculate overall class stats
                statsLabel.setText("Overall class statistics for " + startDate.format(dateFormatter) + 
                                 " to " + endDate.format(dateFormatter) + " - " + tableModel.getRowCount() + " total records");
                statsLabel.setForeground(Color.BLUE);
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
    
    /**
     * Public method to refresh the panel data
     */
    public void refresh() {
        loadStudents();
        clearResults();
        titleLabel.setText("Attendance History");
    }
    
    /**
     * Public method to set the selected student
     */
    public void setSelectedStudent(int studentId) {
        for (int i = 0; i < studentCombo.getItemCount(); i++) {
            StudentItem item = studentCombo.getItemAt(i);
            if (item.getId() == studentId) {
                studentCombo.setSelectedItem(item);
                break;
            }
        }
    }
    
    /**
     * Public method to set the date range
     */
    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        startDateSpinner.setValue(java.sql.Date.valueOf(startDate));
        endDateSpinner.setValue(java.sql.Date.valueOf(endDate));
    }
    
    /**
     * Public method to trigger search programmatically
     */
    public void performSearch() {
        searchHistory();
    }
    
    // Custom cell renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String status = (String) value;
                if ("PRESENT".equals(status)) {
                    c.setBackground(Color.GREEN.brighter());
                } else if ("ABSENT".equals(status)) {
                    c.setBackground(Color.RED.brighter());
                } else if ("LATE".equals(status)) {
                    c.setBackground(Color.YELLOW.brighter());
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            
            return c;
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
