package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable form field component that provides consistent styling and layout
 * for label-input pairs throughout the application
 */
public class FormField extends JPanel {
    private JLabel label;
    private JComponent inputComponent;
    private boolean required;
    
    public FormField(String labelText, JComponent inputComponent) {
        this(labelText, inputComponent, false);
    }
    
    public FormField(String labelText, JComponent inputComponent, boolean required) {
        this.required = required;
        this.inputComponent = inputComponent;
        
        initializeComponents(labelText);
        setupLayout();
    }
    
    private void initializeComponents(String labelText) {
        String displayText = required ? labelText + " *" : labelText;
        label = new JLabel(displayText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (required) {
            label.setForeground(Color.BLACK);
        }
        
        // Set preferred size for input component if not already set
        if (inputComponent.getPreferredSize().width < 200) {
            inputComponent.setPreferredSize(new Dimension(200, inputComponent.getPreferredSize().height));
        }
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Label
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.weightx = 0.0;
        add(label, gbc);
        
        // Input component
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputComponent, gbc);
    }
    
    // Getters and utility methods
    public JComponent getInputComponent() {
        return inputComponent;
    }
    
    public JLabel getLabel() {
        return label;
    }
    
    public String getLabelText() {
        return label.getText();
    }
    
    public void setLabelText(String text) {
        String displayText = required ? text + " *" : text;
        label.setText(displayText);
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
        setLabelText(label.getText().replace(" *", ""));
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        inputComponent.setEnabled(enabled);
        label.setEnabled(enabled);
    }
    
    /**
     * Set tooltip for both label and input component
     */
    public void setFieldToolTip(String tooltip) {
        label.setToolTipText(tooltip);
        inputComponent.setToolTipText(tooltip);
    }
    
    /**
     * Get text value from input component (works for JTextField, JPasswordField, etc.)
     */
    public String getText() {
        if (inputComponent instanceof JTextField) {
            return ((JTextField) inputComponent).getText();
        } else if (inputComponent instanceof JPasswordField) {
            return new String(((JPasswordField) inputComponent).getPassword());
        } else if (inputComponent instanceof JComboBox) {
            Object selected = ((JComboBox<?>) inputComponent).getSelectedItem();
            return selected != null ? selected.toString() : "";
        }
        return "";
    }
    
    /**
     * Set text value to input component
     */
    public void setText(String text) {
        if (inputComponent instanceof JTextField) {
            ((JTextField) inputComponent).setText(text);
        } else if (inputComponent instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<Object> comboBox = (JComboBox<Object>) inputComponent;
            comboBox.setSelectedItem(text);
        }
    }
    
    /**
     * Clear the input component
     */
    public void clear() {
        setText("");
    }
}
