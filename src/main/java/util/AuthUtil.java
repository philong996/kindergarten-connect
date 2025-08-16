package util;

import service.AuthService;
import service.AuthorizationService;
import javax.swing.JOptionPane;

/**
 * Utility class for authorization checks with UI integration
 */
public class AuthUtil {
    
    /**
     * Check permission and show error message if unauthorized
     * Returns true if authorized, false if not
     */
    public static boolean checkPermissionWithMessage(AuthService authService, String permission, String action) {
        AuthorizationService authzService = authService.getAuthorizationService();
        
        if (!authzService.hasPermission(permission)) {
            String message = authzService.getUnauthorizedMessage(action);
            JOptionPane.showMessageDialog(null, message, "Không có quyền truy cập", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Check class access permission and show error message if unauthorized
     */
    public static boolean checkClassAccessWithMessage(AuthService authService, int classId, String action) {
        AuthorizationService authzService = authService.getAuthorizationService();
        
        if (!authzService.canAccessClass(classId)) {
            String message = authzService.getUnauthorizedMessage(action);
            JOptionPane.showMessageDialog(null, message, "Không có quyền truy cập", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Check if current user can manage students
     */
    public static boolean canManageStudents(AuthService authService) {
        return authService.getAuthorizationService().hasPermission(AuthorizationService.PERM_UPDATE_STUDENTS);
    }
    
    /**
     * Check if current user can create posts
     */
    public static boolean canCreatePosts(AuthService authService) {
        return authService.getAuthorizationService().hasPermission(AuthorizationService.PERM_CREATE_POSTS);
    }
    
    /**
     * Check if current user can create users (admin function)
     */
    public static boolean canCreateUsers(AuthService authService) {
        return authService.getAuthorizationService().hasPermission(AuthorizationService.PERM_CREATE_USERS);
    }
    
    /**
     * Check if current user can view all posts (principal)
     */
    public static boolean canViewAllPosts(AuthService authService) {
        return authService.getAuthorizationService().hasPermission(AuthorizationService.PERM_VIEW_ALL_POSTS);
    }
    
    /**
     * Get user role display name in Vietnamese
     */
    public static String getRoleDisplayName(String role) {
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
    
    /**
     * Validate school access for operations
     */
    public static boolean validateSchoolAccess(AuthService authService, int schoolId, String action) {
        AuthorizationService authzService = authService.getAuthorizationService();
        
        if (!authzService.canAccessSchool(schoolId)) {
            String message = "Bạn không có quyền truy cập vào trường này để " + action;
            JOptionPane.showMessageDialog(null, message, "Không có quyền truy cập", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
