package dao;

import model.Attendance;
import model.Student;
import util.DatabaseUtil;
import util.DatabaseImageUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance operations
 */
public class AttendanceDAO {
    
    /**
     * Create new attendance record
     */
    public boolean create(Attendance attendance) {
        // First try with all columns, fallback to basic columns if image columns don't exist
        String sqlWithImages = "INSERT INTO attendance (student_id, date, status, check_in_time, check_out_time, late_arrival_time, excuse_reason, check_in_image, check_out_image) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (student_id, date) " +
                    "DO UPDATE SET status = EXCLUDED.status, check_in_time = EXCLUDED.check_in_time, " +
                    "check_out_time = EXCLUDED.check_out_time, late_arrival_time = EXCLUDED.late_arrival_time, " +
                    "excuse_reason = EXCLUDED.excuse_reason, check_in_image = EXCLUDED.check_in_image, " +
                    "check_out_image = EXCLUDED.check_out_image";
        
        String sqlBasic = "INSERT INTO attendance (student_id, date, status, check_in_time, late_arrival_time, excuse_reason) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (student_id, date) " +
                    "DO UPDATE SET status = EXCLUDED.status, check_in_time = EXCLUDED.check_in_time, " +
                    "late_arrival_time = EXCLUDED.late_arrival_time, excuse_reason = EXCLUDED.excuse_reason";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Try with all columns first
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithImages)) {
                stmt.setInt(1, attendance.getStudentId());
                stmt.setDate(2, Date.valueOf(attendance.getDate()));
                stmt.setString(3, attendance.getStatus());
                
                if (attendance.getCheckInTime() != null) {
                    stmt.setTime(4, Time.valueOf(attendance.getCheckInTime()));
                } else {
                    stmt.setNull(4, Types.TIME);
                }
                
                if (attendance.getCheckOutTime() != null) {
                    stmt.setTime(5, Time.valueOf(attendance.getCheckOutTime()));
                } else {
                    stmt.setNull(5, Types.TIME);
                }
                
                if (attendance.getLateArrivalTime() != null) {
                    stmt.setTime(6, Time.valueOf(attendance.getLateArrivalTime()));
                } else {
                    stmt.setNull(6, Types.TIME);
                }
                
                stmt.setString(7, attendance.getExcuseReason());
                
                // Handle BYTEA image data properly for PostgreSQL
                DatabaseImageUtil.setBytesParameter(stmt, 8, attendance.getCheckInImage());
                DatabaseImageUtil.setBytesParameter(stmt, 9, attendance.getCheckOutImage());
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                // If image columns don't exist, try with basic columns
                if (e.getMessage().contains("check_in_image") || e.getMessage().contains("check_out_image") || 
                    e.getMessage().contains("check_out_time")) {
                    
                    System.out.println("Image/checkout columns not found, using basic attendance tracking...");
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sqlBasic)) {
                        stmt.setInt(1, attendance.getStudentId());
                        stmt.setDate(2, Date.valueOf(attendance.getDate()));
                        stmt.setString(3, attendance.getStatus());
                        
                        if (attendance.getCheckInTime() != null) {
                            stmt.setTime(4, Time.valueOf(attendance.getCheckInTime()));
                        } else {
                            stmt.setNull(4, Types.TIME);
                        }
                        
                        if (attendance.getLateArrivalTime() != null) {
                            stmt.setTime(5, Time.valueOf(attendance.getLateArrivalTime()));
                        } else {
                            stmt.setNull(5, Types.TIME);
                        }
                        
                        stmt.setString(6, attendance.getExcuseReason());
                        
                        int rowsAffected = stmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                } else {
                    throw e;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating attendance record: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update existing attendance record
     */
    public boolean update(Attendance attendance) {
        // Try with all columns first, fallback to basic columns if image/checkout columns don't exist
        String sqlWithImages = "UPDATE attendance SET status = ?, check_in_time = ?, check_out_time = ?, " +
                    "late_arrival_time = ?, excuse_reason = ?, check_in_image = ?, check_out_image = ? " +
                    "WHERE id = ?";
        
        String sqlBasic = "UPDATE attendance SET status = ?, check_in_time = ?, late_arrival_time = ?, excuse_reason = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Try with all columns first
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithImages)) {
                stmt.setString(1, attendance.getStatus());
                
                if (attendance.getCheckInTime() != null) {
                    stmt.setTime(2, Time.valueOf(attendance.getCheckInTime()));
                } else {
                    stmt.setNull(2, Types.TIME);
                }
                
                if (attendance.getCheckOutTime() != null) {
                    stmt.setTime(3, Time.valueOf(attendance.getCheckOutTime()));
                } else {
                    stmt.setNull(3, Types.TIME);
                }
                
                if (attendance.getLateArrivalTime() != null) {
                    stmt.setTime(4, Time.valueOf(attendance.getLateArrivalTime()));
                } else {
                    stmt.setNull(4, Types.TIME);
                }
                
                stmt.setString(5, attendance.getExcuseReason());
                
                // Handle BYTEA image data properly for PostgreSQL
                DatabaseImageUtil.setBytesParameter(stmt, 6, attendance.getCheckInImage());
                DatabaseImageUtil.setBytesParameter(stmt, 7, attendance.getCheckOutImage());
                
                stmt.setInt(8, attendance.getId());
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                // If image/checkout columns don't exist, try with basic columns
                if (e.getMessage().contains("check_in_image") || e.getMessage().contains("check_out_image") || 
                    e.getMessage().contains("check_out_time")) {
                    
                    System.out.println("Image/checkout columns not found, using basic attendance update...");
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sqlBasic)) {
                        stmt.setString(1, attendance.getStatus());
                        
                        if (attendance.getCheckInTime() != null) {
                            stmt.setTime(2, Time.valueOf(attendance.getCheckInTime()));
                        } else {
                            stmt.setNull(2, Types.TIME);
                        }
                        
                        if (attendance.getLateArrivalTime() != null) {
                            stmt.setTime(3, Time.valueOf(attendance.getLateArrivalTime()));
                        } else {
                            stmt.setNull(3, Types.TIME);
                        }
                        
                        stmt.setString(4, attendance.getExcuseReason());
                        stmt.setInt(5, attendance.getId());
                        
                        int rowsAffected = stmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                } else {
                    throw e;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating attendance record: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find attendance by student ID and date
     */
    public Attendance findByStudentAndDate(int studentId, LocalDate date) {
        String sql = "SELECT a.*, s.name as student_name FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id " +
                    "WHERE a.student_id = ? AND a.date = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createAttendanceFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding attendance record: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get attendance records for a specific date
     */
    public List<Attendance> findByDate(LocalDate date) {
        String sql = "SELECT a.*, s.name as student_name FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id " +
                    "WHERE a.date = ? ORDER BY s.name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(createAttendanceFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding attendance records by date: " + e.getMessage());
        }
        
        return attendanceList;
    }
    
    /**
     * Get attendance records for a specific student within date range
     */
    public List<Attendance> findByStudentAndDateRange(int studentId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT a.*, s.name as student_name FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id " +
                    "WHERE a.student_id = ? AND a.date BETWEEN ? AND ? " +
                    "ORDER BY a.date DESC";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(createAttendanceFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding attendance records by student and date range: " + e.getMessage());
        }
        
        return attendanceList;
    }
    
    /**
     * Get attendance records for all students in a class for a specific date
     */
    public List<Attendance> findByClassAndDate(int classId, LocalDate date) {
        // Try with all columns first, fallback to basic columns if image columns don't exist
        String sqlWithImages = "SELECT s.id as student_id, s.name as student_name, " +
                    "a.id as attendance_id, a.date, a.status, a.check_in_time, a.check_out_time, a.late_arrival_time, " +
                    "a.excuse_reason, a.check_in_image, a.check_out_image, a.created_at " +
                    "FROM students s " +
                    "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? " +
                    "WHERE s.class_id = ? ORDER BY s.name";
        
        String sqlBasic = "SELECT s.id as student_id, s.name as student_name, " +
                    "a.id as attendance_id, a.date, a.status, a.check_in_time, a.late_arrival_time, a.excuse_reason, a.created_at " +
                    "FROM students s " +
                    "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? " +
                    "WHERE s.class_id = ? ORDER BY s.name";
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Try with all columns first
            try (PreparedStatement stmt = conn.prepareStatement(sqlWithImages)) {
                stmt.setDate(1, Date.valueOf(date));
                stmt.setInt(2, classId);
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Attendance attendance = createAttendanceFromResultSet(rs, true);
                    // Ensure date is set for records without attendance data
                    if (attendance.getDate() == null) {
                        attendance.setDate(date);
                    }
                    attendanceList.add(attendance);
                }
                
            } catch (SQLException e) {
                // If image/checkout columns don't exist, try with basic columns
                if (e.getMessage().contains("check_in_image") || e.getMessage().contains("check_out_image") || 
                    e.getMessage().contains("check_out_time")) {
                    
                    System.out.println("Image/checkout columns not found, using basic attendance loading...");
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sqlBasic)) {
                        stmt.setDate(1, Date.valueOf(date));
                        stmt.setInt(2, classId);
                        
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            Attendance attendance = createAttendanceFromResultSet(rs, false);
                            // Ensure date is set for records without attendance data
                            if (attendance.getDate() == null) {
                                attendance.setDate(date);
                            }
                            attendanceList.add(attendance);
                        }
                        
                    }
                } else {
                    throw e;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding attendance records by class and date: " + e.getMessage());
        }
        
        return attendanceList;
    }
    
    /**
     * Helper method to create Attendance object from ResultSet
     */
    private Attendance createAttendanceFromResultSet(ResultSet rs, boolean includeImages) throws SQLException {
        Attendance attendance = new Attendance();
        
        // Always set these from the students table
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setStudentName(rs.getString("student_name"));
        
        // Set attendance-specific fields if record exists
        if (rs.getObject("attendance_id") != null) {
            attendance.setId(rs.getInt("attendance_id"));
            attendance.setStatus(rs.getString("status"));
            
            // Get date from attendance record
            Date attendanceDate = rs.getDate("date");
            if (attendanceDate != null) {
                attendance.setDate(attendanceDate.toLocalDate());
            }
            
            Time checkInTime = rs.getTime("check_in_time");
            if (checkInTime != null) {
                attendance.setCheckInTime(checkInTime.toLocalTime());
            }
            
            if (includeImages) {
                // Handle check_out_time
                Time checkOutTime = rs.getTime("check_out_time");
                if (checkOutTime != null) {
                    attendance.setCheckOutTime(checkOutTime.toLocalTime());
                }
                
                // Handle image fields
                attendance.setCheckInImage(DatabaseImageUtil.getBytesFromResultSet(rs, "check_in_image"));
                attendance.setCheckOutImage(DatabaseImageUtil.getBytesFromResultSet(rs, "check_out_image"));
            }
            
            Time lateArrivalTime = rs.getTime("late_arrival_time");
            if (lateArrivalTime != null) {
                attendance.setLateArrivalTime(lateArrivalTime.toLocalTime());
            }
            
            attendance.setExcuseReason(rs.getString("excuse_reason"));
            
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                attendance.setCreatedAt(createdAt.toLocalDateTime());
            }
        } else {
            // No attendance record, set default values
            // The date needs to be set from the query parameter since attendance.date is NULL
            // We'll need to pass the date from the calling method
            attendance.setStatus("ABSENT");
        }
        
        return attendance;
    }
    
    /**
     * Get attendance statistics for a student over a period
     */
    public AttendanceStats getAttendanceStats(int studentId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT " +
                    "COUNT(*) as total_days, " +
                    "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_days, " +
                    "SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_days, " +
                    "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_days " +
                    "FROM attendance " +
                    "WHERE student_id = ? AND date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AttendanceStats(
                    rs.getInt("total_days"),
                    rs.getInt("present_days"),
                    rs.getInt("absent_days"),
                    rs.getInt("late_days")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating attendance stats: " + e.getMessage());
        }
        
        return new AttendanceStats(0, 0, 0, 0);
    }
    
    /**
     * Delete attendance record
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting attendance record: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get students who are absent today (for notifications)
     */
    public List<Student> getAbsentStudentsToday(int classId) {
        String sql = "SELECT s.* FROM students s " +
                    "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = CURRENT_DATE " +
                    "WHERE s.class_id = ? AND (a.status = 'ABSENT' OR a.status IS NULL) " +
                    "ORDER BY s.name";
        
        List<Student> absentStudents = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setDob(rs.getDate("dob").toLocalDate());
                student.setClassId(rs.getInt("class_id"));
                student.setAddress(rs.getString("address"));
                absentStudents.add(student);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting absent students: " + e.getMessage());
        }
        
        return absentStudents;
    }
    
    /**
     * Create Attendance object from ResultSet
     * This method assumes all required attendance fields are present in the ResultSet
     */
    private Attendance createAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id"));
        attendance.setStudentId(rs.getInt("student_id"));
        attendance.setDate(rs.getDate("date").toLocalDate());
        attendance.setStatus(rs.getString("status"));
        
        Time checkInTime = rs.getTime("check_in_time");
        if (checkInTime != null) {
            attendance.setCheckInTime(checkInTime.toLocalTime());
        }
        
        // Handle checkout time (might not exist in older schema)
        try {
            Time checkOutTime = rs.getTime("check_out_time");
            if (checkOutTime != null) {
                attendance.setCheckOutTime(checkOutTime.toLocalTime());
            }
        } catch (SQLException e) {
            // Column doesn't exist, skip
        }
        
        Time lateArrivalTime = rs.getTime("late_arrival_time");
        if (lateArrivalTime != null) {
            attendance.setLateArrivalTime(lateArrivalTime.toLocalTime());
        }
        
        attendance.setExcuseReason(rs.getString("excuse_reason"));
        attendance.setStudentName(rs.getString("student_name"));
        
        // Handle image data (might not exist in older schema)
        try {
            byte[] checkInImage = DatabaseImageUtil.getBytesFromResultSet(rs, "check_in_image");
            if (checkInImage != null) {
                attendance.setCheckInImage(checkInImage);
            }
        } catch (SQLException e) {
            // Column doesn't exist, skip
        }
        
        try {
            byte[] checkOutImage = DatabaseImageUtil.getBytesFromResultSet(rs, "check_out_image");
            if (checkOutImage != null) {
                attendance.setCheckOutImage(checkOutImage);
            }
        } catch (SQLException e) {
            // Column doesn't exist, skip
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            attendance.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return attendance;
    }
    
    /**
     * Inner class for attendance statistics
     */
    public static class AttendanceStats {
        private final int totalDays;
        private final int presentDays;
        private final int absentDays;
        private final int lateDays;
        
        public AttendanceStats(int totalDays, int presentDays, int absentDays, int lateDays) {
            this.totalDays = totalDays;
            this.presentDays = presentDays;
            this.absentDays = absentDays;
            this.lateDays = lateDays;
        }
        
        public int getTotalDays() { return totalDays; }
        public int getPresentDays() { return presentDays; }
        public int getAbsentDays() { return absentDays; }
        public int getLateDays() { return lateDays; }
        
        public double getAttendanceRate() {
            return totalDays > 0 ? (double) presentDays / totalDays * 100 : 0;
        }
        
        public double getLateRate() {
            return totalDays > 0 ? (double) lateDays / totalDays * 100 : 0;
        }
    }
}
