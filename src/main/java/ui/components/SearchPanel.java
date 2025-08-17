package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Reusable search panel component with search field and buttons
 */
public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JLabel searchLabel;
    private Consumer<String> searchHandler;
    private Runnable clearHandler;
    
    public SearchPanel(String labelText) {
        this(labelText, 20);
    }
    
    public SearchPanel(String labelText, int fieldWidth) {
        initializeComponents(labelText, fieldWidth);
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents(String labelText, int fieldWidth) {
        searchLabel = new JLabel(labelText);
        searchField = new JTextField(fieldWidth);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        
        // Style the components
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        searchButton.setPreferredSize(new Dimension(80, 25));
        clearButton.setPreferredSize(new Dimension(70, 25));
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        add(searchLabel);
        add(searchField);
        add(searchButton);
        add(clearButton);
    }
    
    private void setupEventHandlers() {
        // Search button action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performClear();
            }
        });
        
        // Enter key in search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
    }
    
    private void performSearch() {
        if (searchHandler != null) {
            String searchText = searchField.getText().trim();
            searchHandler.accept(searchText);
        }
    }
    
    private void performClear() {
        searchField.setText("");
        if (clearHandler != null) {
            clearHandler.run();
        }
    }
    
    /**
     * Set search handler that will be called when search is performed
     */
    public void setSearchHandler(Consumer<String> handler) {
        this.searchHandler = handler;
    }
    
    /**
     * Set clear handler that will be called when clear is performed
     */
    public void setClearHandler(Runnable handler) {
        this.clearHandler = handler;
    }
    
    /**
     * Get the current search text
     */
    public String getSearchText() {
        return searchField.getText().trim();
    }
    
    /**
     * Set the search text
     */
    public void setSearchText(String text) {
        searchField.setText(text);
    }
    
    /**
     * Clear the search field
     */
    public void clearSearchText() {
        searchField.setText("");
    }
    
    /**
     * Set placeholder text for the search field
     */
    public void setPlaceholder(String placeholder) {
        searchField.setToolTipText(placeholder);
    }
    
    /**
     * Enable or disable the search functionality
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchField.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        searchLabel.setEnabled(enabled);
    }
    
    /**
     * Focus on the search field
     */
    public void focusSearchField() {
        searchField.requestFocusInWindow();
    }
    
    /**
     * Get the search field for direct access if needed
     */
    public JTextField getSearchField() {
        return searchField;
    }
    
    /**
     * Get the search button for direct access if needed
     */
    public JButton getSearchButton() {
        return searchButton;
    }
    
    /**
     * Get the clear button for direct access if needed
     */
    public JButton getClearButton() {
        return clearButton;
    }
    
    /**
     * Create a simple search panel with just a text field and search functionality
     */
    public static SearchPanel createSimple(String labelText, Consumer<String> searchHandler) {
        SearchPanel panel = new SearchPanel(labelText);
        panel.setSearchHandler(searchHandler);
        return panel;
    }
    
    /**
     * Create a search panel with both search and clear functionality
     */
    public static SearchPanel createWithClear(String labelText, Consumer<String> searchHandler, Runnable clearHandler) {
        SearchPanel panel = new SearchPanel(labelText);
        panel.setSearchHandler(searchHandler);
        panel.setClearHandler(clearHandler);
        return panel;
    }
}
