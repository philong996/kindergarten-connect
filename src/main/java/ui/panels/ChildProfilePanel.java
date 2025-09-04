package ui.panels;

import service.ParentService;
import ui.components.AppColor;
import ui.components.CustomButton;
import ui.components.CustomMessageDialog;
import ui.components.RoundedBorder;
import model.Student;
import util.ProfileImageUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
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
        setOpaque(false);
    }
    
    /**
     * Update the panel with new child data
     */
    public void updateChild(Student child) {
        this.currentChild = child;
        this.currentProfile = parentService.getChildProfile(parentUserId, child.getId());
        
        removeAll();
        
        if (currentProfile != null) {
            JPanel imagePanel = createImageDisplayPanel();
            JPanel infoPanel = createProfileInfoPanel();
            // imagePanel.setBackground(AppColor.getColor("yellowOrange"));
            imagePanel.setOpaque(false);
            infoPanel.setOpaque(false);

            JScrollPane scrollPane = new JScrollPane(infoPanel);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setOpaque(false);

            // Dùng JSplitPane để chia 40/60
            JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                imagePanel,
                scrollPane
            );
            splitPane.setResizeWeight(0.3); 
            splitPane.setDividerSize(0);    
            splitPane.setBorder(null);      
            splitPane.setOpaque(false);
            add(splitPane, BorderLayout.CENTER);
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
        ImageIcon profileImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 300, 300);

        imageLabel = new JLabel(profileImage);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        imageLabel.setBorder(new RoundedBorder(10, AppColor.getColor("greenBlue"), 1));
        
        // Create button panel for image actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        // CustomButton changeImageButton = new CustomButton("Change Profile Image");
        CustomButton changeImageButton = new CustomButton("Change Profile Image");
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
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD,24f));
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
        panel.setOpaque(false);
        return panel;
    }
    
    private void addProfileSection(JPanel panel, GridBagConstraints gbc, int row, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD, 18f));
        sectionLabel.setForeground(AppColor.getColor("brown"));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
    }
    
    private int addProfileField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(fieldLabel, gbc);
        
        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value != null ? value : "N/A");
        fieldLabel.setFont(fieldLabel.getFont().deriveFont( 12f));
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
                        CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(ChildProfilePanel.this), 
                            "Image Too Large", "Image file is too large. Please select an image smaller than 5MB.", 
                            CustomMessageDialog.Type.ERROR);

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
                        
                        CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(ChildProfilePanel.this), 
                            "Success", "Profile image updated successfully!", 
                            CustomMessageDialog.Type.SUCCESS);
                    } else {
                        CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(ChildProfilePanel.this), 
                            "Error", "Failed to update profile image. Please try again.", 
                            CustomMessageDialog.Type.ERROR);}
                    
                } catch (IOException ex) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(ChildProfilePanel.this), 
                        "Error", "Error reading image file: " + ex.getMessage(), 
                        CustomMessageDialog.Type.ERROR);
                } catch (Exception ex) {
                    CustomMessageDialog.showMessage((JFrame) SwingUtilities.getWindowAncestor(ChildProfilePanel.this), 
                        "Error", "An unexpected error occurred: " + ex.getMessage(), 
                        CustomMessageDialog.Type.ERROR);
                }
            }
        }
    }
}
