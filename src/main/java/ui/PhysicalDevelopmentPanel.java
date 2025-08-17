package ui;

import model.PhysicalDevelopmentRecord;
import service.PhysicalDevelopmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for displaying student physical development data
 */
public class PhysicalDevelopmentPanel extends JPanel {
    private PhysicalDevelopmentService physicalService;
    private int studentId;
    private String studentName;
    private int teacherId; // For teacher operations
    private boolean isTeacherView;
    
    // Components
    private JLabel nameLabel;
    private JLabel currentHeightLabel;
    private JLabel currentWeightLabel;
    private JLabel currentBMILabel;
    private JLabel ageLabel;
    private JLabel lastMeasurementLabel;
    private JLabel heightChangeLabel;
    private JLabel weightChangeLabel;
    private JLabel bmiChangeLabel;
    private JLabel growthTrendLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton addRecordButton;
    private JButton editRecordButton;
    private JButton deleteRecordButton;
    
    private DecimalFormat df = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public PhysicalDevelopmentPanel(int studentId, String studentName, int teacherId, boolean isTeacherView) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.teacherId = teacherId;
        this.isTeacherView = isTeacherView;
        this.physicalService = new PhysicalDevelopmentService();
        
        initializeComponents();
        layoutComponents();
        loadData();
        
        if (isTeacherView) {
            setupTeacherActions();
        }
    }
    
    // Constructor for parent view (no teacher actions)
    public PhysicalDevelopmentPanel(int studentId, String studentName) {
        this(studentId, studentName, -1, false);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Physical Development Tracking"));
        
        // Current data labels
        nameLabel = new JLabel(studentName, JLabel.CENTER);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        currentHeightLabel = new JLabel("Height: -- cm");
        currentWeightLabel = new JLabel("Weight: -- kg");
        currentBMILabel = new JLabel("BMI: --");
        ageLabel = new JLabel("Age: --");
        lastMeasurementLabel = new JLabel("Last measured: --");
        
        heightChangeLabel = new JLabel("");
        weightChangeLabel = new JLabel("");
        bmiChangeLabel = new JLabel("");
        growthTrendLabel = new JLabel("");
        
        // History table
        String[] columnNames = {"Date", "Height (cm)", "Weight (kg)", "BMI", "Age", "Teacher", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Buttons (only for teachers)
        if (isTeacherView) {
            addRecordButton = new JButton("Add Measurement");
            editRecordButton = new JButton("Edit Selected");
            deleteRecordButton = new JButton("Delete Selected");
            
            editRecordButton.setEnabled(false);
            deleteRecordButton.setEnabled(false);
        }
    }
    
    private void layoutComponents() {
        // Current data panel
        JPanel currentDataPanel = new JPanel(new GridBagLayout());
        currentDataPanel.setBorder(BorderFactory.createTitledBorder("Current Data"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Student name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        currentDataPanel.add(nameLabel, gbc);
        
        // Current measurements
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 1;
        currentDataPanel.add(currentHeightLabel, gbc);
        gbc.gridx = 1;
        currentDataPanel.add(heightChangeLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        currentDataPanel.add(currentWeightLabel, gbc);
        gbc.gridx = 1;
        currentDataPanel.add(weightChangeLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        currentDataPanel.add(currentBMILabel, gbc);
        gbc.gridx = 1;
        currentDataPanel.add(bmiChangeLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        currentDataPanel.add(ageLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        currentDataPanel.add(lastMeasurementLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        currentDataPanel.add(growthTrendLabel, gbc);
        
        add(currentDataPanel, BorderLayout.NORTH);
        
        // History table
        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Measurement History"));
        tableScrollPane.setPreferredSize(new Dimension(0, 200));
        add(tableScrollPane, BorderLayout.CENTER);
        
        // Button panel (only for teachers)
        if (isTeacherView) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(addRecordButton);
            buttonPanel.add(editRecordButton);
            buttonPanel.add(deleteRecordButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }
    
    private void loadData() {
        // Load current data
        PhysicalDevelopmentRecord latest = physicalService.getLatestPhysicalData(studentId);
        if (latest != null) {
            updateCurrentDataDisplay(latest);
        }
        
        // Load history
        List<PhysicalDevelopmentRecord> history = physicalService.getStudentPhysicalHistory(studentId);
        updateHistoryTable(history);
        
        // Update growth trend
        if (history.size() >= 2) {
            String trend = physicalService.getGrowthTrend(history);
            growthTrendLabel.setText("Growth trend: " + trend);
        }
    }
    
    private void updateCurrentDataDisplay(PhysicalDevelopmentRecord record) {
        currentHeightLabel.setText("Height: " + df.format(record.getHeightCm()) + " cm");
        currentWeightLabel.setText("Weight: " + df.format(record.getWeightKg()) + " kg");
        currentBMILabel.setText("BMI: " + df.format(record.getBmi()));
        ageLabel.setText("Age: " + record.getAgeDisplay());
        lastMeasurementLabel.setText("Last measured: " + record.getMeasurementDate().format(dateFormatter));
        
        // Show changes from previous measurement
        BigDecimal heightChange = record.getHeightChange();
        BigDecimal weightChange = record.getWeightChange();
        BigDecimal bmiChange = record.getBmiChange();
        
        if (heightChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (heightChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(heightChange);
            heightChangeLabel.setText("(" + changeText + " cm)");
            heightChangeLabel.setForeground(heightChange.compareTo(BigDecimal.ZERO) > 0 ? Color.GREEN : Color.RED);
        }
        
        if (weightChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (weightChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(weightChange);
            weightChangeLabel.setText("(" + changeText + " kg)");
            weightChangeLabel.setForeground(weightChange.compareTo(BigDecimal.ZERO) > 0 ? Color.GREEN : Color.RED);
        }
        
        if (bmiChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (bmiChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(bmiChange);
            bmiChangeLabel.setText("(" + changeText + ")");
            bmiChangeLabel.setForeground(bmiChange.compareTo(BigDecimal.ZERO) > 0 ? Color.ORANGE : Color.BLUE);
        }
    }
    
    private void updateHistoryTable(List<PhysicalDevelopmentRecord> records) {
        tableModel.setRowCount(0);
        
        for (PhysicalDevelopmentRecord record : records) {
            Object[] row = {
                record.getMeasurementDate().format(dateFormatter),
                df.format(record.getHeightCm()),
                df.format(record.getWeightKg()),
                df.format(record.getBmi()),
                record.getAgeDisplay(),
                record.getRecordedByTeacher() != null ? record.getRecordedByTeacher() : "Unknown",
                record.getNotes() != null ? record.getNotes() : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void setupTeacherActions() {
        // Table selection listener
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = historyTable.getSelectedRow() != -1;
            editRecordButton.setEnabled(hasSelection);
            deleteRecordButton.setEnabled(hasSelection);
        });
        
        // Add record button
        addRecordButton.addActionListener(e -> showAddRecordDialog());
        
        // Edit record button
        editRecordButton.addActionListener(e -> showEditRecordDialog());
        
        // Delete record button
        deleteRecordButton.addActionListener(e -> deleteSelectedRecord());
    }
    
    private void showAddRecordDialog() {
        PhysicalDevelopmentRecordDialog dialog = new PhysicalDevelopmentRecordDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Add Physical Measurement",
            studentId,
            studentName,
            teacherId,
            null
        );
        
        dialog.setVisible(true);
        
        if (dialog.isRecordSaved()) {
            loadData(); // Refresh the display
        }
    }
    
    private void showEditRecordDialog() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        // Get the record data from the selected row
        // Note: This is simplified - in a real implementation, you'd want to get the actual record object
        JOptionPane.showMessageDialog(this, 
            "Edit functionality would be implemented here.\nSelected row: " + selectedRow,
            "Edit Record", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedRecord() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this measurement record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            // Delete logic would be implemented here
            JOptionPane.showMessageDialog(this, 
                "Delete functionality would be implemented here.\nSelected row: " + selectedRow,
                "Delete Record", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void refreshData() {
        loadData();
    }
}
