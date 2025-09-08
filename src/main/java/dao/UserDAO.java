package dao;

import model.User;
import util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations
 */
public class UserDAO {
    
    /**
     * Authenticate user login
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Find user by ID
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get all users by role
     */
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Get users by role as Map for UI dropdowns
     */
    public List<java.util.Map<String, Object>> getUsersByRole(String role) {
        List<java.util.Map<String, Object>> users = new ArrayList<>();
        String sql;
        
        if ("parent".equalsIgnoreCase(role)) {
            // For parents, join with parents table to get the name
            // Use subquery to get the first parent name to avoid duplicates
            sql = """
                SELECT u.id, u.username, 
                       CASE 
                           WHEN u.role = 'PARENT' THEN COALESCE(
                               (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                               u.username
                           )
                           ELSE u.username
                       END as full_name, 
                       u.role 
                FROM users u 
                WHERE u.role = ? 
                ORDER BY CASE 
                           WHEN u.role = 'PARENT' THEN COALESCE(
                               (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                               u.username
                           )
                           ELSE u.username
                       END
                """;
        } else {
            // For teachers and other roles, use username as display name
            sql = """
                SELECT u.id, u.username, u.username as full_name, u.role 
                FROM users u 
                WHERE u.role = ? 
                ORDER BY u.username
                """;
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> user = new java.util.HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    user.put("full_name", rs.getString("full_name"));
                    user.put("role", rs.getString("role"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Get user by ID as Map for role checking
     */
    public java.util.Map<String, Object> getUserById(int id) {
        String sql;
        
        // Join with parents table to get the name if it's a parent
        // Use LIMIT 1 to ensure we get only one result even if parent has multiple children
        sql = """
            SELECT u.id, u.username, 
                   CASE 
                       WHEN u.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                           u.username
                       )
                       ELSE u.username
                   END as full_name, 
                   u.role 
            FROM users u 
            WHERE u.id = ?
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.util.Map<String, Object> user = new java.util.HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    user.put("full_name", rs.getString("full_name"));
                    user.put("role", rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create new user
     */
    public boolean create(User user) {
        String sql = "INSERT INTO users (username, password, role, school_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getSchoolId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update existing user
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, school_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getSchoolId());
            stmt.setInt(5, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete user
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, username";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Get all users with school names for UI display
     */
    public List<User> findAllWithSchoolNames() {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT u.*, s.name as school_name 
            FROM users u 
            LEFT JOIN schools s ON u.school_id = s.id 
            ORDER BY u.role, u.username
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                // Set school name if available
                String schoolName = rs.getString("school_name");
                if (schoolName != null) {
                    user.setSchoolName(schoolName);
                }
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users with school names: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Get all users with school names and child names (for parent users) for UI display
     */
    public List<User> findAllWithSchoolNamesAndChildren() {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT u.*, s.name as school_name, 
                   CASE 
                       WHEN u.role = 'PARENT' THEN st.name
                       ELSE NULL 
                   END as child_name
            FROM users u 
            LEFT JOIN schools s ON u.school_id = s.id 
            LEFT JOIN parents p ON u.id = p.user_id AND u.role = 'PARENT'
            LEFT JOIN students st ON p.student_id = st.id
            ORDER BY u.role, u.username
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                // Set school name if available
                String schoolName = rs.getString("school_name");
                if (schoolName != null) {
                    user.setSchoolName(schoolName);
                }
                // Set child name if available (for parent users)
                String childName = rs.getString("child_name");
                if (childName != null) {
                    user.setChildName(childName);
                }
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users with school names and children: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Get all schools for dropdown options
     */
    public List<Object[]> findAllSchools() {
        List<Object[]> schools = new ArrayList<>();
        String sql = "SELECT id, name FROM schools ORDER BY name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                schools.add(new Object[]{rs.getInt("id"), rs.getString("name")});
            }
        } catch (SQLException e) {
            System.err.println("Error finding all schools: " + e.getMessage());
        }
        return schools;
    }
    
    /**
     * Helper method to map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setSchoolId(rs.getInt("school_id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return user;
    }
}
