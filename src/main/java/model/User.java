package model;

import java.time.LocalDateTime;

/**
 * User model class representing system users (Principal, Teacher, Parent)
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role; // PRINCIPAL, TEACHER, PARENT
    private int schoolId;
    private String schoolName; // For UI display - not stored in database
    private LocalDateTime createdAt;
    
    // Constructors
    public User() {}
    
    public User(String username, String password, String role, int schoolId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.schoolId = schoolId;
    }
    
    public User(int id, String username, String password, String role, int schoolId, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.schoolId = schoolId;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public int getSchoolId() { return schoolId; }
    public void setSchoolId(int schoolId) { this.schoolId = schoolId; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Helper methods
    public boolean isPrincipal() { return "PRINCIPAL".equals(role); }
    public boolean isTeacher() { return "TEACHER".equals(role); }
    public boolean isParent() { return "PARENT".equals(role); }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", schoolId=" + schoolId +
                '}';
    }
}
