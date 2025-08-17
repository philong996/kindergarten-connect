package dao;

import model.PhysicalDevelopmentRecord;
import util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Physical Development Records
 */
public class PhysicalDevelopmentDAO {
    
    /**
     * Add a new physical development record
     */
    public boolean addRecord(PhysicalDevelopmentRecord record) {
        String sql = "INSERT INTO physical_development_records (student_id, height_cm, weight_kg, " +
                    "measurement_date, recorded_by, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, record.getStudentId());
            pstmt.setBigDecimal(2, record.getHeightCm());
            pstmt.setBigDecimal(3, record.getWeightKg());
            pstmt.setDate(4, Date.valueOf(record.getMeasurementDate()));
            pstmt.setInt(5, record.getRecordedBy());
            pstmt.setString(6, record.getNotes());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding physical development record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all physical development records for a student
     */
    public List<PhysicalDevelopmentRecord> getStudentRecords(int studentId) {
        List<PhysicalDevelopmentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM physical_development_summary WHERE student_id = ? ORDER BY measurement_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PhysicalDevelopmentRecord record = mapResultSetToRecord(rs);
                records.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving student physical development records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Get the latest physical development record for a student
     */
    public PhysicalDevelopmentRecord getLatestRecord(int studentId) {
        String sql = "SELECT * FROM physical_development_summary WHERE student_id = ? " +
                    "ORDER BY measurement_date DESC LIMIT 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRecord(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving latest physical development record: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all physical development records for a class
     */
    public List<PhysicalDevelopmentRecord> getClassRecords(int classId) {
        List<PhysicalDevelopmentRecord> records = new ArrayList<>();
        String sql = "SELECT pds.* FROM physical_development_summary pds " +
                    "JOIN students s ON pds.student_id = s.id " +
                    "WHERE s.class_id = ? ORDER BY pds.student_name, pds.measurement_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PhysicalDevelopmentRecord record = mapResultSetToRecord(rs);
                records.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving class physical development records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Update an existing physical development record
     */
    public boolean updateRecord(PhysicalDevelopmentRecord record) {
        String sql = "UPDATE physical_development_records SET height_cm = ?, weight_kg = ?, " +
                    "measurement_date = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, record.getHeightCm());
            pstmt.setBigDecimal(2, record.getWeightKg());
            pstmt.setDate(3, Date.valueOf(record.getMeasurementDate()));
            pstmt.setString(4, record.getNotes());
            pstmt.setInt(5, record.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating physical development record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a physical development record
     */
    public boolean deleteRecord(int recordId) {
        String sql = "DELETE FROM physical_development_records WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting physical development record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get records within a date range for a student
     */
    public List<PhysicalDevelopmentRecord> getRecordsByDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        List<PhysicalDevelopmentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM physical_development_summary WHERE student_id = ? " +
                    "AND measurement_date BETWEEN ? AND ? ORDER BY measurement_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PhysicalDevelopmentRecord record = mapResultSetToRecord(rs);
                records.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving physical development records by date range: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Map ResultSet to PhysicalDevelopmentRecord object
     */
    private PhysicalDevelopmentRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        PhysicalDevelopmentRecord record = new PhysicalDevelopmentRecord();
        
        record.setId(rs.getInt("id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setHeightCm(rs.getBigDecimal("height_cm"));
        record.setWeightKg(rs.getBigDecimal("weight_kg"));
        record.setBmi(rs.getBigDecimal("bmi"));
        record.setMeasurementDate(rs.getDate("measurement_date").toLocalDate());
        record.setRecordedBy(rs.getInt("recorded_by"));
        record.setNotes(rs.getString("notes"));
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            record.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        // Set display fields if available
        try {
            record.setStudentName(rs.getString("student_name"));
            record.setRecordedByTeacher(rs.getString("recorded_by_teacher"));
            record.setAgeYears(rs.getInt("age_years"));
            record.setAgeMonths(rs.getInt("age_months"));
            record.setPrevHeight(rs.getBigDecimal("prev_height"));
            record.setPrevWeight(rs.getBigDecimal("prev_weight"));
            record.setPrevBmi(rs.getBigDecimal("prev_bmi"));
        } catch (SQLException e) {
            // These fields might not be available in all queries, ignore
        }
        
        return record;
    }
}
