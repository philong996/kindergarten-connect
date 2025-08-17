package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Physical Development Record model class for tracking student height, weight, and BMI
 */
public class PhysicalDevelopmentRecord {
    private int id;
    private int studentId;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private LocalDate measurementDate;
    private int recordedBy; // Teacher user ID
    private String notes;
    private LocalDateTime createdAt;
    
    // Additional fields for display purposes
    private String studentName;
    private String recordedByTeacher;
    private int ageYears;
    private int ageMonths;
    private BigDecimal prevHeight;
    private BigDecimal prevWeight;
    private BigDecimal prevBmi;
    
    // Constructors
    public PhysicalDevelopmentRecord() {}
    
    public PhysicalDevelopmentRecord(int studentId, BigDecimal heightCm, BigDecimal weightKg, 
                                   LocalDate measurementDate, int recordedBy, String notes) {
        this.studentId = studentId;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.measurementDate = measurementDate;
        this.recordedBy = recordedBy;
        this.notes = notes;
    }
    
    public PhysicalDevelopmentRecord(int id, int studentId, BigDecimal heightCm, BigDecimal weightKg,
                                   BigDecimal bmi, LocalDate measurementDate, int recordedBy, 
                                   String notes, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bmi = bmi;
        this.measurementDate = measurementDate;
        this.recordedBy = recordedBy;
        this.notes = notes;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
    
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    
    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }
    
    public LocalDate getMeasurementDate() { return measurementDate; }
    public void setMeasurementDate(LocalDate measurementDate) { this.measurementDate = measurementDate; }
    
    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Display fields
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getRecordedByTeacher() { return recordedByTeacher; }
    public void setRecordedByTeacher(String recordedByTeacher) { this.recordedByTeacher = recordedByTeacher; }
    
    public int getAgeYears() { return ageYears; }
    public void setAgeYears(int ageYears) { this.ageYears = ageYears; }
    
    public int getAgeMonths() { return ageMonths; }
    public void setAgeMonths(int ageMonths) { this.ageMonths = ageMonths; }
    
    public BigDecimal getPrevHeight() { return prevHeight; }
    public void setPrevHeight(BigDecimal prevHeight) { this.prevHeight = prevHeight; }
    
    public BigDecimal getPrevWeight() { return prevWeight; }
    public void setPrevWeight(BigDecimal prevWeight) { this.prevWeight = prevWeight; }
    
    public BigDecimal getPrevBmi() { return prevBmi; }
    public void setPrevBmi(BigDecimal prevBmi) { this.prevBmi = prevBmi; }
    
    // Helper methods
    public BigDecimal getHeightChange() {
        if (prevHeight != null && heightCm != null) {
            return heightCm.subtract(prevHeight);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getWeightChange() {
        if (prevWeight != null && weightKg != null) {
            return weightKg.subtract(prevWeight);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getBmiChange() {
        if (prevBmi != null && bmi != null) {
            return bmi.subtract(prevBmi);
        }
        return BigDecimal.ZERO;
    }
    
    public String getAgeDisplay() {
        return ageYears + " years " + ageMonths + " months";
    }
    
    @Override
    public String toString() {
        return "PhysicalDevelopmentRecord{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", heightCm=" + heightCm +
                ", weightKg=" + weightKg +
                ", bmi=" + bmi +
                ", measurementDate=" + measurementDate +
                ", recordedBy=" + recordedBy +
                '}';
    }
}
