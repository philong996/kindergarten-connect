package ui.panels;

import model.Attendance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for editing detailed attendance information
 */
public class AttendanceDetailDialog extends JDialog {
    private Attendance attendance;
    private boolean confirmed = false;
    
    // Components
    private JLabel studentNameLabel;
    private JLabel dateLabel;
    private JComboBox<String> statusCombo;
    private JTextField checkInTimeField;
    private JTextField lateArrivalTimeField;
    private JTextArea excuseReasonArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public AttendanceDetailDialog(JFrame parent, Attendance attendance) {
        super(parent, "Edit Attendance Details", true);
        this.attendance = new Attendance(); // Create a copy
        copyAttendanceData(attendance);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadData();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void copyAttendanceData(Attendance source) {
        this.attendance.setId(source.getId());
        this.attendance.setStudentId(source.getStudentId());
        this.attendance.setStudentName(source.getStudentName());
        this.attendance.setDate(source.getDate());
        this.attendance.setStatus(source.getStatus());
        this.attendance.setCheckInTime(source.getCheckInTime());
        this.attendance.setLateArrivalTime(source.getLateArrivalTime());
        this.attendance.setExcuseReason(source.getExcuseReason());
        this.attendance.setCreatedAt(source.getCreatedAt());
    }
    
    private void initializeComponents() {
        studentNameLabel = new JLabel();
        studentNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        dateLabel = new JLabel();
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        statusCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        
        checkInTimeField = new JTextField(10);
        checkInTimeField.setToolTipText("Format: HH:MM (24-hour)");
        
        lateArrivalTimeField = new JTextField(10);
        lateArrivalTimeField.setToolTipText("Format: HH:MM (24-hour)");
        
        excuseReasonArea = new JTextArea(4, 20);
        excuseReasonArea.setLineWrap(true);
        excuseReasonArea.setWrapStyleWord(true);
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.add(studentNameLabel, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(statusCombo, gbc);
        
        // Check-in time
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Check-in Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(checkInTimeField, gbc);
        
        // Late arrival time
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Late Arrival Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lateArrivalTimeField, gbc);
        
        // Excuse reason
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Excuse Reason:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(excuseReasonArea), gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        statusCombo.addActionListener(e -> updateFieldsBasedOnStatus());
        
        saveButton.addActionListener(e -> {
            if (validateAndSaveData()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadData() {
        studentNameLabel.setText(attendance.getStudentName());
        dateLabel.setText("Date: " + attendance.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        
        statusCombo.setSelectedItem(attendance.getStatus());
        
        if (attendance.getCheckInTime() != null) {
            checkInTimeField.setText(attendance.getCheckInTime().format(timeFormatter));
        }
        
        if (attendance.getLateArrivalTime() != null) {
            lateArrivalTimeField.setText(attendance.getLateArrivalTime().format(timeFormatter));
        }
        
        if (attendance.getExcuseReason() != null) {
            excuseReasonArea.setText(attendance.getExcuseReason());
        }
        
        updateFieldsBasedOnStatus();
    }
    
    private void updateFieldsBasedOnStatus() {
        String status = (String) statusCombo.getSelectedItem();
        
        switch (status) {
            case "PRESENT":
                checkInTimeField.setEnabled(true);
                lateArrivalTimeField.setEnabled(false);
                lateArrivalTimeField.setText("");
                excuseReasonArea.setEnabled(false);
                break;
                
            case "ABSENT":
                checkInTimeField.setEnabled(false);
                checkInTimeField.setText("");
                lateArrivalTimeField.setEnabled(false);
                lateArrivalTimeField.setText("");
                excuseReasonArea.setEnabled(true);
                break;
                
            case "LATE":
                checkInTimeField.setEnabled(true);
                lateArrivalTimeField.setEnabled(true);
                excuseReasonArea.setEnabled(true);
                break;
        }
    }
    
    private boolean validateAndSaveData() {
        try {
            String status = (String) statusCombo.getSelectedItem();
            attendance.setStatus(status);
            
            // Validate and set check-in time
            String checkInText = checkInTimeField.getText().trim();
            if (!checkInText.isEmpty()) {
                try {
                    LocalTime checkInTime = LocalTime.parse(checkInText, timeFormatter);
                    attendance.setCheckInTime(checkInTime);
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid check-in time format. Please use HH:MM format.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                attendance.setCheckInTime(null);
            }
            
            // Validate and set late arrival time
            String lateText = lateArrivalTimeField.getText().trim();
            if (!lateText.isEmpty()) {
                try {
                    LocalTime lateTime = LocalTime.parse(lateText, timeFormatter);
                    attendance.setLateArrivalTime(lateTime);
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid late arrival time format. Please use HH:MM format.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                attendance.setLateArrivalTime(null);
            }
            
            // Set excuse reason
            String excuseReason = excuseReasonArea.getText().trim();
            attendance.setExcuseReason(excuseReason.isEmpty() ? null : excuseReason);
            
            // Validate based on status
            if ("PRESENT".equals(status) && attendance.getCheckInTime() == null) {
                JOptionPane.showMessageDialog(this,
                    "Check-in time is required for present students.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if ("LATE".equals(status) && attendance.getLateArrivalTime() == null) {
                JOptionPane.showMessageDialog(this,
                    "Late arrival time is required for late students.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // If late student has check-in time but no late arrival time, copy check-in to late arrival
            if ("LATE".equals(status) && attendance.getCheckInTime() != null && attendance.getLateArrivalTime() == null) {
                attendance.setLateArrivalTime(attendance.getCheckInTime());
            }
            
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error validating data: " + e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
    
    public Attendance getAttendance() {
        return attendance;
    }
}
