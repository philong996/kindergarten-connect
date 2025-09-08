package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for creating consistent dialogs throughout the application
 */
public class DialogFactory {
    
    /**
     * Show a confirmation dialog with custom message and title
     */
    public static boolean showConfirmation(Component parent, String message, String title) {
        int option = JOptionPane.showConfirmDialog(
            parent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return option == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show a simple confirmation dialog
     */
    public static boolean showConfirmation(Component parent, String message) {
        return showConfirmation(parent, message, "Confirmation");
    }
    
    /**
     * Show a delete confirmation dialog
     */
    public static boolean showDeleteConfirmation(Component parent, String itemName) {
        return showConfirmation(
            parent,
            "Are you sure you want to delete " + itemName + "?\nThis action cannot be undone.",
            "Delete Confirmation"
        );
    }
    
    /**
     * Show an error dialog
     */
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show a simple error dialog
     */
    public static void showError(Component parent, String message) {
        showError(parent, message, "Error");
    }
    
    /**
     * Show a success dialog
     */
    public static void showSuccess(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Show a simple success dialog
     */
    public static void showSuccess(Component parent, String message) {
        showSuccess(parent, message, "Success");
    }
    
    /**
     * Show a warning dialog
     */
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show a simple warning dialog
     */
    public static void showWarning(Component parent, String message) {
        showWarning(parent, message, "Warning");
    }
    
    /**
     * Show an input dialog
     */
    public static String showInput(Component parent, String message, String title, String defaultValue) {
        return (String) JOptionPane.showInputDialog(
            parent,
            message,
            title,
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            defaultValue
        );
    }
    
    /**
     * Show a simple input dialog
     */
    public static String showInput(Component parent, String message) {
        return showInput(parent, message, "Input", "");
    }
    
    /**
     * Create a custom form dialog
     */
    public static class FormDialog extends JDialog {
        private boolean okClicked = false;
        private FormBuilder formBuilder;
        
        public FormDialog(Frame parent, String title, FormBuilder formBuilder) {
            super(parent, title, true);
            this.formBuilder = formBuilder;
            initializeDialog();
        }
        
        private void initializeDialog() {
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());
            
            // Add form
            add(formBuilder.build(), BorderLayout.CENTER);
            
            // Add buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            CustomButton buttonOK = new CustomButton("OK", AppColor.getColor("violet"), AppColor.getColor("softViolet"), Color.BLACK);
            CustomButton buttonCancel = new CustomButton("Cancel", AppColor.getColor("violet"), AppColor.getColor("softViolet"), Color.BLACK);
            buttonPanel.add(buttonOK);
            buttonPanel.add(buttonCancel);
            buttonOK.addActionListener(e -> {
                    if (formBuilder.validateRequired()) {
                        okClicked = true;
                        dispose();
                    }
                });
            buttonCancel.addActionListener(e -> dispose());

            add(buttonPanel, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(getParent());
        }
        
        public boolean isOkClicked() {
            return okClicked;
        }
        
        public FormBuilder getFormBuilder() {
            return formBuilder;
        }
    }
    
    /**
     * Show a custom form dialog
     */
    public static FormDialog showFormDialog(Frame parent, String title, FormBuilder formBuilder) {
        FormDialog dialog = new FormDialog(parent, title, formBuilder);
        dialog.setVisible(true);
        return dialog;
    }
    
    /**
     * Create a loading dialog
     */
    public static class LoadingDialog extends JDialog {
        private JProgressBar progressBar;
        private JLabel messageLabel;
        private boolean cancelled = false;
        
        public LoadingDialog(Frame parent, String title, String message) {
            super(parent, title, true);
            initializeDialog(message);
        }
        
        private void initializeDialog(String message) {
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            setLayout(new BorderLayout());
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            messageLabel = new JLabel(message, SwingConstants.CENTER);
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(300, 25));
            
            mainPanel.add(messageLabel, BorderLayout.NORTH);
            mainPanel.add(Box.createVerticalStrut(15), BorderLayout.CENTER);
            mainPanel.add(progressBar, BorderLayout.SOUTH);
            
            add(mainPanel, BorderLayout.CENTER);
            
            // Optional cancel button
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> {
                cancelled = true;
                dispose();
            });
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(getParent());
        }
        
        public void updateMessage(String message) {
            SwingUtilities.invokeLater(() -> messageLabel.setText(message));
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public void setProgress(int value) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                progressBar.setValue(value);
            });
        }
        
        public void setIndeterminate(boolean indeterminate) {
            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(indeterminate));
        }
    }
    
    /**
     * Show a loading dialog
     */
    public static LoadingDialog showLoadingDialog(Frame parent, String title, String message) {
        LoadingDialog dialog = new LoadingDialog(parent, title, message);
        return dialog;
    }
    
    /**
     * Execute a task with a loading dialog
     */
    public static void executeWithLoading(Frame parent, String title, String message, Runnable task) {
        LoadingDialog dialog = showLoadingDialog(parent, title, message);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
            
            @Override
            protected void done() {
                dialog.dispose();
            }
        };
        
        worker.execute();
        dialog.setVisible(true);
    }
}
