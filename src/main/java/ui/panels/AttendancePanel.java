package ui.panels;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import service.AttendanceService.AttendanceSummary;
import ui.components.ButtonPanel;
import ui.components.DataTable;
import ui.components.DialogFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for managing daily attendance
 */
public class AttendancePanel extends JPanel {
    private AttendanceService attendanceService;
    private int classId;
    private int teacherId;
    private LocalDate selectedDate;
    
    // Components
    private JLabel titleLabel;
    private JLabel dateLabel;
    private JLabel summaryLabel;
    private JSpinner dateSpinner;
    private JButton todayButton;
    private JButton refreshButton;
    private JButton saveAllButton;
    private JButton markAllPresentButton;
    private JButton markAllAbsentButton;
    private JButton viewHistoryButton;
    
    private JTable attendanceTable;
    private AttendanceTableModel tableModel;
    private JScrollPane tableScrollPane;
    
    // Data
    private List<Attendance> attendanceList;
    private Map<Integer, Attendance> attendanceMap; // studentId -> Attendance
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public AttendancePanel(int classId, int teacherId) {
        this.classId = classId;
        this.teacherId = teacherId;
        this.attendanceService = new AttendanceService();
        this.selectedDate = LocalDate.now();
        this.attendanceList = new ArrayList<>();
        this.attendanceMap = new HashMap<>();
        
        initializeComponents();
        layoutComponents();
        loadAttendanceData();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Daily Attendance Management"));
        
        // Title and date selection
        titleLabel = new JLabel("Attendance for Class", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        dateLabel = new JLabel("Select Date:", JLabel.RIGHT);
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.sql.Date.valueOf(selectedDate));
        
        todayButton = new JButton("Today");
        refreshButton = new JButton("Refresh");
        
        // Summary label
        summaryLabel = new JLabel("", JLabel.CENTER);
        summaryLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Action buttons
        saveAllButton = new JButton("Save All Changes");
        markAllPresentButton = new JButton("Mark All Present");
        markAllAbsentButton = new JButton("Mark All Absent");
        viewHistoryButton = new JButton("View History");
        
        // Attendance table
        tableModel = new AttendanceTableModel();
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setRowHeight(30);
        
        // Set up custom renderers and editors for status column
        setupTableColumns();
        
        tableScrollPane = new JScrollPane(attendanceTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 400));
    }
    
    private void setupTableColumns() {
        // Status column with combo box
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        attendanceTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));
        
        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Student Name
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-in Time
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Late Time
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Excuse Reason
        attendanceTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Actions
        
        // Custom renderer for status column
        attendanceTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        // Custom editor for actions column
        attendanceTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        attendanceTable.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());
    }
    
    private void layoutComponents() {
        // Top panel with date selection and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Date selection panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(dateLabel);
        datePanel.add(dateSpinner);
        datePanel.add(todayButton);
        datePanel.add(refreshButton);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        summaryPanel.add(summaryLabel);
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(datePanel, BorderLayout.WEST);
        topPanel.add(summaryPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(markAllPresentButton);
        buttonPanel.add(markAllAbsentButton);
        buttonPanel.add(saveAllButton);
        buttonPanel.add(viewHistoryButton);
        
        // Main layout
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Date spinner change
        dateSpinner.addChangeListener(e -> {
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            selectedDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            loadAttendanceData();
        });
        
        // Today button
        todayButton.addActionListener(e -> {
            selectedDate = LocalDate.now();
            dateSpinner.setValue(java.sql.Date.valueOf(selectedDate));
            loadAttendanceData();
        });
        
        // Refresh button
        refreshButton.addActionListener(e -> loadAttendanceData());
        
        // Save all button
        saveAllButton.addActionListener(e -> saveAllChanges());
        
        // Mark all present button
        markAllPresentButton.addActionListener(e -> markAllStudents("PRESENT"));
        
        // Mark all absent button
        markAllAbsentButton.addActionListener(e -> markAllStudents("ABSENT"));
        
        // View history button
        viewHistoryButton.addActionListener(e -> showAttendanceHistory());
    }
    
    private void loadAttendanceData() {
        try {
            // Generate default attendance records for the selected date
            attendanceList = attendanceService.generateDefaultAttendance(classId, selectedDate);
            attendanceMap.clear();
            
            // Build the attendance map for quick lookup
            for (Attendance attendance : attendanceList) {
                attendanceMap.put(attendance.getStudentId(), attendance);
            }
            
            // Update table model
            tableModel.fireTableDataChanged();
            
            // Update summary
            updateSummary();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading attendance data: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSummary() {
        try {
            AttendanceSummary summary = attendanceService.getClassAttendanceSummary(classId, selectedDate);
            String summaryText = String.format(
                "Total: %d | Present: %d | Absent: %d | Late: %d | Attendance Rate: %.1f%%",
                summary.getTotalStudents(),
                summary.getPresentCount(),
                summary.getAbsentCount(),
                summary.getLateCount(),
                summary.getAttendanceRate()
            );
            summaryLabel.setText(summaryText);
            
            // Color coding based on attendance rate
            if (summary.getAttendanceRate() >= 90) {
                summaryLabel.setForeground(Color.GREEN.darker());
            } else if (summary.getAttendanceRate() >= 75) {
                summaryLabel.setForeground(Color.ORANGE.darker());
            } else {
                summaryLabel.setForeground(Color.RED);
            }
            
        } catch (Exception e) {
            summaryLabel.setText("Error calculating summary");
            summaryLabel.setForeground(Color.RED);
        }
    }
    
    private void saveAllChanges() {
        try {
            List<Attendance> toSave = new ArrayList<>();
            
            // Collect all attendance records that have been modified
            for (Attendance attendance : attendanceList) {
                if (attendance != null && attendance.getStudentId() > 0) {
                    toSave.add(attendance);
                }
            }
            
            if (toSave.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No changes to save.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Show confirmation dialog
            int result = JOptionPane.showConfirmDialog(this,
                "Save attendance for " + toSave.size() + " students on " + selectedDate.format(dateFormatter) + "?",
                "Confirm Save",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                boolean success = attendanceService.markBulkAttendance(toSave);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Attendance saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAttendanceData(); // Refresh to show saved data
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Some attendance records could not be saved. Please check the console for details.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving attendance: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void markAllStudents(String status) {
        int result = JOptionPane.showConfirmDialog(this,
            "Mark all students as " + status.toLowerCase() + " for " + selectedDate.format(dateFormatter) + "?",
            "Confirm Bulk Action",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            for (Attendance attendance : attendanceList) {
                if (attendance != null) {
                    attendance.setStatus(status);
                    
                    if ("PRESENT".equals(status)) {
                        attendance.setCheckInTime(LocalTime.now());
                        attendance.setLateArrivalTime(null);
                        attendance.setExcuseReason(null);
                    } else if ("ABSENT".equals(status)) {
                        attendance.setCheckInTime(null);
                        attendance.setLateArrivalTime(null);
                    } else if ("LATE".equals(status)) {
                        attendance.setCheckInTime(LocalTime.now());
                        attendance.setLateArrivalTime(LocalTime.now());
                    }
                }
            }
            
            tableModel.fireTableDataChanged();
            updateSummary();
        }
    }
    
    private void showAttendanceHistory() {
        // Show dialog to select date range and student
        AttendanceHistoryDialog historyDialog = new AttendanceHistoryDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            classId, 
            attendanceService
        );
        historyDialog.setVisible(true);
    }
    
    private void editAttendanceDetails(int row) {
        if (row >= 0 && row < attendanceList.size()) {
            Attendance attendance = attendanceList.get(row);
            AttendanceDetailDialog detailDialog = new AttendanceDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                attendance
            );
            
            if (detailDialog.showDialog()) {
                // Update the attendance record with details from dialog
                Attendance updatedAttendance = detailDialog.getAttendance();
                attendanceList.set(row, updatedAttendance);
                attendanceMap.put(updatedAttendance.getStudentId(), updatedAttendance);
                tableModel.fireTableRowsUpdated(row, row);
                updateSummary();
            }
        }
    }
    
    // Custom table model for attendance
    private class AttendanceTableModel extends DefaultTableModel {
        private final String[] columnNames = {
            "Student Name", "Date", "Status", "Check-in Time", "Late Arrival", "Excuse Reason", "Actions"
        };
        
        @Override
        public int getRowCount() {
            return attendanceList != null ? attendanceList.size() : 0;
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            if (row >= attendanceList.size()) return null;
            
            Attendance attendance = attendanceList.get(row);
            if (attendance == null) return null;
            
            switch (column) {
                case 0: return attendance.getStudentName();
                case 1: return attendance.getDate().format(dateFormatter);
                case 2: return attendance.getStatus();
                case 3: return attendance.getCheckInTime() != null ? 
                        attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
                case 4: return attendance.getLateArrivalTime() != null ? 
                        attendance.getLateArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
                case 5: return attendance.getExcuseReason() != null ? attendance.getExcuseReason() : "";
                case 6: return "Edit";
                default: return null;
            }
        }
        
        @Override
        public void setValueAt(Object value, int row, int column) {
            if (row >= attendanceList.size()) return;
            
            Attendance attendance = attendanceList.get(row);
            if (attendance == null) return;
            
            switch (column) {
                case 2: // Status
                    String newStatus = (String) value;
                    attendance.setStatus(newStatus);
                    
                    // Auto-update times based on status
                    if ("PRESENT".equals(newStatus)) {
                        if (attendance.getCheckInTime() == null) {
                            attendance.setCheckInTime(LocalTime.now());
                        }
                        attendance.setLateArrivalTime(null);
                    } else if ("ABSENT".equals(newStatus)) {
                        attendance.setCheckInTime(null);
                        attendance.setLateArrivalTime(null);
                    } else if ("LATE".equals(newStatus)) {
                        if (attendance.getLateArrivalTime() == null) {
                            attendance.setLateArrivalTime(LocalTime.now());
                        }
                        if (attendance.getCheckInTime() == null) {
                            attendance.setCheckInTime(attendance.getLateArrivalTime());
                        }
                    }
                    
                    updateSummary();
                    fireTableRowsUpdated(row, row);
                    break;
                    
                case 5: // Excuse Reason
                    attendance.setExcuseReason((String) value);
                    break;
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 5 || column == 6; // Status, Excuse Reason, and Actions
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 6) return JButton.class;
            return String.class;
        }
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
    
    // Custom button renderer for actions column
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Edit");
            return this;
        }
    }
    
    // Custom button editor for actions column
    private class ActionButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;
        
        public ActionButtonEditor() {
            super(new JCheckBox());
            button = new JButton("Edit");
            button.setOpaque(true);
            button.addActionListener(e -> editAttendanceDetails(selectedRow));
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Edit";
        }
    }
}
