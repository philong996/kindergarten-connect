package ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Form builder component that helps create consistent forms
 * using the FormField components
 */
public class FormBuilder {
    private List<FormField> formFields;
    private Map<String, FormField> fieldMap;
    private String title;
    private int columns;
    
    public FormBuilder() {
        this(null, 1);
    }
    
    public FormBuilder(String title) {
        this(title, 1);
    }
    
    public FormBuilder(String title, int columns) {
        this.title = title;
        this.columns = Math.max(1, columns);
        this.formFields = new ArrayList<>();
        this.fieldMap = new HashMap<>();
    }
    
    /**
     * Add a text field to the form
     */
    public FormBuilder addTextField(String id, String label, boolean required) {
        JTextField textField = new JTextField(20);
        FormField formField = new FormField(label, textField, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a text field with default text
     */
    public FormBuilder addTextField(String id, String label, String defaultText, boolean required) {
        JTextField textField = new JTextField(defaultText, 20);
        FormField formField = new FormField(label, textField, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a password field to the form
     */
    public FormBuilder addPasswordField(String id, String label, boolean required) {
        JPasswordField passwordField = new JPasswordField(20);
        FormField formField = new FormField(label, passwordField, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a combo box to the form
     */
    public FormBuilder addComboBox(String id, String label, String[] options, boolean required) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        FormField formField = new FormField(label, comboBox, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a text area to the form
     */
    public FormBuilder addTextArea(String id, String label, int rows, boolean required) {
        JTextArea textArea = new JTextArea(rows, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        FormField formField = new FormField(label, scrollPane, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a date field (text field with date formatting hint)
     */
    public FormBuilder addDateField(String id, String label, boolean required) {
        JTextField dateField = new JTextField(20);
        dateField.setToolTipText("Format: YYYY-MM-DD (e.g., 2019-05-15)");
        FormField formField = new FormField(label, dateField, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Add a number field
     */
    public FormBuilder addNumberField(String id, String label, boolean required) {
        JTextField numberField = new JTextField(20);
        numberField.setToolTipText("Enter numbers only");
        FormField formField = new FormField(label, numberField, required);
        formFields.add(formField);
        fieldMap.put(id, formField);
        return this;
    }
    
    /**
     * Build the form panel
     */
    public JPanel build() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        if (title != null) {
            formPanel.setBorder(BorderFactory.createTitledBorder(title));
        }
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        int col = 0;
        
        for (FormField field : formFields) {
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            
            formPanel.add(field, gbc);
            
            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
        
        return formPanel;
    }
    
    /**
     * Build the form panel with buttons
     */
    public JPanel buildWithButtons(ButtonPanel buttonPanel) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(build(), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        return mainPanel;
    }
    
    /**
     * Get a form field by ID
     */
    public FormField getField(String id) {
        return fieldMap.get(id);
    }
    
    /**
     * Get the value of a field by ID
     */
    public String getValue(String id) {
        FormField field = fieldMap.get(id);
        return field != null ? field.getText() : null;
    }
    
    /**
     * Set the value of a field by ID
     */
    public void setValue(String id, String value) {
        FormField field = fieldMap.get(id);
        if (field != null) {
            field.setText(value);
        }
    }
    
    /**
     * Clear all form fields
     */
    public void clearAll() {
        for (FormField field : formFields) {
            field.clear();
        }
    }
    
    /**
     * Validate required fields
     */
    public boolean validateRequired() {
        for (FormField field : formFields) {
            if (field.isRequired() && field.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    null,
                    "Please fill in the required field: " + field.getLabelText().replace(" *", ""),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                field.getInputComponent().requestFocus();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Set enabled state for all fields
     */
    public void setEnabled(boolean enabled) {
        for (FormField field : formFields) {
            field.setEnabled(enabled);
        }
    }
    
    /**
     * Set enabled state for a specific field
     */
    public void setFieldEnabled(String id, boolean enabled) {
        FormField field = fieldMap.get(id);
        if (field != null) {
            field.setEnabled(enabled);
        }
    }
    
    /**
     * Get all field values as a map
     */
    public Map<String, String> getAllValues() {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, FormField> entry : fieldMap.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getText());
        }
        return values;
    }
    
    /**
     * Set multiple field values from a map
     */
    public void setAllValues(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            setValue(entry.getKey(), entry.getValue());
        }
    }
}
