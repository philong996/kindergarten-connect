package dao;

import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Parent-specific operations and parent-student relationships
 * Works with existing schema where parents table has user_id and student_id
 */
public class ParentDAO {
    
    /**
     * Create parent profile when a user with PARENT role is created
     * This creates a basic entry in the parents table
     */
    public boolean createParentProfile(int userId) {
        String sql = "INSERT INTO parents (user_id, name, relationship, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Get username from users table to use as initial name
            String getUsername = "SELECT username FROM users WHERE id = ?";
            String parentName = "Unknown";
            try (PreparedStatement userStmt = conn.prepareStatement(getUsername)) {
                userStmt.setInt(1, userId);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        parentName = rs.getString("username");
                    }
                }
            }
            
            stmt.setInt(1, userId);
            stmt.setString(2, parentName);
            stmt.setString(3, "Parent"); // Default relationship
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating parent profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete parent profile and all associated data
     */
    public boolean deleteParentProfile(int userId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Delete all parent records for this user
                String deleteProfile = "DELETE FROM parents WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteProfile)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting parent profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get parent profile by user ID with contact info and children count
     */
    public Map<String, Object> getParentProfile(int userId) {
        String sql = """
            SELECT u.id, u.username, u.role, u.created_at,
                   COUNT(p.student_id) as children_count,
                   MAX(p.phone) as phone,
                   MAX(p.email) as email
            FROM users u
            LEFT JOIN parents p ON p.user_id = u.id
            WHERE u.id = ? AND u.role = 'PARENT'
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
                    profile.put("phone", rs.getString("phone"));
                    profile.put("email", rs.getString("email"));
                    profile.put("children_count", rs.getInt("children_count"));
                    profile.put("created_at", rs.getTimestamp("created_at"));
                    
                    return profile;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting parent profile: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all parent profiles with statistics
     */
    public List<Map<String, Object>> getAllParentProfiles() {
        List<Map<String, Object>> profiles = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.role, u.created_at,
                   COUNT(p.student_id) as children_count,
                   MAX(p.phone) as phone,
                   MAX(p.email) as email
            FROM users u
            LEFT JOIN parents p ON p.user_id = u.id
            WHERE u.role = 'PARENT'
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
                profile.put("phone", rs.getString("phone"));
                profile.put("email", rs.getString("email"));
                profile.put("children_count", rs.getInt("children_count"));
                profile.put("created_at", rs.getTimestamp("created_at"));
                
                profiles.add(profile);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all parent profiles: " + e.getMessage());
        }
        
        return profiles;
    }
    
    /**
     * Update parent contact information
     */
    public boolean updateParentContact(int userId, String phone, String email) {
        // Update all parent records for this user with the new contact info
        String sql = "UPDATE parents SET phone = ?, email = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, phone);
            stmt.setString(2, email);
            stmt.setInt(3, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating parent contact: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get children of a parent
     */
    public List<Map<String, Object>> getParentChildren(int userId) {
        List<Map<String, Object>> children = new ArrayList<>();
        String sql = """
            SELECT s.id, s.name, s.dob,
                   c.name as class_name,
                   p.relationship
            FROM students s
            JOIN parents p ON p.student_id = s.id
            LEFT JOIN classes c ON s.class_id = c.id
            WHERE p.user_id = ?
            ORDER BY s.name
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> child = new HashMap<>();
                    child.put("id", rs.getInt("id"));
                    child.put("name", rs.getString("name"));
                    child.put("dob", rs.getDate("dob"));
                    child.put("class_name", rs.getString("class_name"));
                    child.put("relationship", rs.getString("relationship"));
                    
                    children.add(child);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting parent children: " + e.getMessage());
        }
        
        return children;
    }
    
    /**
     * Get quick statistics for a specific child
     */
    public Map<String, Object> getChildQuickStats(int studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get attendance statistics for the current month
        String attendanceSql = """
            SELECT 
                COUNT(*) as total_days,
                SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_days,
                SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_days,
                SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_days
            FROM attendance 
            WHERE student_id = ? 
            AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
            AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(attendanceSql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_days", rs.getInt("total_days"));
                    stats.put("present_days", rs.getInt("present_days"));
                    stats.put("absent_days", rs.getInt("absent_days"));
                    stats.put("late_days", rs.getInt("late_days"));
                    
                    // Calculate attendance percentage
                    int totalDays = rs.getInt("total_days");
                    int presentDays = rs.getInt("present_days");
                    double attendanceRate = totalDays > 0 ? (double) presentDays / totalDays * 100 : 0;
                    stats.put("attendance_rate", Math.round(attendanceRate * 100.0) / 100.0);
                } else {
                    // Default values if no attendance records
                    stats.put("total_days", 0);
                    stats.put("present_days", 0);
                    stats.put("absent_days", 0);
                    stats.put("late_days", 0);
                    stats.put("attendance_rate", 0.0);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting child attendance stats: " + e.getMessage());
            // Return default values on error
            stats.put("total_days", 0);
            stats.put("present_days", 0);
            stats.put("absent_days", 0);
            stats.put("late_days", 0);
            stats.put("attendance_rate", 0.0);
        }
        
        // Get physical development record count
        String physicalDevSql = """
            SELECT COUNT(*) as record_count
            FROM physical_development_records 
            WHERE student_id = ?
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(physicalDevSql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("development_records", rs.getInt("record_count"));
                } else {
                    stats.put("development_records", 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting child development stats: " + e.getMessage());
            stats.put("development_records", 0);
        }
        
        return stats;
    }
    
    /**
     * Get aggregated quick statistics for all children of a parent
     */
    public Map<String, Object> getParentChildrenOverview(int userId) {
        Map<String, Object> overview = new HashMap<>();
        
        // Get children count
        List<Map<String, Object>> children = getParentChildren(userId);
        overview.put("total_children", children.size());
        
        if (children.isEmpty()) {
            overview.put("avg_attendance_rate", 0.0);
            overview.put("total_development_records", 0);
            overview.put("children_with_perfect_attendance", 0);
            return overview;
        }
        
        double totalAttendanceRate = 0;
        int totalDevelopmentRecords = 0;
        int childrenWithPerfectAttendance = 0;
        
        for (Map<String, Object> child : children) {
            int studentId = (Integer) child.get("id");
            Map<String, Object> childStats = getChildQuickStats(studentId);
            
            double attendanceRate = (Double) childStats.get("attendance_rate");
            totalAttendanceRate += attendanceRate;
            
            int devRecords = (Integer) childStats.get("development_records");
            totalDevelopmentRecords += devRecords;
            
            if (attendanceRate >= 100.0) {
                childrenWithPerfectAttendance++;
            }
        }
        
        // Calculate averages
        overview.put("avg_attendance_rate", Math.round(totalAttendanceRate / children.size() * 100.0) / 100.0);
        overview.put("total_development_records", totalDevelopmentRecords);
        overview.put("children_with_perfect_attendance", childrenWithPerfectAttendance);
        
        return overview;
    }
    
    /**
     * Add parent-student relationship
     */
    public boolean addParentStudentRelationship(int parentId, int studentId, String relationship) {
        // First check if parent profile exists, if not create it
        if (!parentExists(parentId)) {
            createParentProfile(parentId);
        }
        
        String sql = "INSERT INTO parents (user_id, name, student_id, relationship, phone, email, created_at) VALUES (?, ?, ?, ?, NULL, NULL, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Get parent name from users table
            String getUsername = "SELECT username FROM users WHERE id = ?";
            String parentName = "Unknown";
            try (PreparedStatement userStmt = conn.prepareStatement(getUsername)) {
                userStmt.setInt(1, parentId);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        parentName = rs.getString("username");
                    }
                }
            }
            
            stmt.setInt(1, parentId);
            stmt.setString(2, parentName);
            stmt.setInt(3, studentId);
            stmt.setString(4, relationship);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding parent-student relationship: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove parent-student relationship
     */
    public boolean removeParentStudentRelationship(int parentId, int studentId) {
        String sql = "DELETE FROM parents WHERE user_id = ? AND student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parentId);
            stmt.setInt(2, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing parent-student relationship: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get parents of a student
     */
    public List<Map<String, Object>> getStudentParents(int studentId) {
        List<Map<String, Object>> parents = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, p.phone, p.email, 
                   p.relationship, p.name
            FROM users u
            JOIN parents p ON p.user_id = u.id
            WHERE p.student_id = ?
            ORDER BY p.relationship
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> parent = new HashMap<>();
                    parent.put("id", rs.getInt("id"));
                    parent.put("username", rs.getString("username"));
                    parent.put("name", rs.getString("name"));
                    parent.put("phone", rs.getString("phone"));
                    parent.put("email", rs.getString("email"));
                    parent.put("relationship", rs.getString("relationship"));
                    
                    parents.add(parent);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student parents: " + e.getMessage());
        }
        
        return parents;
    }
    
    /**
     * Check if parent exists
     */
    public boolean parentExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = 'PARENT'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if parent exists: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if parent-student relationship exists
     */
    public boolean relationshipExists(int parentId, int studentId) {
        String sql = "SELECT COUNT(*) FROM parents WHERE user_id = ? AND student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parentId);
            stmt.setInt(2, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking parent-student relationship: " + e.getMessage());
        }
        
        return false;
    }
}
