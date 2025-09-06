package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student model class
 */
public class Student {
    private int id;
    private String name;
    private LocalDate dob;
    private String gender; // MALE or FEMALE
    private int classId;
    private String address;
    private LocalDateTime createdAt;
    private byte[] profileImage; // Binary data for profile image
    
    // Additional fields for display
    private String className; // For joining with class table
    
    // Constructors
    public Student() {}
    
    public Student(String name, LocalDate dob, String gender, int classId, String address) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.classId = classId;
        this.address = address;
    }
    
    public Student(int id, String name, LocalDate dob, String gender, int classId, String address, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.classId = classId;
        this.address = address;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public byte[] getProfileImage() { return profileImage; }
    public void setProfileImage(byte[] profileImage) { this.profileImage = profileImage; }
    
    // Helper method to calculate age
    public int getAge() {
        if (dob != null) {
            return LocalDate.now().getYear() - dob.getYear();
        }
        return 0;
    }
    
    // Helper method to check gender
    public boolean isMale() {
        return "MALE".equals(gender);
    }
    
    public boolean isFemale() {
        return "FEMALE".equals(gender);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dob=" + dob +
                ", classId=" + classId +
                ", address='" + address + '\'' +
                '}';
    }
}
