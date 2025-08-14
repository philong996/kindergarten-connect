package model;

import java.time.LocalDateTime;

/**
 * Parent model class
 */
public class Parent {
    private int id;
    private int userId;
    private String name;
    private int studentId;
    private String relationship;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    
    // Additional fields for display
    private String studentName;
    
    // Constructors
    public Parent() {}
    
    public Parent(int userId, String name, int studentId, String relationship, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
    }
    
    // Full constructor
    public Parent(int id, int userId, String name, int studentId, String relationship, 
                  String phone, String email, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    @Override
    public String toString() {
        return "Parent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentId=" + studentId +
                ", relationship='" + relationship + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
