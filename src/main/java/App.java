import util.DatabaseInitializer;

import java.awt.Font;

import javax.swing.*;

import ui.components.AppColor;
import ui.components.AppStyle;
import ui.pages.LoginWindow;

/**
 * Main application entry point
 * Refactored from StudentManagementApp to App.java
 */
public class App {
    
    public static void main(String[] args) {

        // Font appFont = CustomFont.getBalooFont(14f);
        Font appFont = new Font("Comic Sans MS", Font.PLAIN, 14);
        AppStyle.setUIFont(appFont);
        AppStyle.setUIForeground(AppColor.getColor("green"));

        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Check if database is already initialized, if not initialize it
        System.out.println("Checking database status...");
        DatabaseInitializer dbInitializer = new DatabaseInitializer();
        
        try {
            if (!dbInitializer.isDatabaseInitialized()) {
                System.out.println("Database not found. Initializing database...");
                dbInitializer.initializeDatabase();
                System.out.println("Database initialized successfully!");
            } else {
                System.out.println("Database already exists. Skipping initialization.");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog and exit
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to database. Please check your database configuration.\n\n" +
                "Error: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Start the Swing application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new LoginWindow().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Failed to start application: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error dialog
                    JOptionPane.showMessageDialog(null, 
                        "Failed to start application: " + e.getMessage(), 
                        "Application Error", 
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        });
    }

}
