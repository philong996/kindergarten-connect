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

### Core Classes

1. **AuthorizationService** - Main authorization logic
2. **AuthUtil** - UI utility functions for permission checking
3. **AuthorizationDAO** - Database queries for authorization
4. **AuthService** - Enhanced with authorization integration

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

### 1. UI Components
- All management panels check permissions before enabling actions
- Form fields are disabled for users without edit permissions
- Buttons show/hide based on user capabilities

### 2. Service Layer
- Business logic validates permissions before database operations
- Throws security exceptions for unauthorized access attempts

### 3. Data Access Layer
- DAO methods include user context in queries
- Automatic filtering based on school and class access rights

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

### 1. Always Check Permissions
```java
// Good - Check permission before action
if (authUtil.hasPermission(PERM_CREATE_STUDENTS)) {
    createStudent();
}

// Bad - Assume user has permission
createStudent(); // Could fail with security exception
```

### 2. Use Utility Methods
```java
// Good - Use utility for common checks
AuthUtil.checkPermissionWithMessage(authService, permission, action);

// Bad - Manual permission checking
if (!authService.getAuthorizationService().hasPermission(permission)) {
    JOptionPane.showMessageDialog(...);
    return;
}
```

### 3. Initialize Authorization Early
```java
// In LoginWindow or main application initialization
authService = new AuthService();
authService.initializeAuthorization();
```

### 4. Handle Edge Cases
- Check for null users
- Validate class and student IDs
- Handle database connection failures gracefully

This authorization system ensures that each user type can only access and modify data appropriate to their role, maintaining security and data integrity throughout the kindergarten management application.
