package model;

import java.time.LocalDateTime;

/**
 * Class model representing a kindergarten class
 */
public class Class {
    private int id;
    private String name;
    private int schoolId;
    private Integer teacherId; // Nullable - class might not have assigned teacher yet
    private String gradeLevel;
    private int capacity;
    private LocalDateTime createdAt;
    
    // Additional fields for display purposes
    private String teacherName; // For joining with users table
    private int currentEnrollment; // Current number of students enrolled
    private String schoolName; // For joining with schools table
    
    // Constructors
    public Class() {}
    
    public Class(String name, int schoolId, String gradeLevel, int capacity) {
        this.name = name;
        this.schoolId = schoolId;
        this.gradeLevel = gradeLevel;
        this.capacity = capacity;
    }
    
    public Class(int id, String name, int schoolId, Integer teacherId, String gradeLevel, int capacity, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.schoolId = schoolId;
        this.teacherId = teacherId;
        this.gradeLevel = gradeLevel;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getSchoolId() { return schoolId; }
    public void setSchoolId(int schoolId) { this.schoolId = schoolId; }
    
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Display fields
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public int getCurrentEnrollment() { return currentEnrollment; }
    public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
    // Computed properties
    public int getAvailableSpots() {
        return Math.max(0, capacity - currentEnrollment);
    }
    
    public boolean isFull() {
        return currentEnrollment >= capacity;
    }
    
    public double getCapacityUtilization() {
        if (capacity == 0) return 0.0;
        return (double) currentEnrollment / capacity * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %d/%d students", 
                           name, gradeLevel, currentEnrollment, capacity);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Class clazz = (Class) o;
        return id == clazz.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
