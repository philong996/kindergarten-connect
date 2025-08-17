package service;

import dao.PhysicalDevelopmentDAO;
import model.PhysicalDevelopmentRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Physical Development operations
 */
public class PhysicalDevelopmentService {
    private PhysicalDevelopmentDAO physicalDAO;
    
    public PhysicalDevelopmentService() {
        this.physicalDAO = new PhysicalDevelopmentDAO();
    }
    
    /**
     * Add a new physical development record
     */
    public boolean recordPhysicalData(int studentId, double heightCm, double weightKg, 
                                    LocalDate measurementDate, int recordedBy, String notes) {
        // Validate input
        if (heightCm <= 0 || weightKg <= 0) {
            throw new IllegalArgumentException("Height and weight must be positive values");
        }
        
        if (heightCm < 50 || heightCm > 200) {
            throw new IllegalArgumentException("Height must be between 50cm and 200cm");
        }
        
        if (weightKg < 5 || weightKg > 100) {
            throw new IllegalArgumentException("Weight must be between 5kg and 100kg");
        }
        
        PhysicalDevelopmentRecord record = new PhysicalDevelopmentRecord(
            studentId,
            BigDecimal.valueOf(heightCm),
            BigDecimal.valueOf(weightKg),
            measurementDate,
            recordedBy,
            notes
        );
        
        return physicalDAO.addRecord(record);
    }
    
    /**
     * Get all physical development records for a student
     */
    public List<PhysicalDevelopmentRecord> getStudentPhysicalHistory(int studentId) {
        return physicalDAO.getStudentRecords(studentId);
    }
    
    /**
     * Get the latest physical development record for a student
     */
    public PhysicalDevelopmentRecord getLatestPhysicalData(int studentId) {
        return physicalDAO.getLatestRecord(studentId);
    }
    
    /**
     * Get physical development records for all students in a class
     */
    public List<PhysicalDevelopmentRecord> getClassPhysicalData(int classId) {
        return physicalDAO.getClassRecords(classId);
    }
    
    /**
     * Update an existing physical development record
     */
    public boolean updatePhysicalRecord(PhysicalDevelopmentRecord record) {
        // Validate input
        if (record.getHeightCm().doubleValue() <= 0 || record.getWeightKg().doubleValue() <= 0) {
            throw new IllegalArgumentException("Height and weight must be positive values");
        }
        
        return physicalDAO.updateRecord(record);
    }
    
    /**
     * Delete a physical development record
     */
    public boolean deletePhysicalRecord(int recordId) {
        return physicalDAO.deleteRecord(recordId);
    }
    
    /**
     * Get physical development records within a date range
     */
    public List<PhysicalDevelopmentRecord> getPhysicalDataByDateRange(int studentId, 
                                                                     LocalDate startDate, 
                                                                     LocalDate endDate) {
        return physicalDAO.getRecordsByDateRange(studentId, startDate, endDate);
    }
    
    /**
     * Calculate BMI growth categories for kindergarten children
     */
    public String getBMICategory(BigDecimal bmi, int ageMonths) {
        if (bmi == null) return "Unknown";
        
        double bmiValue = bmi.doubleValue();
        
        // Simplified BMI categories for kindergarten children (3-6 years)
        // These are general guidelines and should be used with pediatric growth charts
        if (bmiValue < 14.0) {
            return "Underweight";
        } else if (bmiValue < 17.0) {
            return "Normal";
        } else if (bmiValue < 19.0) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    /**
     * Get growth trend analysis for a student
     */
    public String getGrowthTrend(List<PhysicalDevelopmentRecord> records) {
        if (records == null || records.size() < 2) {
            return "Insufficient data for trend analysis";
        }
        
        // Sort by date (most recent first)
        records.sort((r1, r2) -> r2.getMeasurementDate().compareTo(r1.getMeasurementDate()));
        
        PhysicalDevelopmentRecord latest = records.get(0);
        PhysicalDevelopmentRecord previous = records.get(1);
        
        double heightChange = latest.getHeightCm().doubleValue() - previous.getHeightCm().doubleValue();
        double weightChange = latest.getWeightKg().doubleValue() - previous.getWeightKg().doubleValue();
        
        StringBuilder trend = new StringBuilder();
        
        if (heightChange > 2.0) {
            trend.append("Rapid height growth");
        } else if (heightChange > 0.5) {
            trend.append("Normal height growth");
        } else if (heightChange > 0) {
            trend.append("Slow height growth");
        } else {
            trend.append("No height growth");
        }
        
        trend.append(", ");
        
        if (weightChange > 1.0) {
            trend.append("rapid weight gain");
        } else if (weightChange > 0.2) {
            trend.append("normal weight gain");
        } else if (weightChange > 0) {
            trend.append("slow weight gain");
        } else if (weightChange < -0.5) {
            trend.append("weight loss");
        } else {
            trend.append("stable weight");
        }
        
        return trend.toString();
    }
    
    /**
     * Check if a new measurement is due for a student
     */
    public boolean isMeasurementDue(int studentId, int intervalDays) {
        PhysicalDevelopmentRecord latest = getLatestPhysicalData(studentId);
        if (latest == null) {
            return true; // No previous measurement
        }
        
        LocalDate lastMeasurement = latest.getMeasurementDate();
        LocalDate today = LocalDate.now();
        
        return lastMeasurement.plusDays(intervalDays).isBefore(today) || 
               lastMeasurement.plusDays(intervalDays).isEqual(today);
    }
}
