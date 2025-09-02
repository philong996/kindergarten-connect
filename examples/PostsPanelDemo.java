import ui.panels.PostsPanelNew;
import service.AuthService;
import javax.swing.*;
import java.awt.*;

/**
 * Demo application to showcase the new card-based posts panel
 */
public class PostsPanelDemo {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private static void createAndShowGUI() {
        // Create main frame
        JFrame frame = new JFrame("Enhanced Posts Panel Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        
        // Create demo auth service (mock)
        AuthService authService = createMockAuthService();
        
        // Create tabbed pane for different user roles
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Principal view
        PostsPanelNew principalPanel = new PostsPanelNew(1, "PRINCIPAL", authService);
        tabbedPane.addTab("Principal View", principalPanel);
        
        // Teacher view
        PostsPanelNew teacherPanel = new PostsPanelNew(2, "TEACHER", authService);
        tabbedPane.addTab("Teacher View", teacherPanel);
        
        // Parent view
        PostsPanelNew parentPanel = new PostsPanelNew(3, "PARENT", authService);
        tabbedPane.addTab("Parent View", parentPanel);
        
        frame.add(tabbedPane);
        
        // Show welcome message
        showWelcomeMessage();
        
        frame.setVisible(true);
    }
    
    private static AuthService createMockAuthService() {
        // This is a mock implementation for demo purposes
        // In a real application, this would be properly implemented
        return new AuthService() {
            @Override
            public service.AuthorizationService getAuthorizationService() {
                return new service.AuthorizationService() {
                    @Override
                    public int[] getAccessibleClassIds() {
                        return new int[]{1, 2}; // Mock class IDs
                    }
                    
                    @Override
                    public boolean hasPermission(String permission) {
                        return true; // Allow all for demo
                    }
                };
            }
        };
    }
    
    private static void showWelcomeMessage() {
        String message = "<html><div style='width: 400px;'>" +
                        "<h2>Enhanced Posts Panel Demo</h2>" +
                        "<p><b>New Features:</b></p>" +
                        "<ul>" +
                        "<li>📋 <b>Card-based layout</b> - Posts displayed as visual cards instead of tables</li>" +
                        "<li>🏫 <b>Two post types:</b> Class Activities and School Announcements</li>" +
                        "<li>💬 <b>Inline comments</b> - Comments displayed directly under each post</li>" +
                        "<li>🚫 <b>No moderation</b> - Comments appear immediately (no approval needed)</li>" +
                        "<li>📌 <b>Pinned posts</b> - Important announcements can be pinned to the top</li>" +
                        "<li>🏷️ <b>Categories</b> - Announcements can be categorized (Event, Holiday, Schedule, General)</li>" +
                        "<li>👀 <b>Role-based views</b> - Different interfaces for Principal, Teacher, and Parent</li>" +
                        "</ul>" +
                        "<p><b>Try switching between the tabs to see different user perspectives!</b></p>" +
                        "</div></html>";
        
        JOptionPane.showMessageDialog(null, message, "Welcome to Enhanced Posts", JOptionPane.INFORMATION_MESSAGE);
    }
}
