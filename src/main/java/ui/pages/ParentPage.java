package ui.pages;

import service.AuthService;
import service.ParentService;
import model.Student;
import ui.components.AppColor;
import ui.components.HeaderPanel;
import ui.panels.PhysicalDevelopmentPanel;
import ui.panels.AvatarSelector;
import ui.panels.ChildProfilePanel;
import ui.panels.PostsPanel;
import ui.panels.ChatPanel;
import ui.panels.AttendanceHistoryPanel;
import util.ProfileImageUtil;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Parent Page - Main window for Parent users
 * Implements Template Method pattern via BaseAuthenticatedPage
 * Refactored to use reusable UI components
 */
public class ParentPage extends BaseAuthenticatedPage {
    private HeaderPanel headerPanel;
    private JPanel mainPanel;
    private ParentService parentService;
    private JPanel childDevPanel;
    private JPanel attendanceHistoryPanel;
    private JComboBox<String> childCombo;
    private List<Student> children;
    private ChildProfilePanel childProfilePanel;
    private AvatarSelector avatarSelector;   
    public ParentPage(AuthService authService) {
        super(authService);
        this.parentService = new ParentService();
    }
    
    @Override
    protected String getPageTitle() {
        return "Kindergarten Management System - Parent Page";
    }
    
    @Override
    protected void initializeComponents() {
        // Initialize service if not already done
        if (parentService == null) {
            parentService = new ParentService();
        }

        headerPanel = HeaderPanel.createDashboard("Parent", authService.getCurrentUser().getUsername());
        
        // Get children data
        int parentUserId = authService.getCurrentUser().getId();
        children = parentService.getParentChildren(parentUserId);
        
        // Create child selection panel at the top (now includes overview)
        JPanel childSelectionPanel = createChildSelectionPanel();
        
        // Create tabbed pane for parent features
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Initialize mainPanel first before using it
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AppColor.getColor("culture"));
         
        // Child Development tab
        childDevPanel = createChildDevelopmentTab();
        
        // Attendance History tab
        attendanceHistoryPanel = createAttendanceHistoryTab();
        
        // Create child profile panel
        childProfilePanel = new ChildProfilePanel(parentService, parentUserId);
        
        // Create selected child detail panel container
        JPanel detailPanel = new JPanel(new BorderLayout());
        // detailPanel.setOpaque(false);
        
        JLabel instructionLabel = new JLabel("Select a child from the dropdown above to view details", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        instructionLabel.setForeground(Color.GRAY);
        // detailPanel.add(instructionLabel, BorderLayout.CENTER);
        
        // Store reference for updates (mainPanel is now initialized)
        mainPanel.putClientProperty("detailPanel", detailPanel);
        
        // Add tabs with Child Profile first (default selected)
        tabbedPane.addTab("Child Profile", detailPanel);
        tabbedPane.addTab("Attendance History", attendanceHistoryPanel);

        tabbedPane.addTab("Class Posts", createPostsTab());
        tabbedPane.addTab("Messages", createMessagesTab());
        tabbedPane.addTab("Child Development", childDevPanel);
        tabbedPane.addTab("Child Profile", detailPanel);
        tabbedPane.setForeground(AppColor.getColor("darkGreen"));
        
        // Set Child Profile as the default selected tab
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setBackground(AppColor.getColor("greenBlue"));
        
        mainPanel.add(childSelectionPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Initialize with first child if available
        avatarSelector = new AvatarSelector(children);
        avatarSelector.setOnAvatarChange(this::updateChildProfileDetails);
        Student defaultStudent = avatarSelector.getSelectedStudent();
        if (defaultStudent != null) {
            updateChildProfileDetails(defaultStudent);
            updateChildDevelopmentPanel(defaultStudent);
            updateAttendanceHistoryPanel(defaultStudent);
        }
    }
    
    private JPanel createChildSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Children Overview"));
        
        if (children.isEmpty()) {
            JLabel noChildrenLabel = new JLabel("No children found for your account.");
            panel.add(noChildrenLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Create overview statistics panel at the top
        // JPanel statsPanel = createOverviewStatsPanel();
        // panel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setOpaque(false);
        selectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Select Child"));
        
        AvatarSelector avatarSelector = new AvatarSelector(children);
        avatarSelector.setToolTipText("Click to select a different child");
        
        avatarSelector.setOnAvatarChange(selectedStudent -> {
            updateChildProfileDetails(selectedStudent);
            updateChildDevelopmentPanel(selectedStudent);
            updateAttendanceHistoryPanel(selectedStudent);
        });
        // selectionPanel.add(avatarSelector, BorderLayout.WEST);
        panel.add(avatarSelector, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createOverviewStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Children Overview Statistics", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create stats labels
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
        
        statsGrid.add(totalChildrenLabel);
        statsGrid.add(avgAttendanceLabel);
        statsGrid.add(perfectAttendanceLabel);
        statsGrid.add(totalRecordsLabel);
        
        // panel.add(statsGrid, BorderLayout.CENTER);
        
        // Store references for updating
        panel.putClientProperty("totalChildrenLabel", totalChildrenLabel);
        panel.putClientProperty("avgAttendanceLabel", avgAttendanceLabel);
        panel.putClientProperty("perfectAttendanceLabel", perfectAttendanceLabel);
        panel.putClientProperty("totalRecordsLabel", totalRecordsLabel);
        
        // Load and display overview stats
        updateOverviewStats(panel);
        
        return panel;
    }
    
    private void updateOverviewStats(JPanel statsPanel) {
        if (children.isEmpty()) return;
        
        int parentUserId = authService.getCurrentUser().getId();
        Map<String, Object> overview = parentService.getChildrenOverview(parentUserId);
        
        JLabel totalChildrenLabel = (JLabel) statsPanel.getClientProperty("totalChildrenLabel");
        JLabel avgAttendanceLabel = (JLabel) statsPanel.getClientProperty("avgAttendanceLabel");
        JLabel perfectAttendanceLabel = (JLabel) statsPanel.getClientProperty("perfectAttendanceLabel");
        JLabel totalRecordsLabel = (JLabel) statsPanel.getClientProperty("totalRecordsLabel");
        
        if (totalChildrenLabel != null) {
            totalChildrenLabel.setText("Total Children: " + overview.get("total_children"));
            avgAttendanceLabel.setText("Avg Attendance: " + overview.get("avg_attendance_rate") + "%");
            perfectAttendanceLabel.setText("Perfect Attendance: " + overview.get("children_with_perfect_attendance"));
            totalRecordsLabel.setText("Dev. Records: " + overview.get("total_development_records"));
        }
    }
    
    // private void onChildSelectionChanged() {
    //     if (childCombo == null || children.isEmpty()) return;
        
    //     int selectedIndex = childCombo.getSelectedIndex();
    //     if (selectedIndex < 0 || selectedIndex >= children.size()) return;
        
    //     Student selectedChild = children.get(selectedIndex);
        
    //     // Update the child profile tab with selected child details
    //     updateChildProfileDetails(selectedChild);
        // Update the development tab
        // updateChildDevelopmentPanel(selectedChild);
        
        // Update the attendance history tab
        // updateAttendanceHistoryPanel(selectedChild);
    // }
    
    private void updateChildProfileDetails(Student child) {
        JPanel detailPanel = (JPanel) mainPanel.getClientProperty("detailPanel");
        // detailPanel.setBackground(AppColor.getColor("culture"));
        detailPanel.setOpaque(false);
        

        if (detailPanel != null) {
            // Clear existing content
            detailPanel.removeAll();
            
            // Update the child profile panel with the selected child
            childProfilePanel.updateChild(child);
            detailPanel.add(childProfilePanel, BorderLayout.CENTER);

            detailPanel.revalidate();
            detailPanel.repaint();
            
        }
    }
    
    private void updateChildDevelopmentPanel(Student selectedChild) {
        if (childDevPanel != null) {
            // Remove existing content
            childDevPanel.removeAll();
            
            // Create new PhysicalDevelopmentPanel for the selected child

            
            PhysicalDevelopmentPanel physicalPanel = new PhysicalDevelopmentPanel(
                selectedChild.getId(), selectedChild.getName(), true
            );
            
            // Add header info about selected child
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.setOpaque(false);
            // headerPanel.add(new JLabel("Physical development data for: " + selectedChild.getName() + " (Age: " + selectedChild.getAge() + ")"));
            headerPanel.setOpaque(false);
            childDevPanel.add(headerPanel, BorderLayout.NORTH);
            childDevPanel.add(physicalPanel, BorderLayout.CENTER);
            childDevPanel.setOpaque(false);
            childDevPanel.revalidate();
            childDevPanel.repaint();
        }
    }

    private void updateAttendanceHistoryPanel(Student selectedChild) {
        if (attendanceHistoryPanel != null) {
            // Remove existing content
            attendanceHistoryPanel.removeAll();
            
            // Create new AttendanceHistoryPanel for the selected child's class
            AttendanceHistoryPanel historyPanel = new AttendanceHistoryPanel(selectedChild.getClassId());
            
            // Set default date range to last month
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(1);
            historyPanel.setDateRange(startDate, endDate);
            
            // Pre-select the child and perform search
            historyPanel.setSelectedStudent(selectedChild.getId());
            historyPanel.performSearch();
            
            // Add header info about selected child
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.add(new JLabel("Attendance history for: " + selectedChild.getName() + 
                                     " (" + (selectedChild.getClassName() != null ? selectedChild.getClassName() : "Class ID: " + selectedChild.getClassId()) + ")"));
            
            attendanceHistoryPanel.add(headerPanel, BorderLayout.NORTH);
            attendanceHistoryPanel.add(historyPanel, BorderLayout.CENTER);
            
            attendanceHistoryPanel.revalidate();
            attendanceHistoryPanel.repaint();
        }
    }

    private JPanel createChildDevelopmentTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        if (children.isEmpty()) {
            JLabel noChildrenLabel = new JLabel("No children found for your account.", SwingConstants.CENTER);
            noChildrenLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            panel.add(noChildrenLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // This panel will be populated when a child is selected
        // Return the panel that will be updated by updateChildDevelopmentPanel
        return panel;
    }
    
    private JPanel createAttendanceHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (children.isEmpty()) {
            JLabel noChildrenLabel = new JLabel("No children found for your account.", SwingConstants.CENTER);
            noChildrenLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            panel.add(noChildrenLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // This panel will be populated when a child is selected
        // Return the panel that will be updated by updateAttendanceHistoryPanel
        return panel;
    }
    
    private JPanel createPostsTab() {
        int currentUserId = authService.getCurrentUser().getId();
        String currentUserRole = authService.getCurrentUser().getRole();
        
        PostsPanel postsPanel = new PostsPanel(currentUserId, currentUserRole, authService);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(postsPanel, BorderLayout.CENTER);
        panel.setOpaque(false);
        
        return panel;
    }
    
    private JPanel createMessagesTab() {
        int currentUserId = authService.getCurrentUser().getId();
        String currentUserRole = authService.getCurrentUser().getRole();
        
        ChatPanel chatPanel = new ChatPanel(currentUserId, currentUserRole, authService);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chatPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void setupPermissions() {
        // Parent-specific permissions setup
        headerPanel.setStatus("Parent access - View only permissions");
    }
    
    @Override
    protected void setupEventHandlers() {
        // Event handlers are already set up in the tab components
        // Menu bar event handlers are handled by the base class
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isParent();
    }
}
