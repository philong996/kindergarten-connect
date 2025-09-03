package ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Reusable data table component with common functionality like
 * sorting, selection handling, and consistent styling
 */
public class DataTable extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private TableRowSorter<DefaultTableModel> sorter;
    private Consumer<Integer> rowSelectionHandler;
    private Consumer<Integer> doubleClickHandler;
    
    public DataTable(String[] columnNames) {
        initializeComponents(columnNames);
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents(String[] columnNames) {
        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only by default
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(25);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        table.setOpaque(false);
        table.setBackground(new Color(0, 0, 0, 0));
        // Add table header styling
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(AppColor.getColor("lightOrange"));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(AppColor.getColor("yellowOrange"), 2, true));
        
        // Create sorter
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        // Create scroll pane
        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        scrollPane.setOpaque(false);
        // tableScrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(AppColor.getColor("lightOrange"));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Row selection handler
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && rowSelectionHandler != null) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    // Convert view row to model row (for sorting)
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    rowSelectionHandler.accept(modelRow);
                } else {
                    rowSelectionHandler.accept(-1);
                }
            }
        });
        
        // Double-click handler
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && doubleClickHandler != null) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        doubleClickHandler.accept(modelRow);
                    }
                }
            }
        });
    }
    
    /**
     * Set row selection handler
     */
    public void setRowSelectionHandler(Consumer<Integer> handler) {
        this.rowSelectionHandler = handler;
    }
    
    /**
     * Set double-click handler
     */
    public void setDoubleClickHandler(Consumer<Integer> handler) {
        this.doubleClickHandler = handler;
    }
    
    /**
     * Add a row to the table
     */
    public void addRow(Object[] rowData) {
        tableModel.addRow(rowData);
    }
    
    /**
     * Clear all rows
     */
    public void clearRows() {
        tableModel.setRowCount(0);
    }
    
    /**
     * Get selected row index (model index, not view index)
     */
    public int getSelectedRow() {
        int viewRow = table.getSelectedRow();
        return viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
    }
    
    /**
     * Get value at specific row and column (model coordinates)
     */
    public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(row, column);
    }
    
    /**
     * Set value at specific row and column (model coordinates)
     */
    public void setValueAt(Object value, int row, int column) {
        tableModel.setValueAt(value, row, column);
    }
    
    /**
     * Get row count
     */
    public int getRowCount() {
        return tableModel.getRowCount();
    }
    
    /**
     * Get column count
     */
    public int getColumnCount() {
        return tableModel.getColumnCount();
    }
    
    /**
     * Set column widths
     */
    public void setColumnWidths(int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }
    
    /**
     * Set preferred table size
     */
    public void setTableSize(Dimension size) {
        scrollPane.setPreferredSize(size);
    }
    
    /**
     * Apply row filter for searching
     */
    public void applyFilter(String searchText, int columnIndex) {
        if (searchText == null || searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, columnIndex));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Invalid regex, clear filter
                sorter.setRowFilter(null);
            }
        }
    }
    
    /**
     * Clear any applied filters
     */
    public void clearFilter() {
        sorter.setRowFilter(null);
    }
    
    /**
     * Get the underlying JTable for direct access if needed
     */
    public JTable getTable() {
        return table;
    }
    
    /**
     * Get the table model for direct access if needed
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    
    /**
     * Select a specific row by model index
     */
    public void selectRow(int modelRowIndex) {
        if (modelRowIndex >= 0 && modelRowIndex < tableModel.getRowCount()) {
            int viewRowIndex = table.convertRowIndexToView(modelRowIndex);
            table.setRowSelectionInterval(viewRowIndex, viewRowIndex);
            table.scrollRectToVisible(table.getCellRect(viewRowIndex, 0, true));
        }
    }
    
    /**
     * Clear table selection
     */
    public void clearSelection() {
        table.clearSelection();
    }
}
