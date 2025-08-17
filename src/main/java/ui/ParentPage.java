package ui;

import service.AuthService;
import service.ParentService;
import model.Student;
import ui.components.HeaderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Parent Page - Main window for Parent users
 * Implements Template Method pattern via BaseAuthenticatedPage
 * Refactored to use reusable UI components
 */
public class ParentPage extends BaseAuthenticatedPage {
    private HeaderPanel headerPanel;
    private JPanel mainPanel;
    private ParentService parentService;
    
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
        
        // Create tabbed pane for parent features
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Child Development tab
        JPanel childDevPanel = createChildDevelopmentTab();
        tabbedPane.addTab("Child Development", childDevPanel);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createChildDevelopmentTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Ensure parentService is initialized
        if (parentService == null) {
            parentService = new ParentService();
        }
        
        // Get real children data for the logged-in parent
        int parentUserId = authService.getCurrentUser().getId();
        List<Student> children = parentService.getParentChildren(parentUserId);
        
        if (children.isEmpty()) {
            // Show message if no children are found
            JLabel noChildrenLabel = new JLabel("No children found for your account.", SwingConstants.CENTER);
            noChildrenLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            panel.add(noChildrenLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Instructions panel
        JPanel instructionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String instructionText = children.size() > 1 ? "Your children's physical development data:" : "Your child's physical development data:";
        instructionsPanel.add(new JLabel(instructionText));
        panel.add(instructionsPanel, BorderLayout.NORTH);
        
        // Child selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout());
        JComboBox<String> childCombo = new JComboBox<>();
        
        // Populate combo box with real children data
        for (Student child : children) {
            String displayName = child.getName() + " (Age: " + child.getAge() + ")";
            childCombo.addItem(displayName);
        }
        
        JButton viewButton = new JButton("View Development Data");
        
        selectionPanel.add(new JLabel("Child: "));
        selectionPanel.add(childCombo);
        selectionPanel.add(viewButton);
        
        // Content panel for physical development display
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Show first child's data by default
        Student firstChild = children.get(0);
        PhysicalDevelopmentPanel defaultPhysicalPanel = new PhysicalDevelopmentPanel(
            firstChild.getId(), firstChild.getName() // This constructor creates a parent view (read-only)
        );
        contentPanel.add(defaultPhysicalPanel, BorderLayout.CENTER);
        
        // Action listener for view button
        viewButton.addActionListener(e -> {
            int selectedIndex = childCombo.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < children.size()) {
                // Get the selected child
                Student selectedChild = children.get(selectedIndex);
                
                // Create and display physical development panel (parent view - read-only)
                contentPanel.removeAll();
                PhysicalDevelopmentPanel physicalPanel = new PhysicalDevelopmentPanel(
                    selectedChild.getId(), selectedChild.getName() // This constructor creates a parent view (read-only)
                );
                contentPanel.add(physicalPanel, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(panel, "Please select your child first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Only show selection panel if there are multiple children
        if (children.size() > 1) {
            panel.add(selectionPanel, BorderLayout.NORTH);
        }
        panel.add(contentPanel, BorderLayout.CENTER);
        
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
