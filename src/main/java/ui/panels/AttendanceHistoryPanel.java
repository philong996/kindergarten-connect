package ui.panels;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import service.AuthService;
import service.ParentService;
import ui.components.AppColor;
import ui.components.CustomButton;
import ui.components.CustomMessageDialog;
import ui.components.RoundedBorder;
import ui.components.CustomButton.accountType;
import dao.AttendanceDAO.AttendanceStats;
import dao.StudentDAO;
import util.ImageViewerUtil;
import util.ProfileImageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
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
    
    // Data storage for attendance records
    private List<Attendance> currentAttendanceData;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private String role;

    // Colors based on role
    private Color BACKGROUND_COLOR;
    private Color BORDER_COLOR;
    
    public AttendanceHistoryPanel(int classId, AttendanceService attendanceService, AuthService authService) {
        this.classId = classId;
        this.attendanceService = attendanceService;
        this.studentDAO = new StudentDAO();
        role = authService.getCurrentUser().getRole();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadStudents();
    }
    
    // Convenience constructor that creates its own AttendanceService
    public AttendanceHistoryPanel(int classId, AuthService authServic) {
        this(classId, new AttendanceService(), authServic);
    }
    
    private void initializeComponents() {
        switch (role) {
            case "PRINCIPAL":
                throw new IllegalArgumentException("This panel is not available for PRINCIPAL role");
            case "TEACHER":
                BACKGROUND_COLOR = AppColor.getColor("softViolet");
                BORDER_COLOR = AppColor.getColor("darkViolet");
                break;
            case "PARENT":
                BACKGROUND_COLOR = AppColor.getColor("culture");
                BORDER_COLOR = AppColor.getColor("darkGreen");
                break;
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createTitledBorder("Attendance History & Statistics"));
        
        // Title
        titleLabel = new JLabel("Attendance History", JLabel.CENTER);
        titleLabel.setFont(getFont().deriveFont(Font.BOLD, 16f));
        // titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
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
        String[] columnNames = {"Date", "Status", "Check-in Time", "Check-out Time", "Late Arrival", "Excuse Reason", "Images"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Images column is editable (for button clicks)
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);
        historyTable.setOpaque(false);
        historyTable.setBackground(new Color(0, 0, 0, 0)); // trong suốt
        historyTable.setShowGrid(false); 
        JTableHeader header = historyTable.getTableHeader();
            header.setDefaultRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    JLabel label = new JLabel(value.toString(), SwingConstants.CENTER);
                    label.setOpaque(true);
                    label.setBackground(BACKGROUND_COLOR); 
                    label.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2, true)); // bo tròn
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    return label;
                }
            });
        
        // Set up custom renderer for status column
        setupTableColumns();
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Status
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Check-in
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-out
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Late arrival
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Excuse reason
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Images
        
        tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Stats label
        statsLabel = new JLabel("Select a student and date range to view statistics", JLabel.CENTER);
        statsLabel.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        statsLabel.setForeground(BORDER_COLOR);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupTableColumns() {
        // Set up custom renderer for status column (column index 1)
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        
        // Set up custom renderer and editor for images column (column index 6)
        historyTable.getColumnModel().getColumn(6).setCellRenderer(new ImageButtonRenderer());
        historyTable.getColumnModel().getColumn(6).setCellEditor(new ImageButtonEditor());
    }
    
    private void layoutComponents() {
        // Top panel with title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titlePanel.setOpaque(false);
        
        // Search criteria panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Student selection
        
        if ("TEACHER".equals(role)) {
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            searchPanel.add(new JLabel("Student:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            gbc.gridwidth = 3;
            searchPanel.add(studentCombo, gbc);
        }
        
        
        // Date range
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
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
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(statsLabel, BorderLayout.CENTER);
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
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
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "error", 
                "Error loading students: " + e.getMessage(),
                CustomMessageDialog.Type.ERROR);
            // JOptionPane.showMessageDialog(this,
            //     "Error loading students: " + e.getMessage(),
            //     "Error",
            //     JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearResults() {
        tableModel.setRowCount(0);
        currentAttendanceData = new java.util.ArrayList<>();
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
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "error", 
                    "Start date cannot be after end date.",
                    CustomMessageDialog.Type.ERROR);
                // JOptionPane.showMessageDialog(this,
                //     "Start date cannot be after end date.",
                //     "Invalid Date Range",
                //     JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Store current attendance data for image viewing
            currentAttendanceData = new java.util.ArrayList<>();
            
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
                currentAttendanceData.add(attendance); // Store for image viewing
                Object[] row = {
                    attendance.getDate().format(dateFormatter),
                    attendance.getStatus(),
                    attendance.getCheckInTime() != null ? 
                        attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                    attendance.getCheckOutTime() != null ? 
                        attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                    attendance.getLateArrivalTime() != null ? 
                        attendance.getLateArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                    attendance.getExcuseReason() != null ? attendance.getExcuseReason() : "",
                    "View" // Images column button
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
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(this), "error", 
                "Error searching attendance history: " + e.getMessage(),
                CustomMessageDialog.Type.ERROR);
            // JOptionPane.showMessageDialog(this,
            //     "Error searching attendance history: " + e.getMessage(),
            //     "Error",
            //     JOptionPane.ERROR_MESSAGE);
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
                    c.setBackground(AppColor.getColor("lightGreen"));
                } else if ("ABSENT".equals(status)) {
                    c.setBackground(AppColor.getColor("lightRed"));
                } else if ("LATE".equals(status)) {
                    c.setBackground(AppColor.getColor("lightOrange"));
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
    
    // Custom button renderer for images column
    private class ImageButtonRenderer extends JButton implements TableCellRenderer {
        public ImageButtonRenderer() {
            setOpaque(false);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            return this;
        }
    }
    
    // Custom button editor for images column
    private class ImageButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;
        
        public ImageButtonEditor() {
            super(new JCheckBox());
            button = new JButton("View");
            button.setOpaque(false);
            button.addActionListener(e -> showAttendanceImages(selectedRow));
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "View";
        }
    }
    
    /**
     * Shows check-in and check-out images for the selected attendance record
     */
    private void showAttendanceImages(int row) {
        if (row >= 0 && row < currentAttendanceData.size()) {
            Attendance attendance = currentAttendanceData.get(row);
            
            JDialog imageDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                "Attendance Images - " + attendance.getStudentName() + " (" + 
                attendance.getDate().format(dateFormatter) + ")", true);
            imageDialog.setLayout(new BorderLayout());
            imageDialog.setBackground(BACKGROUND_COLOR);
            
            JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
            imagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            imagePanel.setOpaque(false);
            
            // Check-in image section
            JPanel checkInPanel = new JPanel(new BorderLayout());
            checkInPanel.setBorder(BorderFactory.createTitledBorder("Check-in Image"));

            if (attendance.getCheckInImage() != null) {
                byte[] imageData = (byte[]) attendance.getCheckInImage();
                ImageIcon profileImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 200, 200);
                JLabel imageLabel = new JLabel(profileImage);
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                checkInPanel.add(imageLabel, BorderLayout.CENTER);
                
            } else {
                ImageIcon icon = new ImageIcon(getClass().getResource("/images/" + role + "/photo.png"));
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(img));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                checkInPanel.add(imageLabel, BorderLayout.CENTER);
            }
            
            // Check-out image section
            JPanel checkOutPanel = new JPanel(new BorderLayout());
            checkOutPanel.setBorder(BorderFactory.createTitledBorder("Check-out Image"));
            if (attendance.getCheckInImage() != null) {
                byte[] imageData = (byte[]) attendance.getCheckOutImage();
                ImageIcon profileImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 200, 200);
                JLabel imageLabel = new JLabel(profileImage);
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                checkOutPanel.add(imageLabel, BorderLayout.CENTER);
                
            } else {
                ImageIcon icon = new ImageIcon(getClass().getResource("/images/" + role + "/photo.png"));
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(img));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                checkOutPanel.add(imageLabel, BorderLayout.CENTER);
            }
            checkInPanel.setOpaque(false);
            checkOutPanel.setOpaque(false);
            
            imagePanel.add(checkInPanel);
            imagePanel.add(checkOutPanel);
            
            // Information panel
            JPanel infoPanel = new JPanel(new FlowLayout());
            infoPanel.setBorder(BorderFactory.createTitledBorder("Attendance Information"));
            
            String infoText = String.format(
                "<html>Student: %s<br/>Date: %s<br/>Status: %s<br/>Check-in: %s<br/>Check-out: %s</html>",
                attendance.getStudentName(),
                attendance.getDate().format(dateFormatter),
                attendance.getStatus(),
                attendance.getCheckInTime() != null ? 
                    attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A",
                attendance.getCheckOutTime() != null ? 
                    attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A"
            );
            
            JLabel infoLabel = new JLabel(infoText);
            infoLabel.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            infoPanel.add(infoLabel);
            infoPanel.setOpaque(false);
            
            // Close button
            // JButton closeBtn = new JButton("Close");
            CustomButton closeBtn = new CustomButton("Close", "TEACHER".equals(role) ? accountType.TEACHER : accountType.PARENT);
            closeBtn.addActionListener(e -> imageDialog.dispose());
            JPanel closePanel = new JPanel(new FlowLayout());
            closePanel.add(closeBtn);
            closePanel.setOpaque(false);
            
            imageDialog.add(infoPanel, BorderLayout.NORTH);
            imageDialog.add(imagePanel, BorderLayout.CENTER);
            imageDialog.add(closePanel, BorderLayout.SOUTH);
            
            imageDialog.setSize(600, 450);
            imageDialog.setLocationRelativeTo(this);
            imageDialog.setBackground(BACKGROUND_COLOR);
            imageDialog.setVisible(true);
        }
    }
}
