package ui.panels;

import model.PhysicalDevelopmentRecord;
import service.PhysicalDevelopmentService;
import ui.components.AppColor;
import ui.components.CustomButton;
import ui.components.RoundedBorder;
import ui.components.CustomButton.accountType;
import ui.components.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

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
    private String className;
    private boolean isBoy;
    private int teacherId; // For teacher operations
    private boolean isTeacherView;
    
    // Components
    private JLabel nameLabel;
    private JLabel sexLabel;
    private JLabel currentHeightLabel;
    private JLabel currentWeightLabel;
    private JLabel currentBMILabel;
    private JLabel ageLabel;
    private JLabel lastMeasurementLabel;
    private JLabel heightChangeLabel;
    private JLabel weightChangeLabel;
    private JLabel bmiChangeLabel;
    private JLabel growthTrendLabel;
    private JLabel classLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton addRecordButton;
    private JButton editRecordButton;
    private JButton deleteRecordButton;
    
    private DecimalFormat df = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // Color scheme
    private Color backgroundColor;
    private Color labelColor;
    
    public PhysicalDevelopmentPanel(int studentId, String studentName, String className, boolean isBoy, int teacherId, boolean isTeacherView) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.isBoy = isBoy;
        this.teacherId = teacherId;
        this.isTeacherView = isTeacherView;
        this.className = className;
        this.physicalService = new PhysicalDevelopmentService();
        if (isTeacherView) {
            this.backgroundColor = AppColor.getColor("softViolet");
            this.labelColor = AppColor.getColor("darkViolet");
        } else {
            this.backgroundColor = AppColor.getColor("lightGraylishYellow");
            this.labelColor = AppColor.getColor("darkGreen");
        }
        initializeComponents();
        layoutComponents();
        loadData();
        
        if (isTeacherView) {
            setupTeacherActions();
        }
    }
    
    // Constructor for parent view (no teacher actions)
    public PhysicalDevelopmentPanel(int studentId, String studentName, String className, boolean isBoy) {
        this(studentId, studentName, className, isBoy,  -1, false);
    }

    // public PhysicalDevelopmentPanel(int studentId, String studentName, String className, boolean isBoy) {
    //     this(studentId, studentName, className, isBoy, -1, false);
    // }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Physical Development Tracking"));
        setOpaque(false);
        
        // Current data labels
        nameLabel = new JLabel(studentName.toUpperCase(), JLabel.CENTER);
        // nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));

        String sexText = "Boy";
        if (!isBoy) {
            sexText = "Girl";
        }
        sexLabel = new JLabel(sexText, JLabel.CENTER);
        sexLabel.setFont(sexLabel.getFont().deriveFont(Font.BOLD, 16f));

        if (isBoy) {
            sexLabel.setForeground(AppColor.getColor("blue")); // Blue    
        } else {
            sexLabel.setForeground(AppColor.getColor("pink")); // pink
        }
        ageLabel = new JLabel("", JLabel.CENTER);
        ageLabel.setFont(ageLabel.getFont().deriveFont(Font.BOLD, 16f));

        currentHeightLabel = new JLabel("Height: -- cm");
        currentWeightLabel = new JLabel("Weight: -- kg");
        currentBMILabel = new JLabel("BMI: --");
        
        lastMeasurementLabel = new JLabel("Last measured: --");
        
        heightChangeLabel = new JLabel("");
        weightChangeLabel = new JLabel("");
        bmiChangeLabel = new JLabel("");
        growthTrendLabel = new JLabel("");
        classLabel = new JLabel("Class: " + className);
        
        // History table
        String[] columnNames = {"Date", "Height (cm)", "Weight (kg)", "BMI", "Age", "Teacher", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(30);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setOpaque(false);
        historyTable.setBackground(new Color(0, 0, 0, 0)); // trong suốt
        historyTable.setShowGrid(false); 
        historyTable.getColumnModel().getColumn(6).setCellRenderer(new MultiLineCellRenderer()); // notes column    
        JTableHeader header = historyTable.getTableHeader();
            header.setDefaultRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    JLabel label = new JLabel(value.toString(), SwingConstants.CENTER);
                    label.setOpaque(true);
                    label.setBackground(backgroundColor); // màu nền header
                    label.setBorder(BorderFactory.createLineBorder(labelColor, 2, true)); // bo tròn
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    return label;
                }
            });;
        // historyTable.setBackground(Color.GREEN);

        
        // Buttons (only for teachers)
        System.out.println("isTeacherView: " + isTeacherView);
        if (isTeacherView) {
            addRecordButton = new CustomButton("Add Measurement", accountType.TEACHER);
            editRecordButton = new CustomButton("Edit Selected", accountType.TEACHER);
            deleteRecordButton = new CustomButton("Delete Selected", accountType.TEACHER);
            
            editRecordButton.setEnabled(false);
            deleteRecordButton.setEnabled(false);
        }
    }
    
    private void layoutComponents() {
        JPanel currentDataPanel = new JPanel(new GridBagLayout());
        currentDataPanel.setBorder(BorderFactory.createTitledBorder("Current Data"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // Student name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        currentDataPanel.add(nameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        currentDataPanel.add(sexLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        currentDataPanel.add(ageLabel, gbc);

        gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 3;
        currentDataPanel.add(createCellWithBorder(currentHeightLabel, heightChangeLabel, 200, 80), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        currentDataPanel.add(createCellWithBorder(currentWeightLabel, weightChangeLabel, 200, 80), gbc);

        gbc.gridx = 2; gbc.gridy = 3;
        currentDataPanel.add(createCellWithBorder(currentBMILabel, bmiChangeLabel,200, 80), gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        currentDataPanel.add(lastMeasurementLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        currentDataPanel.add(growthTrendLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        currentDataPanel.add(classLabel, gbc);
        currentDataPanel.setOpaque(false);
        
        add(currentDataPanel, BorderLayout.NORTH);
        
        // History table
        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Measurement History"));
        tableScrollPane.setPreferredSize(new Dimension(0, 200));
        tableScrollPane.setOpaque(false);
        // tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.getViewport().setBackground(backgroundColor); // trong suốt
        add(tableScrollPane, BorderLayout.CENTER); 

        if (isTeacherView) {
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addRecordButton);
            buttonPanel.add(editRecordButton);
            buttonPanel.add(deleteRecordButton);
            buttonPanel.setOpaque(false);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    
    private void loadData() {
        try {
            // Load current data
            PhysicalDevelopmentRecord latest = physicalService.getLatestPhysicalData(studentId);
            if (latest != null) {
                updateCurrentDataDisplay(latest);
            } else {
                // No data available - show appropriate message
                currentHeightLabel.setText("Height: No data available");
                currentWeightLabel.setText("Weight: No data available");
                currentBMILabel.setText("BMI: No data available");
                ageLabel.setText("Age: --");
                lastMeasurementLabel.setText("Last measured: No records found");
            }
            
            // Load history
            List<PhysicalDevelopmentRecord> history = physicalService.getStudentPhysicalHistory(studentId);
            updateHistoryTable(history);
            
            // Update growth trend
            if (history.size() >= 2) {
                String trend = physicalService.getGrowthTrend(history);
                growthTrendLabel.setText("Growth trend: " + trend);
            } else {
                growthTrendLabel.setText("Growth trend: Insufficient data for analysis");
            }
        } catch (Exception e) {
            // Handle any errors during data loading
            System.err.println("Error loading physical development data for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
            
            currentHeightLabel.setText("Height: Error loading data");
            currentWeightLabel.setText("Weight: Error loading data");
            currentBMILabel.setText("BMI: Error loading data");
            lastMeasurementLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void updateCurrentDataDisplay(PhysicalDevelopmentRecord record) {
        currentHeightLabel.setText("Height: " + df.format(record.getHeightCm()) + " cm");
        currentWeightLabel.setText("Weight: " + df.format(record.getWeightKg()) + " kg");
        currentBMILabel.setText("BMI: " + df.format(record.getBmi()));
        ageLabel.setText(record.getAgeDisplay());
        lastMeasurementLabel.setText("Last measured: " + record.getMeasurementDate().format(dateFormatter));
        
        // Show changes from previous measurement
        BigDecimal heightChange = record.getHeightChange();
        BigDecimal weightChange = record.getWeightChange();
        BigDecimal bmiChange = record.getBmiChange();
        
        if (heightChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (heightChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(heightChange);
            heightChangeLabel.setText("(" + changeText + " cm)");
            heightChangeLabel.setForeground(heightChange.compareTo(BigDecimal.ZERO) > 0 ? AppColor.getColor("freshGreen") : AppColor.getColor("coralRed"));
        }
        
        if (weightChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (weightChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(weightChange);
            weightChangeLabel.setText("(" + changeText + " kg)");
            weightChangeLabel.setForeground(weightChange.compareTo(BigDecimal.ZERO) > 0 ? AppColor.getColor("freshGreen") : AppColor.getColor("coralRed"));
        }
        
        if (bmiChange.compareTo(BigDecimal.ZERO) != 0) {
            String changeText = (bmiChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + df.format(bmiChange);
            bmiChangeLabel.setText("(" + changeText + ")");
            bmiChangeLabel.setForeground(bmiChange.compareTo(BigDecimal.ZERO) > 0 ? AppColor.getColor("freshGreen") : AppColor.getColor("coralRed"));
        }
    }
    
    private void updateHistoryTable(List<PhysicalDevelopmentRecord> records) {
        tableModel.setRowCount(0);
        
        // System.out.println("Loading " + records.size() + " physical development records for student " + studentId);
        
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
        
        if (records.isEmpty()) {
            // Add a placeholder row to indicate no data
            Object[] emptyRow = {"No records", "found", "", "", "", "", ""};
            tableModel.addRow(emptyRow);
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

    private JPanel createCellWithBorder(JLabel topLabel, JLabel bottomLabel, int width, int height) {
        JPanel cellPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // 2 dòng
        cellPanel.setPreferredSize(new Dimension(width, height));
        cellPanel.setOpaque(false);
        cellPanel.add(topLabel);
        cellPanel.add(bottomLabel);
        cellPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, labelColor, 3),            
            BorderFactory.createEmptyBorder(10, 10, 10, 10) 
        ));
        return cellPanel;
    }
}
