package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Attendance model class
 */
public class Attendance {
    private int id;
    private int studentId;
    private LocalDate date;
    private String status; // PRESENT, ABSENT, LATE
    private LocalTime checkInTime;
    private LocalDateTime createdAt;
    
    // New fields for enhanced attendance tracking
    private LocalTime lateArrivalTime;
    private String excuseReason;
    
    // Additional fields for display
    private String studentName;
    
    // Constructors
    public Attendance() {}
    
    public Attendance(int studentId, LocalDate date, String status, LocalTime checkInTime) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.checkInTime = checkInTime;
    }
    
    // Enhanced constructor with new fields
    public Attendance(int studentId, LocalDate date, String status, LocalTime checkInTime, 
                     LocalTime lateArrivalTime, String excuseReason) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.checkInTime = checkInTime;
        this.lateArrivalTime = lateArrivalTime;
        this.excuseReason = excuseReason;
    }
    
    // Full constructor
    public Attendance(int id, int studentId, LocalDate date, String status, 
                     LocalTime checkInTime, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.checkInTime = checkInTime;
        this.createdAt = createdAt;
    }
    
    // Enhanced full constructor with all fields
    public Attendance(int id, int studentId, LocalDate date, String status, 
                     LocalTime checkInTime, LocalTime lateArrivalTime, String excuseReason,
                     LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.checkInTime = checkInTime;
        this.lateArrivalTime = lateArrivalTime;
        this.excuseReason = excuseReason;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public LocalTime getLateArrivalTime() { return lateArrivalTime; }
    public void setLateArrivalTime(LocalTime lateArrivalTime) { this.lateArrivalTime = lateArrivalTime; }
    
    public String getExcuseReason() { return excuseReason; }
    public void setExcuseReason(String excuseReason) { this.excuseReason = excuseReason; }
    
    // Helper methods
    public boolean isPresent() { return "PRESENT".equals(status); }
    public boolean isAbsent() { return "ABSENT".equals(status); }
    public boolean isLate() { return "LATE".equals(status); }
    
    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", checkInTime=" + checkInTime +
                '}';
    }
}
