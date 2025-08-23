package ui;

import service.ParentService;
import model.Student;
import ui.components.ButtonPanel;
import util.ProfileImageUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * My Children Overview Panel - Displays enrolled children with quick stats 
 * and child selection for context switching
 * Follows the existing UI component patterns and styling
 */
public class MyChildrenPanel extends JPanel {
    private ParentService parentService;
    private int parentUserId;
    private List<Student> children;
    private Consumer<Student> onChildSelected;
    private JPanel childrenGridPanel;
    private JPanel overviewPanel;
    private JLabel selectedChildLabel;
    private Student currentSelectedChild;
    private JPanel childDetailPanel;
    private JSplitPane splitPane;
    
    public MyChildrenPanel(int parentUserId) {
        this(parentUserId, null);
    }
    
    public MyChildrenPanel(int parentUserId, Consumer<Student> onChildSelected) {
        this.parentUserId = parentUserId;
        this.onChildSelected = onChildSelected;
        this.parentService = new ParentService();
        
        initializeComponents();
        loadChildrenData();
        setupLayout();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create overview panel
        overviewPanel = createOverviewPanel();
        
        // Create children grid panel
        childrenGridPanel = new JPanel();
        childrenGridPanel.setLayout(new GridLayout(0, 1, 10, 10)); // Changed to 1 column for left panel
        
        // Create child detail panel (right side)
        childDetailPanel = createEmptyDetailPanel();
        
        // Create selected child info panel
        selectedChildLabel = new JLabel("Select a child to view details");
        selectedChildLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        selectedChildLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Children Overview", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Placeholder labels - will be populated with real data
        JLabel totalChildrenLabel = new JLabel("Total Children: -", SwingConstants.CENTER);
        JLabel avgAttendanceLabel = new JLabel("Avg Attendance: -%", SwingConstants.CENTER);
        JLabel perfectAttendanceLabel = new JLabel("Perfect Attendance: -", SwingConstants.CENTER);
        JLabel totalRecordsLabel = new JLabel("Dev. Records: -", SwingConstants.CENTER);
        
        // Style the labels
        Font statsFont = new Font("Arial", Font.BOLD, 12);
        totalChildrenLabel.setFont(statsFont);
        avgAttendanceLabel.setFont(statsFont);
        perfectAttendanceLabel.setFont(statsFont);
        totalRecordsLabel.setFont(statsFont);
        
        statsPanel.add(totalChildrenLabel);
        statsPanel.add(avgAttendanceLabel);
        statsPanel.add(perfectAttendanceLabel);
        statsPanel.add(totalRecordsLabel);
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // Store references for updating
        panel.putClientProperty("totalChildrenLabel", totalChildrenLabel);
        panel.putClientProperty("avgAttendanceLabel", avgAttendanceLabel);
        panel.putClientProperty("perfectAttendanceLabel", perfectAttendanceLabel);
        panel.putClientProperty("totalRecordsLabel", totalRecordsLabel);
        
        return panel;
    }
    
    private JPanel createEmptyDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Child Details", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        panel.setPreferredSize(new Dimension(400, 0));
        
        JLabel placeholderLabel = new JLabel("Select a child to view detailed information", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        placeholderLabel.setForeground(Color.GRAY);
        
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadChildrenData() {
        // Load children
        children = parentService.getParentChildren(parentUserId);
        
        // Update overview statistics
        updateOverviewStats();
        
        // Populate children grid
        populateChildrenGrid();
    }
    
    private void updateOverviewStats() {
        Map<String, Object> overview = parentService.getChildrenOverview(parentUserId);
        
        JLabel totalChildrenLabel = (JLabel) overviewPanel.getClientProperty("totalChildrenLabel");
        JLabel avgAttendanceLabel = (JLabel) overviewPanel.getClientProperty("avgAttendanceLabel");
        JLabel perfectAttendanceLabel = (JLabel) overviewPanel.getClientProperty("perfectAttendanceLabel");
        JLabel totalRecordsLabel = (JLabel) overviewPanel.getClientProperty("totalRecordsLabel");
        
        totalChildrenLabel.setText("Total Children: " + overview.get("total_children"));
        avgAttendanceLabel.setText("Avg Attendance: " + overview.get("avg_attendance_rate") + "%");
        perfectAttendanceLabel.setText("Perfect Attendance: " + overview.get("children_with_perfect_attendance"));
        totalRecordsLabel.setText("Dev. Records: " + overview.get("total_development_records"));
    }
    
    private void populateChildrenGrid() {
        childrenGridPanel.removeAll();
        
        if (children.isEmpty()) {
            JLabel noChildrenLabel = new JLabel("No children found for your account.", SwingConstants.CENTER);
            noChildrenLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            childrenGridPanel.add(noChildrenLabel);
            return;
        }
        
        for (Student child : children) {
            JPanel childCard = createChildCard(child);
            childrenGridPanel.add(childCard);
        }
        
        // Select first child by default if callback is provided
        if (!children.isEmpty() && onChildSelected != null) {
            selectChild(children.get(0));
        }
    }
    
    private JPanel createChildCard(Student child) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 100)); // Fixed height, flexible width
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Left side - Profile image thumbnail
        byte[] imageData = child.getProfileImage();
        ImageIcon thumbnail = ProfileImageUtil.loadProfileImageFromBytes(imageData, 80, 80);
        JLabel imageLabel = new JLabel(thumbnail);
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        imageLabel.setPreferredSize(new Dimension(80, 80));
        
        // Center - Child info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 10, 2, 5);
        
        // Child name
        JLabel nameLabel = new JLabel(child.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(33, 37, 41));
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(nameLabel, gbc);
        
        // Age
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Age: " + child.getAge()), gbc);
        
        // Get child statistics
        Map<String, Object> stats = parentService.getChildQuickStats(child.getId());
        
        // Attendance rate
        gbc.gridy = 2;
        double attendanceRate = (Double) stats.get("attendance_rate");
        JLabel attendanceLabel = new JLabel("Attendance: " + attendanceRate + "%");
        if (attendanceRate >= 95.0) {
            attendanceLabel.setForeground(new Color(40, 167, 69)); // Green
        } else if (attendanceRate >= 80.0) {
            attendanceLabel.setForeground(new Color(255, 193, 7)); // Yellow
        } else {
            attendanceLabel.setForeground(new Color(220, 53, 69)); // Red
        }
        infoPanel.add(attendanceLabel, gbc);
        
        // Development records
        gbc.gridy = 3;
        infoPanel.add(new JLabel("Dev. Records: " + stats.get("development_records")), gbc);
        
        card.add(imageLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Right side - Button panel
        ButtonPanel buttonPanel = new ButtonPanel(FlowLayout.CENTER);
        buttonPanel.addStyledButton("Select", 
            e -> selectChild(child), 
            ButtonPanel.ButtonStyle.PRIMARY);
        
        card.add(buttonPanel, BorderLayout.EAST);
        
        // Add mouse click listener for the entire card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectChild(child);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.repaint();
            }
        });
        
        return card;
    }
    
    private void selectChild(Student child) {
        currentSelectedChild = child;
        selectedChildLabel.setText("Selected: " + child.getName() + " (Age: " + child.getAge() + ")");
        selectedChildLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectedChildLabel.setForeground(new Color(0, 123, 255));
        
        // Automatically show the child profile in the right panel
        showChildProfile(child);
        
        if (onChildSelected != null) {
            onChildSelected.accept(child);
        }
    }
    
    private void showChildProfile(Student child) {
        Map<String, Object> profile = parentService.getChildProfile(parentUserId, child.getId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this, 
                "Unable to load profile for " + child.getName(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update the right panel with child profile instead of showing dialog
        updateChildDetailPanel(profile);
    }
    
    private void updateChildDetailPanel(Map<String, Object> profile) {
        // Clear existing content
        childDetailPanel.removeAll();
        
        // Create new profile panel with image support
        JPanel profilePanel = createChildProfilePanelWithImage(profile);
        
        // Add scroll pane for the profile
        JScrollPane scrollPane = new JScrollPane(profilePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        // Update the title
        String childName = (String) profile.get("name");
        childDetailPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Child Details - " + childName, 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        childDetailPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh the panel
        childDetailPanel.revalidate();
        childDetailPanel.repaint();
    }
    
    private JPanel createChildProfilePanelWithImage(Map<String, Object> profile) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create image panel (top)
        JPanel imagePanel = createImagePanel(profile);
        panel.add(imagePanel, BorderLayout.NORTH);
        
        // Create info panel (center)
        JPanel infoPanel = createProfileInfoPanel(profile);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createImagePanel(Map<String, Object> profile) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Profile image display
        byte[] imageData = (byte[]) profile.get("profile_image");
        ImageIcon profileImage = ProfileImageUtil.loadProfileImageFromBytes(imageData, 150, 150);
        JLabel imageLabel = new JLabel(profileImage);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Image upload button
        JButton uploadButton = new JButton("Change Photo");
        uploadButton.setPreferredSize(new Dimension(120, 30));
        uploadButton.addActionListener(e -> uploadProfileImage(profile));
        
        // Remove image button (only show if image exists)
        JButton removeButton = new JButton("Remove");
        removeButton.setPreferredSize(new Dimension(80, 30));
        removeButton.addActionListener(e -> removeProfileImage(profile));
        removeButton.setVisible(imageData != null && imageData.length > 0);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(uploadButton);
        buttonPanel.add(removeButton);
        
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void uploadProfileImage(Map<String, Object> profile) {
        File selectedFile = ProfileImageUtil.selectImageFile(this);
        if (selectedFile != null) {
            try {
                int studentId = (Integer) profile.get("id");
                
                // Convert image to binary data
                byte[] imageData = ProfileImageUtil.convertImageToByteArray(selectedFile);
                
                // Update database
                boolean success = parentService.updateChildProfileImage(parentUserId, studentId, imageData);
                
                if (success) {
                    // Update the profile data
                    profile.put("profile_image", imageData);
                    
                    // Refresh the detail panel
                    updateChildDetailPanel(profile);
                    
                    // Refresh the children list to show updated image
                    refreshData();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Profile image updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update profile image.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error processing image: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeProfileImage(Map<String, Object> profile) {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove the profile image?", 
            "Remove Profile Image", 
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            int studentId = (Integer) profile.get("id");
            
            // Update database (set to null)
            boolean success = parentService.updateChildProfileImage(parentUserId, studentId, null);
            
            if (success) {
                // Update the profile data
                profile.put("profile_image", null);
                
                // Refresh the detail panel
                updateChildDetailPanel(profile);
                
                // Refresh the children list
                refreshData();
                
                JOptionPane.showMessageDialog(this, 
                    "Profile image removed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to remove profile image.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel createProfileInfoPanel(Map<String, Object> profile) {
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
        addProfileSection(panel, gbc, row++, "Basic Information", null);
        row = addProfileField(panel, gbc, row, "Name:", (String) profile.get("name"));
        row = addProfileField(panel, gbc, row, "Age:", profile.get("age").toString());
        row = addProfileField(panel, gbc, row, "Date of Birth:", profile.get("dob").toString());
        
        if (profile.get("class_name") != null) {
            row = addProfileField(panel, gbc, row, "Class:", (String) profile.get("class_name"));
        }
        
        if (profile.get("address") != null) {
            row = addProfileField(panel, gbc, row, "Address:", (String) profile.get("address"));
        }
        
        // Attendance Statistics
        row++;
        addProfileSection(panel, gbc, row++, "Attendance Statistics", null);
        row = addProfileField(panel, gbc, row, "Attendance Rate:", profile.get("attendance_rate") + "%");
        row = addProfileField(panel, gbc, row, "Total Days:", profile.get("total_days").toString());
        row = addProfileField(panel, gbc, row, "Present Days:", profile.get("present_days").toString());
        row = addProfileField(panel, gbc, row, "Absent Days:", profile.get("absent_days").toString());
        row = addProfileField(panel, gbc, row, "Late Days:", profile.get("late_days").toString());
        
        // Development Records
        row++;
        addProfileSection(panel, gbc, row++, "Development Records", null);
        row = addProfileField(panel, gbc, row, "Total Records:", profile.get("development_records").toString());
        
        return panel;
    }
    
    private void addProfileSection(JPanel panel, GridBagConstraints gbc, int row, String title, String subtitle) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(sectionLabel, gbc);
        
        if (subtitle != null) {
            gbc.gridy = row + 1;
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            panel.add(subtitleLabel, gbc);
        }
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
    
    private void setupLayout() {
        // Header with selected child info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerPanel.add(selectedChildLabel, BorderLayout.CENTER);
        
        // Left panel - Overview and Children List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(overviewPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(childrenGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "My Children", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create split pane with left and right panels
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, childDetailPanel);
        splitPane.setDividerLocation(500); // Set initial divider position
        splitPane.setResizeWeight(0.6); // Give 60% to left panel, 40% to right panel
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        
        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        // Refresh button panel
        ButtonPanel refreshPanel = new ButtonPanel(FlowLayout.RIGHT);
        refreshPanel.addButton("Refresh Data", e -> refreshData());
        add(refreshPanel, BorderLayout.SOUTH);
    }
    
    private void refreshData() {
        loadChildrenData();
        revalidate();
        repaint();
        
        // Show confirmation
        JOptionPane.showMessageDialog(this, 
            "Data refreshed successfully!", 
            "Refresh Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Get the currently selected child
     */
    public Student getSelectedChild() {
        return currentSelectedChild;
    }
    
    /**
     * Set the child selection callback
     */
    public void setOnChildSelected(Consumer<Student> onChildSelected) {
        this.onChildSelected = onChildSelected;
    }
    
    /**
     * Get all children for this parent
     */
    public List<Student> getChildren() {
        return children;
    }
}
