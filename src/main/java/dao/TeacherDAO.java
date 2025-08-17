package dao;

import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Teacher-specific operations and class assignments
 * Works with existing schema where teachers are users with role='TEACHER'
 */
public class TeacherDAO {
    
    /**
     * Create teacher profile when a user with TEACHER role is created
     * In the current schema, this is just ensuring the user exists as a teacher
     */
    public boolean createTeacherProfile(int userId) {
        // In current schema, no separate teacher table exists
        // Teacher profile is managed through users table with role='TEACHER'
        // This method exists for compatibility but doesn't need to do anything
        return true;
    }
    
    /**
     * Delete teacher profile and all associated data
     */
    public boolean deleteTeacherProfile(int userId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Remove teacher from class assignments first
                String removeClassAssignments = "UPDATE classes SET teacher_id = NULL WHERE teacher_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(removeClassAssignments)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                
                // No separate teacher profile table to delete in current schema
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting teacher profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get teacher profile by user ID
     */
    public Map<String, Object> getTeacherProfile(int userId) {
        String sql = """
            SELECT u.id, u.username, u.role, u.created_at,
                   COUNT(c.id) as class_count,
                   COUNT(DISTINCT s.id) as student_count
            FROM users u
            LEFT JOIN classes c ON c.teacher_id = u.id
            LEFT JOIN students s ON s.class_id = c.id
            WHERE u.id = ? AND u.role = 'TEACHER'
            GROUP BY u.id, u.username, u.role, u.created_at
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("user_id", rs.getInt("id"));
                    profile.put("username", rs.getString("username"));
                    profile.put("role", rs.getString("role"));
                    profile.put("class_count", rs.getInt("class_count"));
                    profile.put("student_count", rs.getInt("student_count"));
                    profile.put("created_at", rs.getTimestamp("created_at"));
                    
                    return profile;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting teacher profile: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all teacher profiles with statistics
     */
    public List<Map<String, Object>> getAllTeacherProfiles() {
        List<Map<String, Object>> profiles = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.role, u.created_at,
                   COUNT(c.id) as class_count,
                   COUNT(DISTINCT s.id) as student_count
            FROM users u
            LEFT JOIN classes c ON c.teacher_id = u.id
            LEFT JOIN students s ON s.class_id = c.id
            WHERE u.role = 'TEACHER'
            GROUP BY u.id, u.username, u.role, u.created_at
            ORDER BY u.username
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("user_id", rs.getInt("id"));
                profile.put("username", rs.getString("username"));
                profile.put("role", rs.getString("role"));
                profile.put("class_count", rs.getInt("class_count"));
                profile.put("student_count", rs.getInt("student_count"));
                profile.put("created_at", rs.getTimestamp("created_at"));
                
                profiles.add(profile);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all teacher profiles: " + e.getMessage());
        }
        
        return profiles;
    }
    
    /**
     * Get classes assigned to a teacher
     */
    public List<Map<String, Object>> getTeacherClasses(int userId) {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = """
            SELECT c.id, c.name, c.school_id,
                   COUNT(s.id) as student_count
            FROM classes c
            LEFT JOIN students s ON s.class_id = c.id
            WHERE c.teacher_id = ?
            GROUP BY c.id, c.name, c.school_id
            ORDER BY c.name
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("id", rs.getInt("id"));
                    classInfo.put("name", rs.getString("name"));
                    classInfo.put("school_id", rs.getInt("school_id"));
                    classInfo.put("student_count", rs.getInt("student_count"));
                    
                    classes.add(classInfo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting teacher classes: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * Assign teacher to a class
     */
    public boolean assignTeacherToClass(int userId, int classId) {
        String sql = "UPDATE classes SET teacher_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, classId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error assigning teacher to class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove teacher from a class
     */
    public boolean removeTeacherFromClass(int classId) {
        String sql = "UPDATE classes SET teacher_id = NULL WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing teacher from class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get available classes (without assigned teacher)
     */
    public List<Map<String, Object>> getAvailableClasses() {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = """
            SELECT c.id, c.name, c.school_id,
                   COUNT(s.id) as student_count
            FROM classes c
            LEFT JOIN students s ON s.class_id = c.id
            WHERE c.teacher_id IS NULL
            GROUP BY c.id, c.name, c.school_id
            ORDER BY c.name
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("id", rs.getInt("id"));
                classInfo.put("name", rs.getString("name"));
                classInfo.put("school_id", rs.getInt("school_id"));
                classInfo.put("student_count", rs.getInt("student_count"));
                
                classes.add(classInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available classes: " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * Check if teacher exists
     */
    public boolean teacherExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = 'TEACHER'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if teacher exists: " + e.getMessage());
        }
        
        return false;
    }
}
