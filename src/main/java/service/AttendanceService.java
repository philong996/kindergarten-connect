package service;

import dao.AttendanceDAO;
import dao.AttendanceDAO.AttendanceStats;
import dao.StudentDAO;
import model.Attendance;
import model.Student;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service class for attendance management operations
 */
public class AttendanceService {
    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    
    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();
    }
    
    /**
     * Mark attendance for a student
     */
    public boolean markAttendance(Attendance attendance) {
        // Validate input
        if (attendance.getStudentId() <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (attendance.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        if (attendance.getStatus() == null || attendance.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        
        // Validate status
        String status = attendance.getStatus().toUpperCase();
        if (!status.equals("PRESENT") && !status.equals("ABSENT") && !status.equals("LATE")) {
            throw new IllegalArgumentException("Invalid status. Must be PRESENT, ABSENT, or LATE");
        }
        attendance.setStatus(status);
        
        // Validate check-in time for present students
        if ("PRESENT".equals(status) && attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(LocalTime.now());
        }
        
        // Validate late arrival time for late students
        if ("LATE".equals(status)) {
            if (attendance.getLateArrivalTime() == null) {
                attendance.setLateArrivalTime(LocalTime.now());
            }
            if (attendance.getCheckInTime() == null) {
                attendance.setCheckInTime(attendance.getLateArrivalTime());
            }
        }
        
        // Clear check-in time for absent students
        if ("ABSENT".equals(status)) {
            attendance.setCheckInTime(null);
            attendance.setLateArrivalTime(null);
        }
        
        return attendanceDAO.create(attendance);
    }
    
    /**
     * Update existing attendance record
     */
    public boolean updateAttendance(Attendance attendance) {
        if (attendance.getId() <= 0) {
            throw new IllegalArgumentException("Invalid attendance ID");
        }
        
        // Validate input (same validation as markAttendance but without creating)
        if (attendance.getStudentId() <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (attendance.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        if (attendance.getStatus() == null || attendance.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        
        // Validate status
        String status = attendance.getStatus().toUpperCase();
        if (!status.equals("PRESENT") && !status.equals("ABSENT") && !status.equals("LATE")) {
            throw new IllegalArgumentException("Invalid status. Must be PRESENT, ABSENT, or LATE");
        }
        attendance.setStatus(status);
        
        // Validate check-in time for present students
        if ("PRESENT".equals(status) && attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(LocalTime.now());
        }
        
        // Validate late arrival time for late students
        if ("LATE".equals(status)) {
            if (attendance.getLateArrivalTime() == null) {
                attendance.setLateArrivalTime(LocalTime.now());
            }
            if (attendance.getCheckInTime() == null) {
                attendance.setCheckInTime(attendance.getLateArrivalTime());
            }
        }
        
        // Clear check-in time for absent students
        if ("ABSENT".equals(status)) {
            attendance.setCheckInTime(null);
            attendance.setLateArrivalTime(null);
        }
        
        return attendanceDAO.update(attendance);
    }
    
    /**
     * Mark multiple students' attendance at once
     */
    public boolean markBulkAttendance(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            throw new IllegalArgumentException("Attendance list cannot be empty");
        }
        
        boolean success = true;
        for (Attendance attendance : attendanceList) {
            try {
                if (!markAttendance(attendance)) {
                    success = false;
                }
            } catch (Exception e) {
                System.err.println("Error marking attendance for student " + attendance.getStudentId() + ": " + e.getMessage());
                success = false;
            }
        }
        
        return success;
    }
    
    /**
     * Get attendance for a specific student and date
     */
    public Attendance getAttendance(int studentId, LocalDate date) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        return attendanceDAO.findByStudentAndDate(studentId, date);
    }
    
    /**
     * Get attendance records for a specific date
     */
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        return attendanceDAO.findByDate(date);
    }
    
    /**
     * Get attendance records for all students in a class for a specific date
     */
    public List<Attendance> getClassAttendance(int classId, LocalDate date) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        return attendanceDAO.findByClassAndDate(classId, date);
    }
    
    /**
     * Get attendance history for a student within date range
     */
    public List<Attendance> getAttendanceHistory(int studentId, LocalDate startDate, LocalDate endDate) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return attendanceDAO.findByStudentAndDateRange(studentId, startDate, endDate);
    }
    
    /**
     * Get attendance statistics for a student
     */
    public AttendanceStats getAttendanceStats(int studentId, LocalDate startDate, LocalDate endDate) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return attendanceDAO.getAttendanceStats(studentId, startDate, endDate);
    }
    
    /**
     * Delete attendance record
     */
    public boolean deleteAttendance(int attendanceId) {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Invalid attendance ID");
        }
        
        return attendanceDAO.delete(attendanceId);
    }
    
    /**
     * Get students who are absent today
     */
    public List<Student> getAbsentStudentsToday(int classId) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        return attendanceDAO.getAbsentStudentsToday(classId);
    }
    
    /**
     * Check if attendance can be marked for the given date
     */
    public boolean canMarkAttendance(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        // Allow marking attendance for today and past dates
        // You can modify this logic based on business rules
        return !date.isAfter(LocalDate.now());
    }
    
    /**
     * Generate default attendance records for all students in a class
     * This is useful for initializing attendance for a new day
     */
    public List<Attendance> generateDefaultAttendance(int classId, LocalDate date) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        
        // Get all students in the class
        List<Student> students = studentDAO.findByClassId(classId);
        
        // Check if attendance already exists for this date
        List<Attendance> existingAttendance = attendanceDAO.findByClassAndDate(classId, date);
        
        // If attendance already exists, return it
        if (!existingAttendance.isEmpty()) {
            return existingAttendance;
        }
        
        // Generate default attendance records (all marked as absent initially)
        List<Attendance> defaultAttendance = new java.util.ArrayList<>();
        for (Student student : students) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(student.getId());
            attendance.setDate(date);
            attendance.setStatus("ABSENT");
            attendance.setStudentName(student.getName());
            defaultAttendance.add(attendance);
        }
        
        return defaultAttendance;
    }
    
    /**
     * Validate attendance status transitions
     */
    public boolean isValidStatusTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || newStatus == null) {
            return true; // Allow initial status setting
        }
        
        // Allow any status transition for flexibility
        // You can add more restrictive rules here if needed
        return true;
    }
    
    /**
     * Get summary of attendance for a class on a specific date
     */
    public AttendanceSummary getClassAttendanceSummary(int classId, LocalDate date) {
        List<Attendance> attendanceList = getClassAttendance(classId, date);
        
        int totalStudents = attendanceList.size();
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        
        for (Attendance attendance : attendanceList) {
            switch (attendance.getStatus()) {
                case "PRESENT":
                    presentCount++;
                    break;
                case "ABSENT":
                    absentCount++;
                    break;
                case "LATE":
                    lateCount++;
                    break;
            }
        }
        
        return new AttendanceSummary(totalStudents, presentCount, absentCount, lateCount);
    }
    
    /**
     * Inner class for attendance summary
     */
    public static class AttendanceSummary {
        private final int totalStudents;
        private final int presentCount;
        private final int absentCount;
        private final int lateCount;
        
        public AttendanceSummary(int totalStudents, int presentCount, int absentCount, int lateCount) {
            this.totalStudents = totalStudents;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
        }
        
        public int getTotalStudents() { return totalStudents; }
        public int getPresentCount() { return presentCount; }
        public int getAbsentCount() { return absentCount; }
        public int getLateCount() { return lateCount; }
        
        public double getAttendanceRate() {
            return totalStudents > 0 ? (double) presentCount / totalStudents * 100 : 0;
        }
        
        public double getAbsenceRate() {
            return totalStudents > 0 ? (double) absentCount / totalStudents * 100 : 0;
        }
        
        public double getLateRate() {
            return totalStudents > 0 ? (double) lateCount / totalStudents * 100 : 0;
        }
    }
}
