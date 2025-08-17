package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable button panel component that provides consistent styling and layout
 * for action buttons throughout the application
 */
public class ButtonPanel extends JPanel {
    private Map<String, JButton> buttons;
    private Map<String, ButtonStyle> buttonStyles;
    private int alignment;
    
    public ButtonPanel() {
        this(FlowLayout.CENTER);
    }
    
    public ButtonPanel(int alignment) {
        this.alignment = alignment;
        this.buttons = new HashMap<>();
        this.buttonStyles = new HashMap<>();
        
        setLayout(new FlowLayout(alignment));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    /**
     * Add a button with text and action listener
     */
    public JButton addButton(String text, ActionListener actionListener) {
        return addButton(text, actionListener, true);
    }
    
    /**
     * Add a button with text, action listener, and enabled state
     */
    public JButton addButton(String text, ActionListener actionListener, boolean enabled) {
        JButton button = new JButton(text);
        button.setEnabled(enabled);
        button.setPreferredSize(new Dimension(120, 30));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (actionListener != null) {
            button.addActionListener(actionListener);
        }
        
        buttons.put(text.toLowerCase().replace(" ", "_"), button);
        add(button);
        
        return button;
    }
    
    /**
     * Add a styled button (primary, secondary, danger)
     */
    public JButton addStyledButton(String text, ActionListener actionListener, ButtonStyle style) {
        JButton button = addButton(text, actionListener);
        String key = text.toLowerCase().replace(" ", "_");
        buttonStyles.put(key, style);
        applyButtonStyle(button, style);
        return button;
    }
    
    /**
     * Get a button by its text (converted to lowercase with underscores)
     */
    public JButton getButton(String text) {
        return buttons.get(text.toLowerCase().replace(" ", "_"));
    }
    
    /**
     * Enable or disable a button by text
     */
    public void setButtonEnabled(String text, boolean enabled) {
        JButton button = getButton(text);
        if (button != null) {
            button.setEnabled(enabled);
            
            // Reapply style after enabling/disabling to maintain custom colors
            String key = text.toLowerCase().replace(" ", "_");
            ButtonStyle style = buttonStyles.get(key);
            if (style != null) {
                applyButtonStyle(button, style);
            }
        }
    }
    
    /**
     * Remove a button by text
     */
    public void removeButton(String text) {
        String key = text.toLowerCase().replace(" ", "_");
        JButton button = buttons.remove(key);
        buttonStyles.remove(key);
        if (button != null) {
            remove(button);
            revalidate();
            repaint();
        }
    }
    
    /**
     * Clear all buttons
     */
    public void clearButtons() {
        buttons.clear();
        buttonStyles.clear();
        removeAll();
        revalidate();
        repaint();
    }
    
    /**
     * Apply different styles to buttons
     */
    private void applyButtonStyle(JButton button, ButtonStyle style) {
        // Make button opaque to ensure background color shows
        button.setOpaque(true);
        button.setBorderPainted(false);
        
        switch (style) {
            case PRIMARY:
                button.setBackground(new Color(52, 152, 219));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                break;
            case SECONDARY:
                button.setBackground(new Color(149, 165, 166));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                break;
            case DANGER:
                button.setBackground(new Color(231, 76, 60));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                break;
            case SUCCESS:
                button.setBackground(new Color(46, 204, 113));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                break;
            default:
                // Keep default styling
                break;
        }
    }
    
    /**
     * Create a standard CRUD button panel (Add, Update, Delete, Clear)
     */
    public static ButtonPanel createCrudPanel(
            ActionListener addAction,
            ActionListener updateAction,
            ActionListener deleteAction,
            ActionListener clearAction) {
        
        ButtonPanel panel = new ButtonPanel();
        
        panel.addStyledButton("Add", addAction, ButtonStyle.PRIMARY);
        panel.addStyledButton("Update", updateAction, ButtonStyle.SECONDARY);
        panel.addStyledButton("Delete", deleteAction, ButtonStyle.DANGER);
        panel.addButton("Clear", clearAction);
        
        // Initially disable update and delete
        panel.setButtonEnabled("Update", false);
        panel.setButtonEnabled("Delete", false);
        
        return panel;
    }
    
    /**
     * Create a standard form button panel (Save, Cancel)
     */
    public static ButtonPanel createFormPanel(ActionListener saveAction, ActionListener cancelAction) {
        ButtonPanel panel = new ButtonPanel();
        
        panel.addStyledButton("Save", saveAction, ButtonStyle.PRIMARY);
        panel.addStyledButton("Cancel", cancelAction, ButtonStyle.SECONDARY);
        
        return panel;
    }
    
    /**
     * Create a search panel with search and clear buttons
     */
    public static ButtonPanel createSearchPanel(ActionListener searchAction, ActionListener clearAction) {
        ButtonPanel panel = new ButtonPanel(FlowLayout.LEFT);
        
        panel.addStyledButton("Search", searchAction, ButtonStyle.PRIMARY);
        panel.addButton("Show All", clearAction);
        
        return panel;
    }
    
    /**
     * Button style enumeration
     */
    public enum ButtonStyle {
        DEFAULT, PRIMARY, SECONDARY, DANGER, SUCCESS
    }
}
