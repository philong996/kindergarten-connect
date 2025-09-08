package ui.panels;

import model.Attendance;
import ui.components.AppColor;
import ui.components.CustomButton;
import ui.components.CustomButton.accountType;
import ui.components.CustomMessageDialog;
import util.CameraUtil;
import util.ImageViewerUtil;
import javax.swing.*;
import java.awt.*;
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
    private JTextField checkOutTimeField;
    private JTextField lateArrivalTimeField;
    private JTextArea excuseReasonArea;
    private CustomButton saveButton;
    private CustomButton cancelButton;
    private JButton viewCheckInImageButton;
    private JButton captureCheckInImageButton;
    private JButton viewCheckOutImageButton;
    private JButton captureCheckOutImageButton;
    
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public AttendanceDetailDialog(JFrame parent, Attendance attendance) {
        super(parent, "Edit Attendance Details", true);
        this.attendance = new Attendance(); // Create a copy
        copyAttendanceData(attendance);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadData();
        
        setSize(500, 350);
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
        this.attendance.setCheckOutTime(source.getCheckOutTime());
        this.attendance.setLateArrivalTime(source.getLateArrivalTime());
        this.attendance.setExcuseReason(source.getExcuseReason());
        this.attendance.setCheckInImage(source.getCheckInImage());
        this.attendance.setCheckOutImage(source.getCheckOutImage());
        this.attendance.setCreatedAt(source.getCreatedAt());
    }
    
    private void initializeComponents() {
        setBackground(AppColor.getColor("lightViolet"));
        getContentPane().setBackground(AppColor.getColor("lightViolet"));

        studentNameLabel = new JLabel();
        // studentNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        studentNameLabel.setFont(studentNameLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        dateLabel = new JLabel();
        // dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 12f));
        
        statusCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        
        checkInTimeField = new JTextField(10);
        checkInTimeField.setToolTipText("Format: HH:MM (24-hour)");
        
        checkOutTimeField = new JTextField(10);
        checkOutTimeField.setToolTipText("Format: HH:MM (24-hour)");
        
        lateArrivalTimeField = new JTextField(10);
        lateArrivalTimeField.setToolTipText("Format: HH:MM (24-hour)");
        
        excuseReasonArea = new JTextArea(4, 20);
        excuseReasonArea.setLineWrap(true);
        excuseReasonArea.setWrapStyleWord(true);
        
        // Image buttons
        viewCheckInImageButton = new JButton("View");
        captureCheckInImageButton = new JButton("Capture");
        viewCheckOutImageButton = new JButton("View");
        captureCheckOutImageButton = new JButton("Capture");
        
        saveButton = new CustomButton("Save", accountType.TEACHER);
        cancelButton = new CustomButton("Cancel", accountType.TEACHER);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.add(studentNameLabel, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        headerPanel.setOpaque(false);
        
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
        
        // Check-in image buttons
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JPanel checkInImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        checkInImagePanel.add(viewCheckInImageButton);
        checkInImagePanel.add(captureCheckInImageButton);
        checkInImagePanel.setOpaque(false);
        formPanel.add(checkInImagePanel, gbc);
        
        // Check-out time
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Check-out Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(checkOutTimeField, gbc);
        
        // Check-out image buttons
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JPanel checkOutImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        checkOutImagePanel.add(viewCheckOutImageButton);
        checkOutImagePanel.add(captureCheckOutImageButton);
        checkOutImagePanel.setOpaque(false);
        formPanel.add(checkOutImagePanel, gbc);
        
        // Late arrival time
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Late Arrival Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lateArrivalTimeField, gbc);
        
        // Excuse reason
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Excuse Reason:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(excuseReasonArea), gbc);
        formPanel.setOpaque(false);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setOpaque(false);
        
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        statusCombo.addActionListener(e -> updateFieldsBasedOnStatus());
        
        // Image button handlers
        viewCheckInImageButton.addActionListener(e -> {
            if (attendance.getCheckInImage() != null) {
                ImageViewerUtil.showImage(this, attendance.getCheckInImage(), 
                    "Check-in Image - " + attendance.getStudentName());
            } else {
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "info", 
                    "No check-in image available", 
                    CustomMessageDialog.Type.INFO);
                // JOptionPane.showMessageDialog(this, "No check-in image available", 
                //     "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        captureCheckInImageButton.addActionListener(e -> {
            byte[] imageData = CameraUtil.captureImage(this, "Capture Check-in Image");
            if (imageData != null) {
                attendance.setCheckInImage(imageData);
                if (attendance.getCheckInTime() == null) {
                    checkInTimeField.setText(LocalTime.now().format(timeFormatter));
                }
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "info", 
                    "Check-in image captured!", 
                    CustomMessageDialog.Type.SUCCESS);
                // JOptionPane.showMessageDialog(this, "Check-in image captured!", 
                //     "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        viewCheckOutImageButton.addActionListener(e -> {
            if (attendance.getCheckOutImage() != null) {
                ImageViewerUtil.showImage(this, attendance.getCheckOutImage(), 
                    "Check-out Image - " + attendance.getStudentName());
            } else {
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "info", 
                    "No check-out image available", 
                    CustomMessageDialog.Type.INFO);
                // JOptionPane.showMessageDialog(this, "No check-out image available", 
                //     "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        captureCheckOutImageButton.addActionListener(e -> {
            if (attendance.getCheckInTime() == null && checkInTimeField.getText().trim().isEmpty()) {
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "warning", 
                    "Student must check-in before check-out", 
                    CustomMessageDialog.Type.ERROR);
                // JOptionPane.showMessageDialog(this, 
                //     "Student must check-in before check-out", 
                //     "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            byte[] imageData = CameraUtil.captureImage(this, "Capture Check-out Image");
            if (imageData != null) {
                attendance.setCheckOutImage(imageData);
                if (attendance.getCheckOutTime() == null) {
                    checkOutTimeField.setText(LocalTime.now().format(timeFormatter));
                }
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "success", 
                        "Check-out image captured!", 
                        CustomMessageDialog.Type.SUCCESS);
                // JOptionPane.showMessageDialog(this, "Check-out image captured!", 
                //     "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
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
        
        if (attendance.getCheckOutTime() != null) {
            checkOutTimeField.setText(attendance.getCheckOutTime().format(timeFormatter));
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
                checkInTimeField.setBackground(Color.WHITE);
                checkOutTimeField.setEnabled(true);
                checkOutTimeField.setBackground(Color.WHITE);
                lateArrivalTimeField.setEnabled(false);
                lateArrivalTimeField.setBackground(AppColor.getColor("softViolet"));
                lateArrivalTimeField.setText("");
                excuseReasonArea.setEnabled(false);
                captureCheckInImageButton.setEnabled(true);
                captureCheckOutImageButton.setEnabled(true);
                viewCheckInImageButton.setEnabled(true);
                viewCheckOutImageButton.setEnabled(true);
                break;
                
            case "ABSENT":
                checkInTimeField.setEnabled(false);
                checkInTimeField.setText("");
                checkInTimeField.setBackground(AppColor.getColor("softViolet"));
                checkOutTimeField.setEnabled(false);
                checkOutTimeField.setText("");
                checkOutTimeField.setBackground(AppColor.getColor("softViolet"));
                lateArrivalTimeField.setEnabled(false);
                lateArrivalTimeField.setText("");
                lateArrivalTimeField.setBackground(AppColor.getColor("softViolet"));
                excuseReasonArea.setEnabled(true);
                viewCheckInImageButton.setEnabled(false);
                viewCheckOutImageButton.setEnabled(false);
                captureCheckInImageButton.setEnabled(false);
                captureCheckOutImageButton.setEnabled(false);
                break;
                
            case "LATE":
                checkInTimeField.setEnabled(true);
                checkInTimeField.setBackground(Color.WHITE);
                checkOutTimeField.setEnabled(true);
                checkOutTimeField.setBackground(Color.WHITE);
                lateArrivalTimeField.setEnabled(true);
                lateArrivalTimeField.setBackground(Color.WHITE);
                excuseReasonArea.setEnabled(true);
                excuseReasonArea.setBackground(Color.WHITE);
                viewCheckInImageButton.setEnabled(true);
                viewCheckOutImageButton.setEnabled(true);
                captureCheckInImageButton.setEnabled(true);
                captureCheckOutImageButton.setEnabled(true);
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
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                        "Invalid check-in time format. Please use HH:MM format.", 
                        CustomMessageDialog.Type.ERROR);
                    // JOptionPane.showMessageDialog(this,
                    //     "Invalid check-in time format. Please use HH:MM format.",
                    //     "Validation Error",
                    //     JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                attendance.setCheckInTime(null);
            }
            
            // Validate and set check-out time
            String checkOutText = checkOutTimeField.getText().trim();
            if (!checkOutText.isEmpty()) {
                try {
                    LocalTime checkOutTime = LocalTime.parse(checkOutText, timeFormatter);
                    attendance.setCheckOutTime(checkOutTime);
                } catch (DateTimeParseException e) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                        "Invalid check-out time format. Please use HH:MM format.", 
                        CustomMessageDialog.Type.ERROR);
                    // JOptionPane.showMessageDialog(this,
                    //     "Invalid check-out time format. Please use HH:MM format.",
                    //     "Validation Error",
                    //     JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                attendance.setCheckOutTime(null);
            }
            
            // Validate and set late arrival time
            String lateText = lateArrivalTimeField.getText().trim();
            if (!lateText.isEmpty()) {
                try {
                    LocalTime lateTime = LocalTime.parse(lateText, timeFormatter);
                    attendance.setLateArrivalTime(lateTime);
                } catch (DateTimeParseException e) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                        "Invalid late arrival time format. Please use HH:MM format.", 
                        CustomMessageDialog.Type.ERROR);
                    // JOptionPane.showMessageDialog(this,
                    //     "Invalid late arrival time format. Please use HH:MM format.",
                    //     "Validation Error",
                    //     JOptionPane.ERROR_MESSAGE);
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
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                    "Check-in time is required for present students.",
                    CustomMessageDialog.Type.ERROR);
                // JOptionPane.showMessageDialog(this,
                //     "Check-in time is required for present students.",
                //     "Validation Error",
                //     JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if ("LATE".equals(status) && attendance.getLateArrivalTime() == null) {
                CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                    "Late arrival time is required for late students.",
                    CustomMessageDialog.Type.ERROR);
                // JOptionPane.showMessageDialog(this,
                //     "Late arrival time is required for late students.",
                //     "Validation Error",
                //     JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // If late student has check-in time but no late arrival time, copy check-in to late arrival
            if ("LATE".equals(status) && attendance.getCheckInTime() != null && attendance.getLateArrivalTime() == null) {
                attendance.setLateArrivalTime(attendance.getCheckInTime());
            }
            
            return true;
            
        } catch (Exception e) {
            CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(AttendanceDetailDialog.this), "error", 
                "Error validating data: " + e.getMessage(),
                CustomMessageDialog.Type.ERROR);
            // JOptionPane.showMessageDialog(this,
            //     "Error validating data: " + e.getMessage(),
            //     "Validation Error",
            //     JOptionPane.ERROR_MESSAGE);
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
