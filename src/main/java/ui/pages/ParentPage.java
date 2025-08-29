package ui.pages;

import service.AuthService;
import service.ParentService;
import model.Student;
import ui.components.HeaderPanel;
import ui.panels.PhysicalDevelopmentPanel;
import ui.panels.ChildProfilePanel;
import ui.panels.PostsPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
    private JComboBox<String> childCombo;
    private List<Student> children;
    private ChildProfilePanel childProfilePanel;
    
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
        
        // Child Development tab
        childDevPanel = createChildDevelopmentTab();
        
        // Create child profile panel
        childProfilePanel = new ChildProfilePanel(parentService, parentUserId);
        
        // Create selected child detail panel container
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Selected Child Details"));
        
        JLabel instructionLabel = new JLabel("Select a child from the dropdown above to view details", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        instructionLabel.setForeground(Color.GRAY);
        detailPanel.add(instructionLabel, BorderLayout.CENTER);
        
        // Store reference for updates (mainPanel is now initialized)
        mainPanel.putClientProperty("detailPanel", detailPanel);
        
        // Add tabs with Child Profile first (default selected)
        tabbedPane.addTab("Child Profile", detailPanel);
        tabbedPane.addTab("Class Posts", createPostsTab());
        tabbedPane.addTab("Child Development", childDevPanel);
        
        // Set Child Profile as the default selected tab
        tabbedPane.setSelectedIndex(0);
        
        mainPanel.add(childSelectionPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Initialize with first child if available
        if (!children.isEmpty()) {
            childCombo.setSelectedIndex(0);
            onChildSelectionChanged();
        }
    }
    
    private JPanel createChildSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Children Overview"));
        
        if (children.isEmpty()) {
            JLabel noChildrenLabel = new JLabel("No children found for your account.");
            panel.add(noChildrenLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Create overview statistics panel at the top
        JPanel statsPanel = createOverviewStatsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Create child selection section
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Child"));
        
        JLabel label = new JLabel("Child: ");
        childCombo = new JComboBox<>();
        
        // Populate combo box
        for (Student child : children) {
            String displayName = child.getName() + " (Age: " + child.getAge() + ")";
            childCombo.addItem(displayName);
        }
        
        // Add selection listener
        childCombo.addActionListener(e -> onChildSelectionChanged());
        
        selectionPanel.add(label);
        selectionPanel.add(childCombo);
        
        panel.add(selectionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOverviewStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Children Overview Statistics", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 10, 0));
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
        
        panel.add(statsGrid, BorderLayout.CENTER);
        
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
    
    private void onChildSelectionChanged() {
        if (childCombo == null || children.isEmpty()) return;
        
        int selectedIndex = childCombo.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= children.size()) return;
        
        Student selectedChild = children.get(selectedIndex);
        
        // Update the child profile tab with selected child details
        updateChildProfileDetails(selectedChild);
        
        // Update the development tab
        updateChildDevelopmentPanel(selectedChild);
    }
    
    private void updateChildProfileDetails(Student child) {
        // Find the detail panel stored in mainPanel
        JPanel detailPanel = (JPanel) mainPanel.getClientProperty("detailPanel");
        
        if (detailPanel != null) {
            // Clear existing content
            detailPanel.removeAll();
            
            // Update the child profile panel with the selected child
            childProfilePanel.updateChild(child);
            
            // Add the child profile panel to a scroll pane
            JScrollPane scrollPane = new JScrollPane(childProfilePanel);
            scrollPane.setBorder(null);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            detailPanel.add(scrollPane, BorderLayout.CENTER);
            
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
                selectedChild.getId(), selectedChild.getName()
            );
            
            // Add header info about selected child
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.add(new JLabel("Physical development data for: " + selectedChild.getName() + " (Age: " + selectedChild.getAge() + ")"));
            
            childDevPanel.add(headerPanel, BorderLayout.NORTH);
            childDevPanel.add(physicalPanel, BorderLayout.CENTER);
            
            childDevPanel.revalidate();
            childDevPanel.repaint();
        }
    }

    private JPanel createChildDevelopmentTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
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
    
    private JPanel createPostsTab() {
        int currentUserId = authService.getCurrentUser().getId();
        String currentUserRole = authService.getCurrentUser().getRole();
        
        PostsPanel postsPanel = new PostsPanel(currentUserId, currentUserRole);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(postsPanel, BorderLayout.CENTER);
        
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
