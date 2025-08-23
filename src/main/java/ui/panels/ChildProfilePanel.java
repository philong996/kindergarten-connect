package ui.panels;

import service.ParentService;
import model.Student;
import util.ProfileImageUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * Panel for displaying and editing child profile information
 * Extracted from ParentPage for better code organization
 */
public class ChildProfilePanel extends JPanel {
    private final ParentService parentService;
    private final int parentUserId;
    private Student currentChild;
    private Map<String, Object> currentProfile;
    private JLabel imageLabel;
    private JButton changeImageButton;
    
    public ChildProfilePanel(ParentService parentService, int parentUserId) {
        this.parentService = parentService;
        this.parentUserId = parentUserId;
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
    /**
     * Update the panel with new child data
     */
    public void updateChild(Student child) {
        this.currentChild = child;
        this.currentProfile = parentService.getChildProfile(parentUserId, child.getId());
        
        removeAll();
        
        if (currentProfile != null) {
            // Create image panel (top)
            JPanel imagePanel = createImageDisplayPanel();
            add(imagePanel, BorderLayout.NORTH);
            
            // Create info panel (center)
            JPanel infoPanel = createProfileInfoPanel();
            add(infoPanel, BorderLayout.CENTER);
        } else {
            JLabel errorLabel = new JLabel("Unable to load profile for " + child.getName(), SwingConstants.CENTER);
            add(errorLabel, BorderLayout.CENTER);
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createImageDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Profile image display
        byte[] imageData = (byte[]) currentProfile.get("profile_image");
        ImageIcon profileImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 150, 150);
        imageLabel = new JLabel(profileImage);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Create button panel for image actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        changeImageButton = new JButton("Change Profile Image");
        changeImageButton.setFont(new Font("Arial", Font.PLAIN, 12));
        changeImageButton.addActionListener(new ChangeImageActionListener());
        
        buttonPanel.add(changeImageButton);
        
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Profile Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        int row = 1;
        
        // Basic Information
        addProfileSection(panel, gbc, row++, "Basic Information");
        row = addProfileField(panel, gbc, row, "Name:", (String) currentProfile.get("name"));
        row = addProfileField(panel, gbc, row, "Age:", currentProfile.get("age").toString());
        row = addProfileField(panel, gbc, row, "Date of Birth:", currentProfile.get("dob").toString());
        
        if (currentProfile.get("class_name") != null) {
            row = addProfileField(panel, gbc, row, "Class:", (String) currentProfile.get("class_name"));
        }
        
        if (currentProfile.get("address") != null) {
            row = addProfileField(panel, gbc, row, "Address:", (String) currentProfile.get("address"));
        }
        
        // Attendance Statistics
        row++;
        addProfileSection(panel, gbc, row++, "Attendance Statistics");
        row = addProfileField(panel, gbc, row, "Attendance Rate:", currentProfile.get("attendance_rate") + "%");
        row = addProfileField(panel, gbc, row, "Total Days:", currentProfile.get("total_days").toString());
        row = addProfileField(panel, gbc, row, "Present Days:", currentProfile.get("present_days").toString());
        row = addProfileField(panel, gbc, row, "Absent Days:", currentProfile.get("absent_days").toString());
        row = addProfileField(panel, gbc, row, "Late Days:", currentProfile.get("late_days").toString());
        
        // Development Records
        row++;
        addProfileSection(panel, gbc, row++, "Development Records");
        row = addProfileField(panel, gbc, row, "Total Records:", currentProfile.get("development_records").toString());
        
        return panel;
    }
    
    private void addProfileSection(JPanel panel, GridBagConstraints gbc, int row, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
    }
    
    private int addProfileField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(fieldLabel, gbc);
        
        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value != null ? value : "N/A");
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(valueLabel, gbc);
        
        return row + 1;
    }
    
    /**
     * Action listener for changing profile image
     */
    private class ChangeImageActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Profile Image");
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            int result = fileChooser.showOpenDialog(ChildProfilePanel.this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                    
                    // Validate image size (limit to 5MB)
                    if (imageData.length > 5 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(ChildProfilePanel.this,
                            "Image file is too large. Please select an image smaller than 5MB.",
                            "Image Too Large", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Update profile image in database
                    boolean success = parentService.updateChildProfileImage(
                        parentUserId, currentChild.getId(), imageData);
                    
                    if (success) {
                        // Update the display
                        ImageIcon newImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 150, 150);
                        imageLabel.setIcon(newImage);
                        
                        // Update current profile data
                        currentProfile.put("profile_image", imageData);
                        
                        JOptionPane.showMessageDialog(ChildProfilePanel.this,
                            "Profile image updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ChildProfilePanel.this,
                            "Failed to update profile image. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ChildProfilePanel.this,
                        "Error reading image file: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChildProfilePanel.this,
                        "An unexpected error occurred: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
