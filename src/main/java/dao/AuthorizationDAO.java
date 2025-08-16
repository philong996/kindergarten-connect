package dao;

import util.DatabaseUtil;
import java.sql.*;

/**
 * DAO for authorization-related database queries
 */
public class AuthorizationDAO {
    
    /**
     * Get school ID for a given class
     */
    public int getClassSchoolId(int classId) {
        String sql = "SELECT school_id FROM classes WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("school_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting class school ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }
    
    /**
     * Check if a teacher is assigned to a specific class
     */
    public boolean isTeacherAssignedToClass(int teacherId, int classId) {
        String sql = "SELECT COUNT(*) FROM classes WHERE teacher_id = ? AND id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            stmt.setInt(2, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking teacher class assignment: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if a parent's child is in a specific class
     */
    public boolean isParentChildInClass(int parentUserId, int classId) {
        String sql = "SELECT COUNT(*) FROM students s " +
                     "JOIN parents p ON s.id = p.student_id " +
                     "WHERE p.user_id = ? AND s.class_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parentUserId);
            stmt.setInt(2, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking parent child in class: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get the class ID for a specific student
     */
    public int getStudentClassId(int studentId) {
        String sql = "SELECT class_id FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("class_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student class ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }
    
    /**
     * Get all class IDs that a teacher is assigned to
     */
    public int[] getTeacherClassIds(int teacherId) {
        String sql = "SELECT id FROM classes WHERE teacher_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Use ArrayList to collect results
                java.util.List<Integer> classIdsList = new java.util.ArrayList<>();
                
                while (rs.next()) {
                    classIdsList.add(rs.getInt("id"));
                }
                
                // Convert to array
                return classIdsList.stream().mapToInt(Integer::intValue).toArray();
            }
        } catch (SQLException e) {
            System.err.println("Error getting teacher class IDs: " + e.getMessage());
        }
        return new int[0]; // Return empty array if error
    }
    
    /**
     * Get all class IDs where a parent has children
     */
    public int[] getParentChildrenClassIds(int parentUserId) {
        String sql = "SELECT DISTINCT s.class_id FROM students s " +
                     "JOIN parents p ON s.id = p.student_id " +
                     "WHERE p.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parentUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Use ArrayList to collect results
                java.util.List<Integer> classIdsList = new java.util.ArrayList<>();
                
                while (rs.next()) {
                    classIdsList.add(rs.getInt("class_id"));
                }
                
                // Convert to array
                return classIdsList.stream().mapToInt(Integer::intValue).toArray();
            }
        } catch (SQLException e) {
            System.err.println("Error getting parent children class IDs: " + e.getMessage());
        }
        return new int[0]; // Return empty array if error
    }
    
    /**
     * Check if a post belongs to a class that the user can access
     */
    public boolean canUserAccessPost(int userId, String userRole, int postId) {
        if ("PRINCIPAL".equals(userRole)) {
            // Principal can access all posts in their school
            return canPrincipalAccessPost(userId, postId);
        } else if ("TEACHER".equals(userRole)) {
            // Teacher can access posts in their assigned classes
            return canTeacherAccessPost(userId, postId);
        } else if ("PARENT".equals(userRole)) {
            // Parent can access posts in their children's classes
            return canParentAccessPost(userId, postId);
        }
        return false;
    }
    
    private boolean canPrincipalAccessPost(int principalUserId, int postId) {
        String sql = "SELECT COUNT(*) FROM posts p " +
                     "JOIN classes c ON p.class_id = c.id " +
                     "JOIN users u ON u.id = ? " +
                     "WHERE p.id = ? AND c.school_id = u.school_id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, principalUserId);
            stmt.setInt(2, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking principal post access: " + e.getMessage());
        }
        return false;
    }
    
    private boolean canTeacherAccessPost(int teacherUserId, int postId) {
        String sql = "SELECT COUNT(*) FROM posts p " +
                     "JOIN classes c ON p.class_id = c.id " +
                     "WHERE p.id = ? AND c.teacher_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            stmt.setInt(2, teacherUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking teacher post access: " + e.getMessage());
        }
        return false;
    }
    
    private boolean canParentAccessPost(int parentUserId, int postId) {
        String sql = "SELECT COUNT(*) FROM posts p " +
                     "JOIN classes c ON p.class_id = c.id " +
                     "JOIN students s ON s.class_id = c.id " +
                     "JOIN parents pr ON pr.student_id = s.id " +
                     "WHERE p.id = ? AND pr.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            stmt.setInt(2, parentUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking parent post access: " + e.getMessage());
        }
        return false;
    }
}
