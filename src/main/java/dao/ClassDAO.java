package dao;

import model.Class;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Class operations
 */
public class ClassDAO {
    
    /**
     * Create new class
     */
    public boolean create(Class clazz) {
        String sql = "INSERT INTO classes (name, school_id, teacher_id, grade_level, capacity) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clazz.getName());
            stmt.setInt(2, clazz.getSchoolId());
            
            if (clazz.getTeacherId() != null) {
                stmt.setInt(3, clazz.getTeacherId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.setString(4, clazz.getGradeLevel());
            stmt.setInt(5, clazz.getCapacity());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find class by ID with teacher and enrollment info
     */
    public Class findById(int id) {
        String sql = """
            SELECT c.*, 
                   u.username as teacher_name,
                   s.name as school_name,
                   COALESCE(student_count.enrollment, 0) as current_enrollment
            FROM classes c
            LEFT JOIN users u ON c.teacher_id = u.id
            LEFT JOIN schools s ON c.school_id = s.id
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            WHERE c.id = ?
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClass(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding class by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Find all classes with teacher and enrollment information
     */
    public List<Class> findAll() {
        List<Class> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   u.username as teacher_name,
                   s.name as school_name,
                   COALESCE(student_count.enrollment, 0) as current_enrollment
            FROM classes c
            LEFT JOIN users u ON c.teacher_id = u.id
            LEFT JOIN schools s ON c.school_id = s.id
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            ORDER BY c.grade_level, c.name
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                classes.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all classes: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * Find classes by school ID
     */
    public List<Class> findBySchoolId(int schoolId) {
        List<Class> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   u.username as teacher_name,
                   s.name as school_name,
                   COALESCE(student_count.enrollment, 0) as current_enrollment
            FROM classes c
            LEFT JOIN users u ON c.teacher_id = u.id
            LEFT JOIN schools s ON c.school_id = s.id
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            WHERE c.school_id = ?
            ORDER BY c.grade_level, c.name
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, schoolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding classes by school ID: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * Find classes by teacher ID
     */
    public List<Class> findByTeacherId(int teacherId) {
        List<Class> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   u.username as teacher_name,
                   s.name as school_name,
                   COALESCE(student_count.enrollment, 0) as current_enrollment
            FROM classes c
            LEFT JOIN users u ON c.teacher_id = u.id
            LEFT JOIN schools s ON c.school_id = s.id
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            WHERE c.teacher_id = ?
            ORDER BY c.grade_level, c.name
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding classes by teacher ID: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * Find available teachers (teachers without assigned class)
     */
    public List<Object[]> findAvailableTeachers() {
        List<Object[]> teachers = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username 
            FROM users u 
            WHERE u.role = 'TEACHER' 
            AND u.id NOT IN (SELECT teacher_id FROM classes WHERE teacher_id IS NOT NULL)
            ORDER BY u.username
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                teachers.add(new Object[]{rs.getInt("id"), rs.getString("username")});
            }
        } catch (SQLException e) {
            System.err.println("Error finding available teachers: " + e.getMessage());
        }
        return teachers;
    }
    
    /**
     * Find all teachers (for reassignment purposes)
     */
    public List<Object[]> findAllTeachers() {
        List<Object[]> teachers = new ArrayList<>();
        String sql = "SELECT id, username FROM users WHERE role = 'TEACHER' ORDER BY username";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                teachers.add(new Object[]{rs.getInt("id"), rs.getString("username")});
            }
        } catch (SQLException e) {
            System.err.println("Error finding all teachers: " + e.getMessage());
        }
        return teachers;
    }
    
    /**
     * Update existing class
     */
    public boolean update(Class clazz) {
        String sql = "UPDATE classes SET name = ?, teacher_id = ?, grade_level = ?, capacity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clazz.getName());
            
            if (clazz.getTeacherId() != null) {
                stmt.setInt(2, clazz.getTeacherId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            stmt.setString(3, clazz.getGradeLevel());
            stmt.setInt(4, clazz.getCapacity());
            stmt.setInt(5, clazz.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete class
     */
    public boolean delete(int id) {
        // First check if class has students
        String checkSql = "SELECT COUNT(*) FROM students WHERE class_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Cannot delete class: class has enrolled students");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking class students: " + e.getMessage());
            return false;
        }
        
        String sql = "DELETE FROM classes WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Assign teacher to class
     */
    public boolean assignTeacher(int classId, int teacherId) {
        String sql = "UPDATE classes SET teacher_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            stmt.setInt(2, classId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error assigning teacher to class: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove teacher from class
     */
    public boolean removeTeacher(int classId) {
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
     * Search classes by name
     */
    public List<Class> searchByName(String name) {
        List<Class> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   u.username as teacher_name,
                   s.name as school_name,
                   COALESCE(student_count.enrollment, 0) as current_enrollment
            FROM classes c
            LEFT JOIN users u ON c.teacher_id = u.id
            LEFT JOIN schools s ON c.school_id = s.id
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            WHERE LOWER(c.name) LIKE LOWER(?)
            ORDER BY c.grade_level, c.name
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching classes by name: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * Get class statistics
     */
    public Object[] getClassStatistics() {
        String sql = """
            SELECT 
                COUNT(*) as total_classes,
                COUNT(teacher_id) as classes_with_teachers,
                SUM(capacity) as total_capacity,
                SUM(COALESCE(student_count.enrollment, 0)) as total_enrollment
            FROM classes c
            LEFT JOIN (
                SELECT class_id, COUNT(*) as enrollment 
                FROM students 
                WHERE class_id IS NOT NULL 
                GROUP BY class_id
            ) student_count ON c.id = student_count.class_id
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("total_classes"),
                    rs.getInt("classes_with_teachers"),
                    rs.getInt("total_capacity"),
                    rs.getInt("total_enrollment")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getting class statistics: " + e.getMessage());
        }
        return new Object[]{0, 0, 0, 0};
    }
    
    /**
     * Map ResultSet to Class object
     */
    private Class mapResultSetToClass(ResultSet rs) throws SQLException {
        Class clazz = new Class();
        clazz.setId(rs.getInt("id"));
        clazz.setName(rs.getString("name"));
        clazz.setSchoolId(rs.getInt("school_id"));
        
        int teacherId = rs.getInt("teacher_id");
        if (!rs.wasNull()) {
            clazz.setTeacherId(teacherId);
        }
        
        clazz.setGradeLevel(rs.getString("grade_level"));
        clazz.setCapacity(rs.getInt("capacity"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            clazz.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set display fields
        clazz.setTeacherName(rs.getString("teacher_name"));
        clazz.setSchoolName(rs.getString("school_name"));
        clazz.setCurrentEnrollment(rs.getInt("current_enrollment"));
        
        return clazz;
    }
}
