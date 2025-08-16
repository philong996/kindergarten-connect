# Authorization System Documentation

## Overview

This authorization system provides comprehensive role-based access control for the Kindergarten Management System, implementing the following user roles and permissions:

## User Roles and Permissions

### 1. Hiệu trưởng (Principal) - PRINCIPAL
**Full system access within their school**
- ✅ Tạo account phụ huynh/giáo viên (Create user accounts)
- ✅ Tạo học sinh (Create students)
- ✅ Tạo post (Create posts)
- ✅ Bình luận và like posts (Comment and like posts)
- ✅ Cập nhật thông tin học sinh (Update student information)
- ✅ Xem tất cả posts trong trường (View all posts in school)
- ✅ Quản lý điểm danh (Manage attendance)
- ✅ Xem báo cáo (View reports)
- ✅ Gửi tin nhắn (Send messages)
- ✅ Quản lý toàn bộ trường (Manage entire school)

### 2. Giáo viên (Teacher) - TEACHER
**Access limited to their assigned class**
- ✅ Tạo post thông tin học sinh (Create posts about students)
- ✅ Bình luận posts (Comment on posts)
- ✅ Cập nhật thông tin học sinh lớp mình (Update student information in their class)
- ✅ Xem posts của lớp mình dạy (View posts in their assigned class)
- ✅ Điểm danh học sinh (Mark attendance)
- ✅ Gửi tin nhắn với phụ huynh (Send messages to parents)
- ✅ Like posts

### 3. Phụ huynh (Parent) - PARENT
**Access limited to their children's classes**
- ✅ Bình luận posts (Comment on posts)
- ✅ Like posts
- ✅ Xem posts của lớp con mình (View posts only from their children's classes)
- ✅ Gửi tin nhắn với giáo viên (Send messages to teachers)
- ❌ Không xem được posts của lớp khác (Cannot view posts from other classes)

## Implementation Architecture

### Design Patterns

The authorization system implements a **Multi-Layered Authorization Architecture** that combines several well-established design patterns for maintainable, secure, and scalable access control:

#### 1. **Dependency Injection Pattern**
- **Purpose**: Decouples UI components from service implementations
- **Implementation**: Each UI page receives `AuthService` through constructor injection
```java
public class PrincipalPage extends JFrame {
    private AuthService authService;
    
    public PrincipalPage(AuthService authService) {
        this.authService = authService;  // Injected dependency
        initializeComponents();
    }
}
```
- **Benefits**: Enables easy testing, promotes loose coupling, supports different service implementations

#### 2. **Service Layer Pattern**
- **Purpose**: Separates business logic from presentation layer
- **Implementation**: 
  - `AuthService` handles authentication (login/logout/current user state)
  - `AuthorizationService` manages permissions and role-based access control
- **Benefits**: Centralized business logic, reusable across different UI components

#### 3. **Facade Pattern**
- **Purpose**: Provides simplified interface to complex authorization subsystem
- **Implementation**: `AuthService` acts as a facade providing unified access
```java
public class AuthService {
    public AuthorizationService getAuthorizationService() {
        if (authorizationService == null) {
            initializeAuthorization();
        }
        return authorizationService;
    }
}
```
- **Benefits**: Hides complexity, provides single entry point, manages initialization

#### 4. **Strategy Pattern**
- **Purpose**: Implements different permission strategies for different user roles
- **Implementation**: Role-based permission sets with pluggable strategies
```java
private boolean hasPermissionForRole(String role, String permission) {
    switch (role) {
        case "PRINCIPAL": return getPrincipalPermissions().contains(permission);
        case "TEACHER": return getTeacherPermissions().contains(permission);
        case "PARENT": return getParentPermissions().contains(permission);
    }
}
```
- **Benefits**: Easy to add new roles, modify permissions, supports role hierarchy

#### 5. **Utility/Helper Pattern**
- **Purpose**: Provides common authorization operations with UI integration
- **Implementation**: `AuthUtil` class with static convenience methods
```java
public class AuthUtil {
    public static boolean checkPermissionWithMessage(AuthService authService, 
                                                   String permission, String action) {
        // Check permission and show localized error message if unauthorized
    }
    
    public static boolean canManageStudents(AuthService authService) {
        return authService.getAuthorizationService()
                         .hasPermission(AuthorizationService.PERM_UPDATE_STUDENTS);
    }
}
```
- **Benefits**: Reduces code duplication, consistent error messages, UI-aware authorization

#### 6. **Template Method Pattern**
- **Purpose**: Defines consistent authorization algorithm structure across all UI pages
- **Implementation**: Abstract base class `BaseAuthenticatedPage` with template method `initializePage()`
```java
public abstract class BaseAuthenticatedPage extends JFrame {
    // Template method - defines the algorithm structure (final to prevent override)
    private final void initializePage() {
        initializeWindowProperties();    // Common behavior
        initializeComponents();          // Hook method - subclass specific
        setupLayout();                   // Hook method - subclass specific
        setupPermissions();              // Hook method - subclass specific
        setupEventHandlers();            // Hook method - subclass specific
        setupCommonWindowBehavior();     // Common behavior
        loadInitialData();               // Hook method - optional
    }
    
    // Hook methods for subclasses to implement
    protected abstract String getPageTitle();
    protected abstract Dimension getWindowSize();
    protected abstract void initializeComponents();
    protected abstract void setupLayout();
    protected abstract void setupPermissions();
    protected abstract void setupEventHandlers();
    
    // Common methods available to all subclasses
    protected final void performLogout() { /* ... */ }
    protected final String getCurrentUserRoleDisplay() { /* ... */ }
}
```
- **Benefits**: Enforces consistent initialization flow, prevents code duplication, ensures authorization setup

#### 7. **Factory Method Pattern**
- **Purpose**: Creates appropriate page instances based on user role without exposing instantiation logic
- **Implementation**: Static factory method in base class
```java
public static JFrame createPageForRole(AuthService authService) {
    String role = authService.getCurrentUser().getRole();
    switch (role) {
        case "PRINCIPAL": return new PrincipalPage(authService);
        case "TEACHER": return new TeacherPage(authService);
        case "PARENT": return new ParentPage(authService);
        default: throw new IllegalArgumentException("Unknown user role: " + role);
    }
}

// Usage in LoginWindow
JFrame mainPage = BaseAuthenticatedPage.createPageForRole(authService);
mainPage.setVisible(true);
```
- **Benefits**: Centralized page creation logic, easy to add new roles, type-safe instantiation

### Core Classes

1. **AuthorizationService** - Main authorization logic implementing Strategy pattern
2. **AuthUtil** - UI utility functions implementing Helper pattern
3. **AuthorizationDAO** - Database queries for authorization data
4. **AuthService** - Authentication service acting as Facade to authorization

### Permission Constants

```java
// User Management
PERM_CREATE_USERS = "CREATE_USERS"
PERM_CREATE_STUDENTS = "CREATE_STUDENTS"

// Content Management
PERM_CREATE_POSTS = "CREATE_POSTS"
PERM_COMMENT_POSTS = "COMMENT_POSTS"
PERM_LIKE_POSTS = "LIKE_POSTS"

// Student Management
PERM_UPDATE_STUDENTS = "UPDATE_STUDENTS"

// Viewing Permissions
PERM_VIEW_ALL_POSTS = "VIEW_ALL_POSTS"      // Principal only
PERM_VIEW_CLASS_POSTS = "VIEW_CLASS_POSTS"  // Teacher, Parent

// System Functions
PERM_MANAGE_ATTENDANCE = "MANAGE_ATTENDANCE"
PERM_VIEW_REPORTS = "VIEW_REPORTS"
PERM_SEND_MESSAGES = "SEND_MESSAGES"
PERM_MANAGE_SCHOOL = "MANAGE_SCHOOL"
```

## Usage Examples

### 1. Check Permission in UI Components

```java
// Basic permission check with error message
if (AuthUtil.checkPermissionWithMessage(authService, 
    AuthorizationService.PERM_CREATE_STUDENTS, "thêm học sinh")) {
    // User has permission, proceed with action
    addStudent();
}

// Check class access
if (AuthUtil.checkClassAccessWithMessage(authService, classId, "xem posts")) {
    // User can access this class
    showClassPosts(classId);
}
```

### 2. Setup UI Permissions

```java
private void setupPermissions() {
    // Check if user can manage students
    boolean canManageStudents = AuthUtil.canManageStudents(authService);
    
    // Enable/disable buttons based on permissions
    addButton.setEnabled(canManageStudents);
    updateButton.setEnabled(canManageStudents);
    deleteButton.setEnabled(canManageStudents);
    
    // Disable form fields if no permission
    if (!canManageStudents) {
        nameField.setEditable(false);
        dobField.setEditable(false);
        // Add explanatory tooltip
        nameField.setToolTipText("Bạn không có quyền chỉnh sửa thông tin học sinh");
    }
}
```

### 3. Service Layer Authorization

```java
public boolean updateStudent(Student student) {
    // Check if current user can update this student
    if (!authorizationService.canUpdateStudent(student.getId())) {
        throw new SecurityException("Không có quyền cập nhật học sinh này");
    }
    
    // Check if user can access the new class
    if (!authorizationService.canAccessClass(student.getClassId())) {
        throw new SecurityException("Không có quyền truy cập lớp này");
    }
    
    // Proceed with update
    return studentDAO.update(student);
}
```

### 4. Post Access Control

```java
// Check if user can view a specific post
if (authorizationService.canAccessPost(postId)) {
    // Show post content
    displayPost(post);
} else {
    // Show access denied message
    showAccessDeniedMessage();
}

// Get only accessible class IDs for current user
int[] accessibleClasses = authorizationService.getAccessibleClassIds();
List<Post> posts = postService.getPostsByClasses(accessibleClasses);
```

## Database Schema Considerations

The authorization system relies on the following database relationships:

```sql
-- Users belong to schools
users.school_id -> schools.id

-- Teachers are assigned to classes
classes.teacher_id -> users.id

-- Students belong to classes
students.class_id -> classes.id

-- Parents are linked to students
parents.user_id -> users.id
parents.student_id -> students.id

-- Posts belong to classes
posts.class_id -> classes.id
```

## Security Features

### 1. School Isolation
- Users can only access data within their assigned school
- Principals cannot manage users from other schools

### 2. Class-Level Access Control
- Teachers can only access their assigned classes
- Parents can only access classes where their children are enrolled

### 3. Role-Based Permissions
- Each role has a predefined set of permissions
- Permissions are checked at both UI and service levels

### 4. Data Filtering
- Database queries automatically filter based on user access rights
- No unauthorized data is returned to the application layer

## Integration Points

### 1. UI Components - Template Method Pattern
- All page classes extend `BaseAuthenticatedPage` which enforces consistent authorization flow:
  1. **Template Method**: `initializePage()` defines the algorithm structure
  2. **Hook Methods**: Subclasses implement `setupPermissions()`, `initializeComponents()`, etc.
  3. **Common Behavior**: Logout, window closing, role display handled by base class
  4. **Role Validation**: Each page can override `validateUserRole()` for additional security

```java
public class PrincipalPage extends BaseAuthenticatedPage {
    @Override
    protected void setupPermissions() {
        // Template method ensures this is called at the right time
        String roleDisplay = getCurrentUserRoleDisplay();
        // ... configure UI based on principal permissions
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.isPrincipal(); // Additional role validation
    }
}
```

### 2. Service Layer - Strategy + Facade Patterns
- **Facade**: `AuthService` provides simplified access to authorization
- **Strategy**: Different permission strategies per role
- **Validation**: Business logic validates permissions before database operations

```java
// Service layer authorization example
public boolean updateStudent(Student student) {
    // Strategy pattern - role-based permission check
    if (!authorizationService.canUpdateStudent(student.getId())) {
        throw new SecurityException(authorizationService.getUnauthorizedMessage("cập nhật học sinh"));
    }
    
    // Class access validation
    if (!authorizationService.canAccessClass(student.getClassId())) {
        throw new SecurityException("Không có quyền truy cập lớp này");
    }
    
    // Proceed with authorized operation
    return studentDAO.update(student);
}
```

### 3. Data Access Layer - Strategy Pattern
- **Context-Aware Queries**: DAO methods include user context for automatic filtering
- **School Isolation**: Queries automatically filter by user's school
- **Class-Level Security**: Access restricted to authorized classes only

```java
// DAO with authorization context
public List<Student> getStudentsForUser(User user) {
    switch (user.getRole()) {
        case "PRINCIPAL":
            return getStudentsBySchool(user.getSchoolId());
        case "TEACHER":
            return getStudentsByTeacherClass(user.getId());
        case "PARENT":
            return getStudentsByParent(user.getId());
        default:
            return Collections.emptyList();
    }
}
```

## Error Handling

### 1. UI Messages
- Localized Vietnamese error messages
- Clear explanations of why access was denied
- Role-specific guidance

### 2. Service Exceptions
```java
// Custom exception types
SecurityException - For permission violations
IllegalArgumentException - For invalid access attempts
```

### 3. Graceful Degradation
- UI components gracefully disable unavailable features
- No error dialogs for expected permission restrictions
- Clear visual indicators of user capabilities

## Best Practices

### 1. Always Use Dependency Injection for Authorization
```java
// Good - Proper dependency injection
public class StudentManagementPanel extends JPanel {
    private AuthService authService;
    private AuthorizationService authorizationService;
    
    public StudentManagementPanel(AuthService authService) {
        this.authService = authService;
        this.authorizationService = authService.getAuthorizationService();
        setupPermissions();
    }
}

// Bad - Direct instantiation breaks testability
public class BadExample extends JPanel {
    private AuthService authService = new AuthService(); // Hard to test/mock
}
```

### 2. Follow Template Method Pattern for UI Authorization
```java
// Good - Extend BaseAuthenticatedPage for consistent authorization
public class NewRolePage extends BaseAuthenticatedPage {
    public NewRolePage(AuthService authService) {
        super(authService); // Template method automatically called
    }
    
    @Override
    protected void setupPermissions() {
        // This method guaranteed to be called at the right time in initialization
        boolean canManage = AuthUtil.canManageStudents(authService);
        configureUIBasedOnPermissions(canManage);
    }
    
    @Override
    protected boolean validateUserRole() {
        return authService.hasRole("NEW_ROLE");
    }
}

// Bad - Direct JFrame extension without template structure
public class BadPage extends JFrame {
    // Scattered initialization, no guaranteed authorization setup
}
```

### 3. Use Utility Methods for Common Authorization Patterns
```java
// Good - Use AuthUtil for common patterns
if (AuthUtil.checkPermissionWithMessage(authService, 
        AuthorizationService.PERM_CREATE_STUDENTS, "thêm học sinh")) {
    addStudent();
}

// Bad - Manual permission checking with repeated code
if (!authService.getAuthorizationService().hasPermission(PERM_CREATE_STUDENTS)) {
    JOptionPane.showMessageDialog(this, "Không có quyền thêm học sinh", 
                                 "Lỗi", JOptionPane.ERROR_MESSAGE);
    return;
}
```

### 4. Implement Proper Separation of Concerns
```java
// Good - UI delegates to service layer
addButton.addActionListener(e -> {
    if (AuthUtil.canManageStudents(authService)) {
        studentService.addStudent(createStudentFromForm()); // Service handles business logic
    }
});

// Bad - UI contains business logic
addButton.addActionListener(e -> {
    if (authService.hasRole("PRINCIPAL") || authService.hasRole("TEACHER")) {
        // Business logic mixed with UI
        Student student = new Student();
        // ... complex business logic in UI
    }
});
```

### 5. Use Strategy Pattern for Role-Based Logic
```java
// Good - Strategy pattern for role-specific behavior
public List<Student> getAccessibleStudents() {
    return authorizationService.getStudentsForCurrentUser();
}

// Bad - Role checking scattered throughout code
public List<Student> getStudents() {
    if (authService.isPrincipal()) {
        return studentDAO.getAllStudents();
    } else if (authService.isTeacher()) {
        return studentDAO.getStudentsByTeacher(authService.getCurrentUser().getId());
    }
    // ... repeated role checking logic
}
```

### 6. Use Factory Method for Page Creation
```java
// Good - Use factory method for role-based page creation
JFrame mainPage = BaseAuthenticatedPage.createPageForRole(authService);
mainPage.setVisible(true);

// Bad - Manual role checking and instantiation
switch (authService.getCurrentUser().getRole()) {
    case "PRINCIPAL": new PrincipalPage(authService).setVisible(true); break;
    case "TEACHER": new TeacherPage(authService).setVisible(true); break;
    // Repetitive and error-prone
}
```

### 7. Implement Template Method Pattern Correctly
```java
// Good - Proper template method implementation
public abstract class BaseAuthenticatedPage extends JFrame {
    // Template method (final - cannot be overridden)
    private final void initializePage() {
        initializeWindowProperties();
        initializeComponents();     // Hook method
        setupLayout();             // Hook method
        setupPermissions();        // Hook method - ensures authorization setup
        setupEventHandlers();      // Hook method
        setupCommonWindowBehavior();
        loadInitialData();         // Hook method
    }
}

// Bad - No template structure, inconsistent initialization
public class InconsistentPage extends JFrame {
    // Constructor might forget to call setupPermissions()
    // No guaranteed order of initialization
}
```

### 8. Initialize Authorization Properly with Facade Pattern
```java
// Good - Proper initialization using facade
authService = new AuthService();
authService.initializeAuthorization(); // Facade handles complex initialization

// Bad - Manual management of dependencies
authService = new AuthService();
authorizationService = new AuthorizationService(authService);
authService.setAuthorizationService(authorizationService);
```

### 9. Handle Edge Cases Gracefully
```java
// Good - Defensive programming with proper error handling
public boolean canAccessStudent(int studentId) {
    if (!authService.isLoggedIn()) {
        return false;
    }
    
    User currentUser = authService.getCurrentUser();
    if (currentUser == null) {
        return false;
    }
    
    return authorizationService.canUpdateStudent(studentId);
}

// Bad - Assuming valid state
public boolean canAccessStudent(int studentId) {
    return authorizationService.canUpdateStudent(studentId); // NPE if not logged in
}
```

### 10. Use Permission Constants Instead of Magic Strings
```java
// Good - Use defined constants
if (authorizationService.hasPermission(AuthorizationService.PERM_CREATE_STUDENTS)) {
    // ...
}

// Bad - Magic strings prone to typos
if (authorizationService.hasPermission("CREATE_STUDENTS")) {
    // Typos not caught at compile time
}
```

### Design Pattern Benefits

#### **Maintainability**
- **Single Responsibility**: Each class has one clear purpose (AuthService for auth, AuthorizationService for permissions)
- **Open/Closed Principle**: Easy to add new roles without modifying existing code
- **Template Method**: Enforces consistent initialization and authorization setup across all pages
- **Factory Method**: Centralized page creation logic, easy to extend for new roles

#### **Testability**
- **Dependency Injection**: Services can be easily mocked for unit testing
- **Separation of Concerns**: Business logic can be tested independently of UI
- **Strategy Pattern**: Each role's permissions can be tested in isolation

#### **Scalability**
- **Strategy Pattern**: Adding new roles is simply adding new permission strategies
- **Facade Pattern**: Complex authorization logic hidden behind simple interface
- **Service Layer**: Business logic reusable across different UI frameworks

#### **Security**
- **Centralized Authorization**: All permission logic in one place reduces security gaps
- **Fail-Safe Defaults**: UI components disable features by default, enable only when authorized
- **Multi-Layer Validation**: Permissions checked at both UI and service layers

#### **User Experience**
- **Graceful Degradation**: Features disabled rather than hidden, with helpful tooltips
- **Localized Messages**: AuthUtil provides Vietnamese error messages appropriate to user context
- **Role-Aware UI**: Interface adapts to show user's role and capabilities

This authorization system ensures that each user type can only access and modify data appropriate to their role, maintaining security and data integrity throughout the kindergarten management application.
