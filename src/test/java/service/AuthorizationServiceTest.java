package service;

import model.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Authorization System
 * Tests role-based permissions and access control
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorizationServiceTest {
    
    private AuthService authService;
    private AuthorizationService authorizationService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService();
        authService.initializeAuthorization();
        authorizationService = authService.getAuthorizationService();
    }
    
    @AfterEach
    void tearDown() {
        if (authService != null && authService.isLoggedIn()) {
            authService.logout();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Test Principal (Hiệu trưởng) Permissions")
    void testPrincipalPermissions() {
        // Login as principal
        boolean loginSuccess = authService.login("admin", "admin123");
        assertTrue(loginSuccess, "Principal should be able to login");
        
        User currentUser = authService.getCurrentUser();
        assertNotNull(currentUser, "Current user should not be null");
        assertEquals("PRINCIPAL", currentUser.getRole(), "User role should be PRINCIPAL");
        assertEquals("admin", currentUser.getUsername(), "Username should be admin");
        
        // Test all permissions that Principal should have
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_USERS), 
                  "Principal should be able to create users");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_STUDENTS), 
                  "Principal should be able to create students");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_POSTS), 
                  "Principal should be able to create posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_COMMENT_POSTS), 
                  "Principal should be able to comment on posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_UPDATE_STUDENTS), 
                  "Principal should be able to update students");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_ALL_POSTS), 
                  "Principal should be able to view all posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_CLASS_POSTS), 
                  "Principal should be able to view class posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_SCHOOL), 
                  "Principal should be able to manage school");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_ATTENDANCE), 
                  "Principal should be able to manage attendance");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_REPORTS), 
                  "Principal should be able to view reports");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_SEND_MESSAGES), 
                  "Principal should be able to send messages");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_LIKE_POSTS), 
                  "Principal should be able to like posts");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test Teacher (Giáo viên) Permissions")
    void testTeacherPermissions() {
        // Login as teacher
        boolean loginSuccess = authService.login("teacher1", "teacher123");
        assertTrue(loginSuccess, "Teacher should be able to login");
        
        User currentUser = authService.getCurrentUser();
        assertNotNull(currentUser, "Current user should not be null");
        assertEquals("TEACHER", currentUser.getRole(), "User role should be TEACHER");
        assertEquals("teacher1", currentUser.getUsername(), "Username should be teacher1");
        
        // Test permissions that Teacher should have
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_POSTS), 
                  "Teacher should be able to create posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_COMMENT_POSTS), 
                  "Teacher should be able to comment on posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_UPDATE_STUDENTS), 
                  "Teacher should be able to update students in their class");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_CLASS_POSTS), 
                  "Teacher should be able to view class posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_ATTENDANCE), 
                  "Teacher should be able to manage attendance");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_SEND_MESSAGES), 
                  "Teacher should be able to send messages");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_LIKE_POSTS), 
                  "Teacher should be able to like posts");
        
        // Test permissions that Teacher should NOT have
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_USERS), 
                   "Teacher should NOT be able to create users");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_STUDENTS), 
                   "Teacher should NOT be able to create students");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_ALL_POSTS), 
                   "Teacher should NOT be able to view all posts");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_SCHOOL), 
                   "Teacher should NOT be able to manage school");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_REPORTS), 
                   "Teacher should NOT be able to view reports");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test Parent (Phụ huynh) Permissions")
    void testParentPermissions() {
        // Login as parent
        boolean loginSuccess = authService.login("parent1", "parent123");
        assertTrue(loginSuccess, "Parent should be able to login");
        
        User currentUser = authService.getCurrentUser();
        assertNotNull(currentUser, "Current user should not be null");
        assertEquals("PARENT", currentUser.getRole(), "User role should be PARENT");
        assertEquals("parent1", currentUser.getUsername(), "Username should be parent1");
        
        // Test permissions that Parent should have
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_COMMENT_POSTS), 
                  "Parent should be able to comment on posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_LIKE_POSTS), 
                  "Parent should be able to like posts");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_CLASS_POSTS), 
                  "Parent should be able to view class posts of their children");
        assertTrue(authorizationService.hasPermission(AuthorizationService.PERM_SEND_MESSAGES), 
                  "Parent should be able to send messages");
        
        // Test permissions that Parent should NOT have
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_USERS), 
                   "Parent should NOT be able to create users");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_STUDENTS), 
                   "Parent should NOT be able to create students");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_POSTS), 
                   "Parent should NOT be able to create posts");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_UPDATE_STUDENTS), 
                   "Parent should NOT be able to update students");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_ALL_POSTS), 
                   "Parent should NOT be able to view all posts");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_SCHOOL), 
                   "Parent should NOT be able to manage school");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_MANAGE_ATTENDANCE), 
                   "Parent should NOT be able to manage attendance");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_REPORTS), 
                   "Parent should NOT be able to view reports");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Login and Logout Functionality")
    void testLoginLogout() {
        // Test successful login
        assertFalse(authService.isLoggedIn(), "Should not be logged in initially");
        
        boolean loginSuccess = authService.login("admin", "admin123");
        assertTrue(loginSuccess, "Login should succeed with valid credentials");
        assertTrue(authService.isLoggedIn(), "Should be logged in after successful login");
        
        User user = authService.getCurrentUser();
        assertNotNull(user, "Current user should not be null after login");
        assertEquals("admin", user.getUsername(), "Username should match");
        
        // Test logout
        authService.logout();
        assertFalse(authService.isLoggedIn(), "Should not be logged in after logout");
        assertNull(authService.getCurrentUser(), "Current user should be null after logout");
        
        // Test invalid login
        boolean invalidLogin = authService.login("invalid", "invalid");
        assertFalse(invalidLogin, "Login should fail with invalid credentials");
        assertFalse(authService.isLoggedIn(), "Should not be logged in after failed login");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test Access Control Without Login")
    void testAccessControlWithoutLogin() {
        // Ensure no one is logged in
        assertFalse(authService.isLoggedIn(), "Should not be logged in");
        
        // Test that all permissions are denied when not logged in
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_USERS), 
                   "Should not have any permissions when not logged in");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_POSTS), 
                   "Should not have any permissions when not logged in");
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_VIEW_ALL_POSTS), 
                   "Should not have any permissions when not logged in");
        
        // Test class and school access
        assertFalse(authorizationService.canAccessClass(1), 
                   "Should not be able to access any class when not logged in");
        assertFalse(authorizationService.canAccessSchool(1), 
                   "Should not be able to access any school when not logged in");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test School Access Control")
    void testSchoolAccess() {
        // Login as principal
        authService.login("admin", "admin123");
        
        // Principal should be able to access their school (school_id = 1 based on sample data)
        assertTrue(authorizationService.canAccessSchool(1), 
                  "Principal should be able to access their assigned school");
        
        // Test with different school (should be false since they're not assigned to it)
        assertFalse(authorizationService.canAccessSchool(999), 
                   "Principal should not be able to access schools they're not assigned to");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test Class Access Control")
    void testClassAccess() {
        // Login as teacher
        authService.login("teacher1", "teacher123");
        
        // Note: The actual class access will depend on database data
        // This test demonstrates the structure, but results may vary based on actual data
        
        // Get accessible classes for the teacher
        int[] accessibleClasses = authorizationService.getAccessibleClassIds();
        assertNotNull(accessibleClasses, "Accessible classes array should not be null");
        
        // Teacher should have access to at least their assigned class
        // The exact behavior depends on the database setup
        System.out.println("Teacher accessible classes: " + java.util.Arrays.toString(accessibleClasses));
    }
    
    @Test
    @Order(8)
    @DisplayName("Test Role-Based Dashboard Access")
    void testRoleBasedAccess() {
        // Test each role has appropriate role checks
        
        // Test Principal
        authService.login("admin", "admin123");
        assertTrue(authService.isPrincipal(), "admin should be identified as Principal");
        assertFalse(authService.isTeacher(), "admin should not be identified as Teacher");
        assertFalse(authService.isParent(), "admin should not be identified as Parent");
        authService.logout();
        
        // Test Teacher
        authService.login("teacher1", "teacher123");
        assertFalse(authService.isPrincipal(), "teacher1 should not be identified as Principal");
        assertTrue(authService.isTeacher(), "teacher1 should be identified as Teacher");
        assertFalse(authService.isParent(), "teacher1 should not be identified as Parent");
        authService.logout();
        
        // Test Parent
        authService.login("parent1", "parent123");
        assertFalse(authService.isPrincipal(), "parent1 should not be identified as Principal");
        assertFalse(authService.isTeacher(), "parent1 should not be identified as Teacher");
        assertTrue(authService.isParent(), "parent1 should be identified as Parent");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test Error Messages and Unauthorized Access")
    void testErrorMessages() {
        // Login as parent (limited permissions)
        authService.login("parent1", "parent123");
        
        // Test getting appropriate error messages
        String errorMessage = authorizationService.getUnauthorizedMessage("tạo tài khoản người dùng");
        assertNotNull(errorMessage, "Error message should not be null");
        assertTrue(errorMessage.contains("Phụ huynh"), "Error message should contain role in Vietnamese");
        assertTrue(errorMessage.contains("không có quyền"), "Error message should indicate lack of permission");
        
        System.out.println("Sample error message: " + errorMessage);
    }
    
    @Test
    @Order(10)
    @DisplayName("Test Invalid Role Handling")
    void testInvalidRole() {
        // This test would require creating a user with invalid role in the database
        // For now, we test the permission system's robustness
        
        // Test with null user (logged out state)
        authService.logout();
        assertFalse(authorizationService.hasPermission(AuthorizationService.PERM_CREATE_POSTS), 
                   "Should not have permissions when user is null");
        
        // Test permission checking doesn't crash with invalid inputs
        assertDoesNotThrow(() -> {
            authorizationService.hasPermission(null);
            authorizationService.hasPermission("");
            authorizationService.canAccessClass(-1);
            authorizationService.canAccessSchool(-1);
        }, "Permission methods should handle invalid inputs gracefully");
    }
}
