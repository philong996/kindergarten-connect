# UI Components Library

This library provides reusable UI components for the Student Management Application to improve code consistency, maintainability, and development efficiency.

## Components Overview

### 1. FormField
A reusable form field component that provides consistent styling and layout for label-input pairs.

```java
// Basic usage
JTextField nameField = new JTextField(20);
FormField nameFormField = new FormField("Name", nameField, true); // true = required

// Usage with different input types
FormField emailField = new FormField("Email", new JTextField(20));
FormField passwordField = new FormField("Password", new JPasswordField(20), true);
FormField roleField = new FormField("Role", new JComboBox<>(new String[]{"TEACHER", "PARENT"}));

// Access methods
String value = nameFormField.getText();
nameFormField.setText("John Doe");
nameFormField.clear();
nameFormField.setEnabled(false);
```

### 2. ButtonPanel
A reusable button panel that provides consistent styling and common button layouts.

```java
// CRUD panel (most common usage)
ButtonPanel crudPanel = ButtonPanel.createCrudPanel(
    e -> addItem(),      // Add action
    e -> updateItem(),   // Update action
    e -> deleteItem(),   // Delete action
    e -> clearForm()     // Clear action
);

// Custom button panel
ButtonPanel customPanel = new ButtonPanel();
customPanel.addStyledButton("Save", e -> save(), ButtonPanel.ButtonStyle.PRIMARY);
customPanel.addStyledButton("Cancel", e -> cancel(), ButtonPanel.ButtonStyle.SECONDARY);

// Enable/disable buttons
crudPanel.setButtonEnabled("Update", false);
crudPanel.setButtonEnabled("Delete", false);
```

### 3. DataTable
A reusable data table component with built-in sorting, filtering, and selection handling.

```java
// Create table
String[] columns = {"ID", "Name", "Email", "Role"};
DataTable userTable = new DataTable(columns);

// Set event handlers
userTable.setRowSelectionHandler(rowIndex -> onRowSelected(rowIndex));
userTable.setDoubleClickHandler(rowIndex -> onRowDoubleClick(rowIndex));

// Add data
userTable.addRow(new Object[]{1, "John Doe", "john@example.com", "TEACHER"});

// Search/filter
userTable.applyFilter("John", 1); // Search "John" in column 1 (Name)
userTable.clearFilter();

// Selection
int selectedRow = userTable.getSelectedRow();
Object value = userTable.getValueAt(selectedRow, 1);
```

### 4. SearchPanel
A reusable search panel with search field and buttons.

```java
// Simple search panel
SearchPanel searchPanel = SearchPanel.createSimple("Search users:", this::searchUsers);

// Search panel with clear functionality
SearchPanel searchPanel = SearchPanel.createWithClear(
    "Search students:", 
    this::searchStudents,   // Search handler
    this::loadAllStudents   // Clear handler
);

// Access search text
String searchText = searchPanel.getSearchText();
searchPanel.clearSearchText();
```

### 5. HeaderPanel
A reusable header panel for consistent page headers.

```java
// Simple header
HeaderPanel header = HeaderPanel.createSimple("Student Management");

// Header with user info
HeaderPanel header = HeaderPanel.createWithUser("Dashboard", "Welcome Admin - john_admin");

// Dashboard header with role-based styling
HeaderPanel header = HeaderPanel.createDashboard("PRINCIPAL", "john_admin");

// Customize
header.setTitle("New Title");
header.setUserInfo("Updated user info");
header.setHeaderColor(Color.BLUE);
```

### 6. FormBuilder
A builder pattern component for creating forms with consistent layout.

```java
// Create form builder
FormBuilder formBuilder = new FormBuilder("User Information", 2); // 2 columns

// Add fields with IDs for later access
formBuilder.addTextField("username", "Username", true)           // required
          .addPasswordField("password", "Password", true)        // required  
          .addComboBox("role", "Role", new String[]{"TEACHER", "PARENT"}, true)
          .addTextArea("notes", "Notes", 3, false);              // not required

// Build the form
JPanel formPanel = formBuilder.build();

// Or build with buttons
ButtonPanel buttons = ButtonPanel.createFormPanel(e -> save(), e -> cancel());
JPanel formWithButtons = formBuilder.buildWithButtons(buttons);

// Access field values
String username = formBuilder.getValue("username");
formBuilder.setValue("username", "john_doe");
formBuilder.clearAll();

// Validation
if (formBuilder.validateRequired()) {
    // All required fields are filled
    Map<String, String> allValues = formBuilder.getAllValues();
}
```

### 7. DialogFactory
Utility class for creating consistent dialogs.

```java
// Confirmation dialogs
boolean confirmed = DialogFactory.showConfirmation(this, "Are you sure?");
boolean deleteConfirmed = DialogFactory.showDeleteConfirmation(this, "this user");

// Message dialogs
DialogFactory.showSuccess(this, "Operation completed successfully!");
DialogFactory.showError(this, "An error occurred");
DialogFactory.showWarning(this, "Please check your input");

// Input dialog
String input = DialogFactory.showInput(this, "Enter your name:");

// Custom form dialog
FormBuilder dialogForm = new FormBuilder("Add User")
    .addTextField("name", "Name", true)
    .addTextField("email", "Email", true);

DialogFactory.FormDialog dialog = DialogFactory.showFormDialog(this, "New User", dialogForm);
if (dialog.isOkClicked()) {
    String name = dialog.getFormBuilder().getValue("name");
    String email = dialog.getFormBuilder().getValue("email");
}

// Loading dialog
DialogFactory.executeWithLoading(this, "Processing", "Please wait...", () -> {
    // Long running task
    doTimeConsumingOperation();
});
```



## Benefits

1. **Consistency**: All components follow the same design patterns and styling
2. **Maintainability**: Changes to UI behavior can be made in one place
3. **Productivity**: Faster development with pre-built, tested components
4. **Reusability**: Components can be used across different panels and dialogs
5. **Flexibility**: Components are configurable and extensible



## Best Practices

1. Use consistent field IDs in `FormBuilder` (use constants)
2. Always handle exceptions in event handlers
3. Use `DialogFactory` for all user notifications
4. Set up proper event handlers for `DataTable` selection
5. Validate forms using `FormBuilder.validateRequired()`
6. Use appropriate button styles for different actions (PRIMARY, DANGER, etc.)

## Design Patterns Applied

### 1. **Template Method Pattern**
Applied in `BaseAuthenticatedPage` to define the structure of page initialization while allowing subclasses to customize specific steps.

```java
// BaseAuthenticatedPage defines the algorithm structure
private final void initializePage() {
    initializeWindowProperties();    // Fixed step
    initializeComponents();          // Hook method - customizable
    setupLayout();                   // Hook method - customizable
    setupPermissions();             // Hook method - customizable
    setupEventHandlers();           // Hook method - customizable
    setupCommonWindowBehavior();    // Fixed step
    loadInitialData();              // Hook method - customizable
}

// Subclasses implement specific hook methods
public class PrincipalPage extends BaseAuthenticatedPage {
    @Override
    protected void initializeComponents() {
        // Principal-specific component initialization
    }
    
    @Override
    protected void setupPermissions() {
        // Principal-specific permission setup
    }
}
```

**Benefits:**
- Ensures consistent initialization sequence across all pages
- Prevents subclasses from breaking the initialization flow
- Promotes code reuse while allowing customization

### 2. **Builder Pattern**
Applied in `FormBuilder` to construct complex forms step by step with a fluent interface.

```java
// Fluent builder interface
FormBuilder formBuilder = new FormBuilder("User Information", 2)
    .addTextField("username", "Username", true)
    .addPasswordField("password", "Password", true)
    .addComboBox("role", "Role", roles, true)
    .addTextArea("notes", "Notes", 3, false);

JPanel form = formBuilder.build();
```

**Benefits:**
- Readable and intuitive form construction
- Flexible field ordering and configuration
- Immutable builder state until build() is called
- Type-safe construction process

### 3. **Factory Pattern**
Applied in `DialogFactory` to create consistent dialog instances and in various component factory methods.

```java
// DialogFactory - Static factory methods
public class DialogFactory {
    public static boolean showConfirmation(Component parent, String message) {
        // Consistent dialog creation logic
    }
    
    public static void showError(Component parent, String message) {
        // Standardized error dialog creation
    }
}

// Component factory methods
HeaderPanel header = HeaderPanel.createDashboard("PRINCIPAL", username);
ButtonPanel buttons = ButtonPanel.createCrudPanel(add, update, delete, clear);
SearchPanel search = SearchPanel.createWithClear(label, searchHandler, clearHandler);
```

**Benefits:**
- Encapsulates object creation complexity
- Ensures consistent dialog/component configuration
- Provides convenient factory methods for common use cases
- Centralizes creation logic for easier maintenance

### 4. **Observer Pattern**
Applied in `DataTable` for row selection events and `SearchPanel` for search events using functional interfaces.

```java
// DataTable uses Consumer<Integer> for row selection events
DataTable table = new DataTable(columns);
table.setRowSelectionHandler(rowIndex -> {
    // Handle row selection
    loadSelectedItem(rowIndex);
    updateButtonStates();
});

// SearchPanel uses Consumer<String> and Runnable for events
SearchPanel search = SearchPanel.createWithClear(
    "Search:",
    searchText -> performSearch(searchText),    // Consumer<String>
    () -> loadAllData()                         // Runnable
);
```

**Benefits:**
- Loose coupling between UI components and business logic
- Event-driven architecture for responsive UI
- Type-safe event handling with functional interfaces
- Easy to add/remove event listeners

### 5. **Composite Pattern**
Applied in panel composition where complex UI panels are built from simpler component parts.

```java
// StudentManagementPanelNew composes multiple components
public class StudentManagementPanelNew extends JPanel {
    private DataTable studentTable;        // Leaf component
    private SearchPanel searchPanel;       // Leaf component  
    private FormBuilder formBuilder;       // Composite component
    private ButtonPanel buttonPanel;       // Composite component
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);     // Add leaf
        add(studentTable, BorderLayout.CENTER);   // Add leaf
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formBuilder.build(), BorderLayout.CENTER);  // Add composite
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);           // Add composite
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
```

**Benefits:**
- Uniform treatment of individual components and compositions
- Simplified client code for complex UI structures
- Easy to add new component types
- Hierarchical organization of UI elements

### 6. **Strategy Pattern**
Applied in button styling and component behavior customization.

```java
// ButtonPanel.ButtonStyle enum defines different styling strategies
public enum ButtonStyle {
    DEFAULT, PRIMARY, SECONDARY, DANGER, SUCCESS
}

// Different styling strategies applied based on button type
private void applyButtonStyle(JButton button, ButtonStyle style) {
    switch (style) {
        case PRIMARY:
            button.setBackground(new Color(52, 152, 219));
            break;
        case DANGER:
            button.setBackground(new Color(231, 76, 60));
            break;
        // ... other strategies
    }
}

// Usage
buttonPanel.addStyledButton("Save", saveAction, ButtonStyle.PRIMARY);
buttonPanel.addStyledButton("Delete", deleteAction, ButtonStyle.DANGER);
```

**Benefits:**
- Encapsulates styling algorithms
- Easy to add new button styles
- Consistent styling application
- Runtime style selection

### 7. **Facade Pattern**
Applied in high-level component interfaces that hide complexity of underlying Swing components.

```java
// DataTable facade hides complex JTable setup
public class DataTable extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JScrollPane scrollPane;
    
    // Simple facade methods hide complexity
    public void addRow(Object[] rowData) {
        tableModel.addRow(rowData);
    }
    
    public void applyFilter(String searchText, int columnIndex) {
        // Complex filtering logic hidden
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, columnIndex));
    }
}

// Client code is simplified
DataTable table = new DataTable(columns);
table.addRow(data);              // Simple interface
table.applyFilter("John", 1);    // Complex operation simplified
```

**Benefits:**
- Simplified interface for complex subsystems
- Reduced learning curve for component usage
- Encapsulates Swing complexity
- Promotes consistent usage patterns

### 8. **Decorator Pattern**
Applied in `FormField` to enhance basic input components with additional features.

```java
// FormField decorates basic input components
public class FormField extends JPanel {
    private JLabel label;
    private JComponent inputComponent;  // The component being decorated
    private boolean required;
    
    public FormField(String labelText, JComponent inputComponent, boolean required) {
        this.inputComponent = inputComponent;  // Wrap the original component
        this.required = required;
        
        // Add decorations: label, validation indicators, tooltips
        initializeComponents(labelText);
        setupLayout();
    }
    
    // Enhanced behavior
    public void setFieldToolTip(String tooltip) {
        label.setToolTipText(tooltip);           // Enhance label
        inputComponent.setToolTipText(tooltip);  // Enhance input
    }
}
```

**Benefits:**
- Adds functionality to existing components without modification
- Consistent enhancement across different input types
- Flexible combination of decorations
- Maintains component interface compatibility

### 9. **Command Pattern**
Applied in button actions and event handlers using lambda expressions and method references.

```java
// Button actions encapsulated as commands
ButtonPanel buttonPanel = ButtonPanel.createCrudPanel(
    e -> addStudent(),     // Command: add operation
    e -> updateStudent(),  // Command: update operation  
    e -> deleteStudent(),  // Command: delete operation
    e -> clearForm()       // Command: clear operation
);

// Search commands
SearchPanel searchPanel = SearchPanel.createWithClear(
    "Search:",
    this::performSearch,   // Command: search operation
    this::loadAllData      // Command: refresh operation
);
```

**Benefits:**
- Encapsulates operations as objects
- Supports undo operations (future enhancement)
- Parameterizable actions
- Decouples invoker from receiver

### 10. **MVC (Model-View-Controller) Pattern**
Applied in the overall architecture separating concerns between data, UI, and business logic.

```java
// Model: Data entities
public class Student {
    private int id;
    private String name;
    // ... data fields
}

// View: UI Components
public class StudentManagementPanelNew extends JPanel {
    private DataTable studentTable;        // View component
    private FormBuilder formBuilder;       // View component
    // ... UI components only
}

// Controller: Service layer
public class StudentService {
    public List<Student> getAllStudents() { /* business logic */ }
    public boolean addStudent(Student student) { /* business logic */ }
    // ... business operations
}

// Usage in View
private void loadStudentData() {
    List<Student> students = studentService.getAllStudents();  // Controller
    updateTable(students);                                      // Update View
}
```

**Benefits:**
- Clear separation of concerns
- Testable business logic
- Reusable service layer
- Maintainable UI code

## Pattern Benefits Summary

| Pattern | Primary Benefit | Applied In |
|---------|----------------|------------|
| Template Method | Consistent initialization flow | BaseAuthenticatedPage |
| Builder | Fluent object construction | FormBuilder |
| Factory | Standardized object creation | DialogFactory, Component factories |
| Observer | Event-driven architecture | DataTable, SearchPanel |
| Composite | Hierarchical UI organization | Panel compositions |
| Strategy | Configurable behavior | Button styling |
| Facade | Simplified interfaces | DataTable, complex components |
| Decorator | Enhanced functionality | FormField |
| Command | Encapsulated operations | Button actions, event handlers |
| MVC | Separation of concerns | Overall architecture |

These patterns work together to create a maintainable, extensible, and consistent UI framework that promotes code reuse and follows established software engineering principles.