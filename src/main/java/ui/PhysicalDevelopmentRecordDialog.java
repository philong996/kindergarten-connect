package ui;

import service.PhysicalDevelopmentService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for adding or editing physical development records
 */
public class PhysicalDevelopmentRecordDialog extends JDialog {
    private PhysicalDevelopmentService physicalService;
    private int studentId;
    private String studentName;
    private int teacherId;
    private boolean isEdit;
    private boolean recordSaved = false;
    
    // Form components
    private JTextField heightField;
    private JTextField weightField;
    private JTextField dateField;
    private JTextArea notesArea;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel bmiLabel;
    
    public PhysicalDevelopmentRecordDialog(Frame parent, String title, int studentId, 
                                         String studentName, int teacherId, Object existingRecord) {
        super(parent, title, true);
        this.studentId = studentId;
        this.studentName = studentName;
        this.teacherId = teacherId;
        this.isEdit = existingRecord != null;
        this.physicalService = new PhysicalDevelopmentService();
        
        initializeComponents();
        layoutComponents();
        setupActions();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Form fields
        heightField = new JTextField(10);
        weightField = new JTextField(10);
        dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 15);
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        bmiLabel = new JLabel("BMI: --");
        
        saveButton = new JButton(isEdit ? "Update" : "Save");
        cancelButton = new JButton("Cancel");
        
        // Add listeners for automatic BMI calculation
        heightField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateBMI();
            }
        });
        
        weightField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateBMI();
            }
        });
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Physical Measurement for " + studentName, JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Height
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1;
        formPanel.add(heightField, gbc);
        
        // Weight
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        formPanel.add(weightField, gbc);
        
        // BMI display
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("BMI:"), gbc);
        gbc.gridx = 1;
        formPanel.add(bmiLabel, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Date (yyyy-mm-dd):"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(notesArea), gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupActions() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRecord();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void calculateBMI() {
        try {
            String heightText = heightField.getText().trim();
            String weightText = weightField.getText().trim();
            
            if (!heightText.isEmpty() && !weightText.isEmpty()) {
                double height = Double.parseDouble(heightText);
                double weight = Double.parseDouble(weightText);
                
                if (height > 0 && weight > 0) {
                    double bmi = weight / Math.pow(height / 100, 2);
                    bmiLabel.setText(String.format("BMI: %.2f", bmi));
                } else {
                    bmiLabel.setText("BMI: --");
                }
            } else {
                bmiLabel.setText("BMI: --");
            }
        } catch (NumberFormatException e) {
            bmiLabel.setText("BMI: --");
        }
    }
    
    private void saveRecord() {
        try {
            // Validate input
            String heightText = heightField.getText().trim();
            String weightText = weightField.getText().trim();
            String dateText = dateField.getText().trim();
            String notes = notesArea.getText().trim();
            
            if (heightText.isEmpty() || weightText.isEmpty() || dateText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill in height, weight, and date fields.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);
            LocalDate measurementDate = LocalDate.parse(dateText);
            
            // Get current user as the teacher who recorded this
            
            // Save the record
            boolean success = physicalService.recordPhysicalData(
                studentId, height, weight, measurementDate, teacherId, 
                notes.isEmpty() ? null : notes
            );
            
            if (success) {
                recordSaved = true;
                JOptionPane.showMessageDialog(this,
                    "Physical measurement recorded successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to save the measurement record. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid numbers for height and weight.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid date in yyyy-mm-dd format.",
                "Invalid Date",
                JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "An error occurred while saving: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public boolean isRecordSaved() {
        return recordSaved;
    }
}
