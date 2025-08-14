package dao;

import model.Student;
import util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student operations
 */
public class StudentDAO {
    
    /**
     * Create new student
     */
    public boolean create(Student student) {
        String sql = "INSERT INTO students (name, dob, class_id, address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getName());
            stmt.setDate(2, Date.valueOf(student.getDob()));
            stmt.setInt(3, student.getClassId());
            stmt.setString(4, student.getAddress());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating student: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find student by ID
     */
    public Student findById(int id) {
        String sql = "SELECT s.*, c.name as class_name FROM students s " +
                     "LEFT JOIN classes c ON s.class_id = c.id WHERE s.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Find all students
     */
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, c.name as class_name FROM students s " +
                     "LEFT JOIN classes c ON s.class_id = c.id ORDER BY s.name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all students: " + e.getMessage());
        }
        return students;
    }
    
    /**
     * Find students by class ID
     */
    public List<Student> findByClassId(int classId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, c.name as class_name FROM students s " +
                     "LEFT JOIN classes c ON s.class_id = c.id WHERE s.class_id = ? ORDER BY s.name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding students by class ID: " + e.getMessage());
        }
        return students;
    }
    
    /**
     * Search students by name
     */
    public List<Student> searchByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, c.name as class_name FROM students s " +
                     "LEFT JOIN classes c ON s.class_id = c.id " +
                     "WHERE LOWER(s.name) LIKE LOWER(?) ORDER BY s.name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching students by name: " + e.getMessage());
        }
        return students;
    }
    
    /**
     * Update student
     */
    public boolean update(Student student) {
        String sql = "UPDATE students SET name = ?, dob = ?, class_id = ?, address = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getName());
            stmt.setDate(2, Date.valueOf(student.getDob()));
            stmt.setInt(3, student.getClassId());
            stmt.setString(4, student.getAddress());
            stmt.setInt(5, student.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete student
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get student count by class
     */
    public int getStudentCountByClass(int classId) {
        String sql = "SELECT COUNT(*) FROM students WHERE class_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Helper method to map ResultSet to Student object
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setName(rs.getString("name"));
        
        Date dob = rs.getDate("dob");
        if (dob != null) {
            student.setDob(dob.toLocalDate());
        }
        
        student.setClassId(rs.getInt("class_id"));
        student.setAddress(rs.getString("address"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            student.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set class name if available from JOIN
        try {
            String className = rs.getString("class_name");
            student.setClassName(className);
        } catch (SQLException e) {
            // class_name might not be available in all queries
        }
        
        return student;
    }
}
