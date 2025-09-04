package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable button panel component that provides consistent styling and layout
 * for action buttons throughout the application, now using CustomButton
 */
public class ButtonPanel1 extends JPanel {
    private Map<String, CustomButton> buttons;
    private Map<String, ButtonStyle> buttonStyles;
    private int alignment;

    public ButtonPanel1() {
        this(FlowLayout.CENTER);
    }

    public ButtonPanel1(int alignment) {
        this.alignment = alignment;
        this.buttons = new HashMap<>();
        this.buttonStyles = new HashMap<>();

        setLayout(new FlowLayout(alignment, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setOpaque(false);
    }

    /**
     * Add a button with text and action listener
     */
    public CustomButton addButton(String text, ActionListener actionListener) {
        return addButton(text, actionListener, true);
    }

    public CustomButton addButton(String text, ActionListener actionListener, int width, int height) {
        return addButton(text, actionListener, true, width, height);
    }

    /**
     * Add a button with text, action listener, and enabled state
     */
    public CustomButton addButton(String text, ActionListener actionListener, boolean enabled) {
        CustomButton button = new CustomButton(text);
        button.setEnabled(enabled);
        button.setPreferredSize(new Dimension(120, 30));

        if (actionListener != null) {
            button.addActionListener(actionListener);
        }

        String key = text.toLowerCase().replace(" ", "_");
        buttons.put(key, button);
        add(button);

        return button;
    }

    public CustomButton addButton(String text, ActionListener actionListener, boolean enabled, int width, int height) {
        CustomButton button = new CustomButton(text);
        button.setEnabled(enabled);
        button.setPreferredSize(new Dimension(width, height));

        if (actionListener != null) {
            button.addActionListener(actionListener);
        }

        String key = text.toLowerCase().replace(" ", "_");
        buttons.put(key, button);
        add(button);

        return button;
    }

    /**
     * Add a styled button (primary, secondary, danger, success)
     */
    public CustomButton addStyledButton(String text, ActionListener actionListener, ButtonStyle style) {
        Color bg, hover, fg = Color.WHITE;

        switch (style) {
            case PRIMARY:
                bg = AppColor.getColor("blue");
                hover = bg.brighter();
                break;
            case SECONDARY:
                bg = AppColor.getColor("freshGreen");
                hover = bg.brighter();
                break;
            case DANGER:
                bg = AppColor.getColor("coralRed");
                hover = bg.brighter();
                break;
            case SUCCESS:
                bg = AppColor.getColor("yellowOrange");
                hover = bg.brighter();
                break;
            default:
                bg = Color.WHITE;
                hover = bg.darker();
                fg = UIManager.getColor("Button.foreground");
                break;
        }

        CustomButton button = new CustomButton(text, bg, hover, fg);
        button.setPreferredSize(new Dimension(120, 30));

        if (actionListener != null) {
            button.addActionListener(actionListener);
        }

        String key = text.toLowerCase().replace(" ", "_");
        buttons.put(key, button);
        buttonStyles.put(key, style);

        add(button);
        return button;
    }

    /**
     * Get a button by its text (converted to lowercase with underscores)
     */
    public CustomButton getButton(String text) {
        return buttons.get(text.toLowerCase().replace(" ", "_"));
    }

    /**
     * Enable or disable a button by text
     */
    public void setButtonEnabled(String text, boolean enabled) {
        CustomButton button = getButton(text);
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    /**
     * Remove a button by text
     */
    public void removeButton(String text) {
        String key = text.toLowerCase().replace(" ", "_");
        CustomButton button = buttons.remove(key);
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
     * Create a standard CRUD button panel (Add, Update, Delete, Clear)
     */
    public static ButtonPanel1 createCrudPanel(
            ActionListener addAction,
            ActionListener updateAction,
            ActionListener deleteAction,
            ActionListener clearAction) {

        ButtonPanel1 panel = new ButtonPanel1();

        panel.addStyledButton("Add", addAction, ButtonStyle.PRIMARY);
        panel.addStyledButton("Update", updateAction, ButtonStyle.SECONDARY);
        panel.addStyledButton("Delete", deleteAction, ButtonStyle.DANGER);
        panel.addStyledButton("Clear", clearAction, ButtonStyle.DEFAULT);

        // Initially disable update and delete
        panel.setButtonEnabled("Update", false);
        panel.setButtonEnabled("Delete", false);

        return panel;
    }

    /**
     * Create a standard form button panel (Save, Cancel)
     */
    public static ButtonPanel1 createFormPanel(ActionListener saveAction, ActionListener cancelAction) {
        ButtonPanel1 panel = new ButtonPanel1();

        panel.addStyledButton("Save", saveAction, ButtonStyle.PRIMARY);
        panel.addStyledButton("Cancel", cancelAction, ButtonStyle.SECONDARY);

        return panel;
    }

    /**
     * Create a search panel with search and clear buttons
     */
    public static ButtonPanel1 createSearchPanel(ActionListener searchAction, ActionListener clearAction) {
        ButtonPanel1 panel = new ButtonPanel1(FlowLayout.LEFT);

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
