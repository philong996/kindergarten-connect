package service;

import dao.UserDAO;
import dao.AuthorizationDAO;
import model.User;
import java.util.List;
import java.util.Arrays;

/**
 * Authorization service for managing user permissions and access control
 */
public class AuthorizationService {
    private AuthService authService;
    private UserDAO userDAO;
    private AuthorizationDAO authDAO;
    
    // Define permissions as constants
    public static final String PERM_CREATE_USERS = "CREATE_USERS";
    public static final String PERM_CREATE_STUDENTS = "CREATE_STUDENTS";
    public static final String PERM_CREATE_POSTS = "CREATE_POSTS";
    public static final String PERM_COMMENT_POSTS = "COMMENT_POSTS";
    public static final String PERM_LIKE_POSTS = "LIKE_POSTS";
    public static final String PERM_UPDATE_STUDENTS = "UPDATE_STUDENTS";
    public static final String PERM_VIEW_ALL_POSTS = "VIEW_ALL_POSTS";
    public static final String PERM_VIEW_CLASS_POSTS = "VIEW_CLASS_POSTS";
    public static final String PERM_MANAGE_ATTENDANCE = "MANAGE_ATTENDANCE";
    public static final String PERM_VIEW_REPORTS = "VIEW_REPORTS";
    public static final String PERM_SEND_MESSAGES = "SEND_MESSAGES";
    public static final String PERM_MANAGE_SCHOOL = "MANAGE_SCHOOL";
    
    public AuthorizationService(AuthService authService) {
        this.authService = authService;
        this.userDAO = new UserDAO();
        this.authDAO = new AuthorizationDAO();
    }
    
    /**
     * Check if current user has a specific permission
     */
    public boolean hasPermission(String permission) {
        if (!authService.isLoggedIn()) {
            return false;
        }
        
        User currentUser = authService.getCurrentUser();
        return hasPermissionForRole(currentUser.getRole(), permission);
    }
    
    /**
     * Check if a role has a specific permission
     */
    private boolean hasPermissionForRole(String role, String permission) {
        switch (role) {
            case "PRINCIPAL":
                return getPrincipalPermissions().contains(permission);
            case "TEACHER":
                return getTeacherPermissions().contains(permission);
            case "PARENT":
                return getParentPermissions().contains(permission);
            default:
                return false;
        }
    }
    
    /**
     * Get all permissions for Principal (School Admin)
     * Hiệu trưởng có full quyền: Tạo account phụ huynh/ giáo viên, học sinh, tạo post, inbox….
     */
    private List<String> getPrincipalPermissions() {
        return Arrays.asList(
            PERM_CREATE_USERS,      // Tạo account phụ huynh/giáo viên
            PERM_CREATE_STUDENTS,   // Tạo học sinh
            PERM_CREATE_POSTS,      // Tạo post
            PERM_COMMENT_POSTS,     // Bình luận
            PERM_LIKE_POSTS,        // Like post
            PERM_UPDATE_STUDENTS,   // Update thông tin học sinh
            PERM_VIEW_ALL_POSTS,    // Xem tất cả posts
            PERM_VIEW_CLASS_POSTS,  // Xem posts của lớp
            PERM_MANAGE_ATTENDANCE, // Quản lý điểm danh
            PERM_VIEW_REPORTS,      // Xem báo cáo
            PERM_SEND_MESSAGES,     // Gửi tin nhắn
            PERM_MANAGE_SCHOOL      // Quản lý trường
        );
    }
    
    /**
     * Get all permissions for Teacher
     * Giáo viên có quyền tạo post thông tin học sinh, bình luận, update thông tin học sinh
     */
    private List<String> getTeacherPermissions() {
        return Arrays.asList(
            PERM_CREATE_POSTS,      // Tạo post thông tin học sinh
            PERM_COMMENT_POSTS,     // Bình luận
            PERM_UPDATE_STUDENTS,   // Update thông tin học sinh
            PERM_VIEW_CLASS_POSTS,  // Xem posts của lớp mình dạy
            PERM_MANAGE_ATTENDANCE, // Điểm danh học sinh
            PERM_SEND_MESSAGES,     // Gửi tin nhắn với phụ huynh
            PERM_LIKE_POSTS         // Like posts
        );
    }
    
    /**
     * Get all permissions for Parent
     * Phụ huynh có quyền bình luận, like post của giáo viên
     */
    private List<String> getParentPermissions() {
        return Arrays.asList(
            PERM_COMMENT_POSTS,     // Bình luận
            PERM_LIKE_POSTS,        // Like post
            PERM_VIEW_CLASS_POSTS,  // Xem posts của lớp con mình
            PERM_SEND_MESSAGES      // Gửi tin nhắn với giáo viên
        );
    }
    
    /**
     * Check if current user can access a specific class
     * For teachers: can only access their assigned class
     * For parents: can only access their child's class
     * For principals: can access all classes in their school
     */
    public boolean canAccessClass(int classId) {
        if (!authService.isLoggedIn()) {
            return false;
        }
        
        User currentUser = authService.getCurrentUser();
        
        switch (currentUser.getRole()) {
            case "PRINCIPAL":
                // Principal can access all classes in their school
                return canAccessSchool(getClassSchoolId(classId));
                
            case "TEACHER":
                // Teacher can only access their assigned class
                return isTeacherAssignedToClass(currentUser.getId(), classId);
                
            case "PARENT":
                // Parent can only access their child's class
                return isParentChildInClass(currentUser.getId(), classId);
                
            default:
                return false;
        }
    }
    
    /**
     * Check if current user can access a specific school
     */
    public boolean canAccessSchool(int schoolId) {
        if (!authService.isLoggedIn()) {
            return false;
        }
        
        User currentUser = authService.getCurrentUser();
        return currentUser.getSchoolId() == schoolId;
    }
    
    /**
     * Check if current user can manage another user
     * Only principals can manage users in their school
     */
    public boolean canManageUser(int targetUserId) {
        if (!hasPermission(PERM_CREATE_USERS)) {
            return false;
        }
        
        User currentUser = authService.getCurrentUser();
        User targetUser = userDAO.findById(targetUserId);
        
        if (targetUser == null) {
            return false;
        }
        
        // Principal can manage users in their school
        return currentUser.getSchoolId() == targetUser.getSchoolId();
    }
    
    /**
     * Check if current user can create posts for a specific class
     */
    public boolean canCreatePostForClass(int classId) {
        if (!hasPermission(PERM_CREATE_POSTS)) {
            return false;
        }
        
        return canAccessClass(classId);
    }
    
    /**
     * Check if current user can view posts from a specific class
     */
    public boolean canViewClassPosts(int classId) {
        if (!hasPermission(PERM_VIEW_CLASS_POSTS) && !hasPermission(PERM_VIEW_ALL_POSTS)) {
            return false;
        }
        
        // If user has VIEW_ALL_POSTS permission (Principal), they can view any class
        if (hasPermission(PERM_VIEW_ALL_POSTS)) {
            return canAccessSchool(getClassSchoolId(classId));
        }
        
        // Otherwise, check specific class access
        return canAccessClass(classId);
    }
    
    /**
     * Check if current user can update student information
     */
    public boolean canUpdateStudent(int studentId) {
        if (!hasPermission(PERM_UPDATE_STUDENTS)) {
            return false;
        }
        
        int studentClassId = getStudentClassId(studentId);
        return canAccessClass(studentClassId);
    }
    
    /**
     * Get error message for unauthorized access
     */
    public String getUnauthorizedMessage(String action) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return "Bạn cần đăng nhập để thực hiện hành động này.";
        }
        
        String role = getRoleDisplayName(currentUser.getRole());
        return String.format("Bạn (%s) không có quyền %s.", role, action);
    }
    
    /**
     * Get display name for role in Vietnamese
     */
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "PRINCIPAL":
                return "Hiệu trưởng";
            case "TEACHER":
                return "Giáo viên";
            case "PARENT":
                return "Phụ huynh";
            default:
                return role;
        }
    }
    
    // Helper methods using AuthorizationDAO
    
    private int getClassSchoolId(int classId) {
        return authDAO.getClassSchoolId(classId);
    }
    
    private boolean isTeacherAssignedToClass(int teacherId, int classId) {
        return authDAO.isTeacherAssignedToClass(teacherId, classId);
    }
    
    private boolean isParentChildInClass(int parentUserId, int classId) {
        return authDAO.isParentChildInClass(parentUserId, classId);
    }
    
    private int getStudentClassId(int studentId) {
        return authDAO.getStudentClassId(studentId);
    }
    
    /**
     * Check if current user can access a specific post
     */
    public boolean canAccessPost(int postId) {
        if (!authService.isLoggedIn()) {
            return false;
        }
        
        User currentUser = authService.getCurrentUser();
        return authDAO.canUserAccessPost(currentUser.getId(), currentUser.getRole(), postId);
    }
    
    /**
     * Get all class IDs that current user can access
     */
    public int[] getAccessibleClassIds() {
        if (!authService.isLoggedIn()) {
            return new int[0];
        }
        
        User currentUser = authService.getCurrentUser();
        
        switch (currentUser.getRole()) {
            case "TEACHER":
                return authDAO.getTeacherClassIds(currentUser.getId());
            case "PARENT":
                return authDAO.getParentChildrenClassIds(currentUser.getId());
            case "PRINCIPAL":
                // Principal can access all classes in their school
                // This would need a separate method to get all classes in school
                return new int[0]; // TODO: Implement getAllSchoolClassIds
            default:
                return new int[0];
        }
    }
}
