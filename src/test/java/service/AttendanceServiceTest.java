package service;

import dao.AttendanceDAO;
import model.Attendance;
import model.Student;
import service.AttendanceService;
import dao.StudentDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Test class to demonstrate attendance functionality
 */
public class AttendanceServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== Attendance Service Test ===\n");
        
        AttendanceService attendanceService = new AttendanceService();
        StudentDAO studentDAO = new StudentDAO();
        
        try {
            // Test 1: Get students in class 1
            System.out.println("1. Getting students in class 1:");
            List<Student> students = studentDAO.findByClassId(1);
            for (Student student : students) {
                System.out.println("   - " + student.getName() + " (ID: " + student.getId() + ")");
            }
            System.out.println();
            
            // Test 2: Generate default attendance for today
            System.out.println("2. Generating default attendance for today:");
            LocalDate today = LocalDate.now();
            List<Attendance> defaultAttendance = attendanceService.generateDefaultAttendance(1, today);
            for (Attendance attendance : defaultAttendance) {
                System.out.println("   - " + attendance.getStudentName() + ": " + attendance.getStatus());
            }
            System.out.println();
            
            // Test 3: Mark some students as present
            System.out.println("3. Marking attendance:");
            if (!defaultAttendance.isEmpty()) {
                // Mark first student as present
                Attendance firstStudent = defaultAttendance.get(0);
                System.out.println("   - First student ID: " + firstStudent.getStudentId() + ", Name: " + firstStudent.getStudentName());
                firstStudent.setStatus("PRESENT");
                firstStudent.setCheckInTime(LocalTime.of(8, 30));
                boolean success1 = attendanceService.markAttendance(firstStudent);
                System.out.println("   - Marked " + firstStudent.getStudentName() + " as PRESENT: " + success1);
                
                // Mark second student as late (if exists)
                if (defaultAttendance.size() > 1) {
                    Attendance secondStudent = defaultAttendance.get(1);
                    System.out.println("   - Second student ID: " + secondStudent.getStudentId() + ", Name: " + secondStudent.getStudentName());
                    secondStudent.setStatus("LATE");
                    secondStudent.setCheckInTime(LocalTime.of(8, 45));
                    secondStudent.setLateArrivalTime(LocalTime.of(8, 45));
                    secondStudent.setExcuseReason("Traffic jam");
                    boolean success2 = attendanceService.markAttendance(secondStudent);
                    System.out.println("   - Marked " + secondStudent.getStudentName() + " as LATE: " + success2);
                }
                
                // Mark third student as absent (if exists)
                if (defaultAttendance.size() > 2) {
                    Attendance thirdStudent = defaultAttendance.get(2);
                    System.out.println("   - Third student ID: " + thirdStudent.getStudentId() + ", Name: " + thirdStudent.getStudentName());
                    thirdStudent.setStatus("ABSENT");
                    thirdStudent.setExcuseReason("Sick");
                    boolean success3 = attendanceService.markAttendance(thirdStudent);
                    System.out.println("   - Marked " + thirdStudent.getStudentName() + " as ABSENT: " + success3);
                }
            }
            System.out.println();
            
            // Test 4: Get class attendance summary
            System.out.println("4. Class attendance summary for today:");
            AttendanceService.AttendanceSummary summary = attendanceService.getClassAttendanceSummary(1, today);
            System.out.println("   - Total Students: " + summary.getTotalStudents());
            System.out.println("   - Present: " + summary.getPresentCount());
            System.out.println("   - Absent: " + summary.getAbsentCount());
            System.out.println("   - Late: " + summary.getLateCount());
            System.out.println("   - Attendance Rate: " + String.format("%.1f%%", summary.getAttendanceRate()));
            System.out.println();
            
            // Test 5: Get attendance history for a student
            if (!students.isEmpty()) {
                System.out.println("5. Attendance history for " + students.get(0).getName() + ":");
                LocalDate weekAgo = today.minusWeeks(1);
                List<Attendance> history = attendanceService.getAttendanceHistory(students.get(0).getId(), weekAgo, today);
                if (history.isEmpty()) {
                    System.out.println("   - No attendance records found for the past week");
                } else {
                    for (Attendance record : history) {
                        System.out.println("   - " + record.getDate() + ": " + record.getStatus() + 
                                         (record.getCheckInTime() != null ? " at " + record.getCheckInTime() : "") +
                                         (record.getExcuseReason() != null ? " (" + record.getExcuseReason() + ")" : ""));
                    }
                }
                System.out.println();
            }
            
            // Test 6: Get attendance statistics
            if (!students.isEmpty()) {
                System.out.println("6. Attendance statistics for " + students.get(0).getName() + ":");
                LocalDate monthAgo = today.minusMonths(1);
                dao.AttendanceDAO.AttendanceStats stats = attendanceService.getAttendanceStats(students.get(0).getId(), monthAgo, today);
                System.out.println("   - Total Days: " + stats.getTotalDays());
                System.out.println("   - Present Days: " + stats.getPresentDays());
                System.out.println("   - Absent Days: " + stats.getAbsentDays());
                System.out.println("   - Late Days: " + stats.getLateDays());
                System.out.println("   - Attendance Rate: " + String.format("%.1f%%", stats.getAttendanceRate()));
                System.out.println("   - Late Rate: " + String.format("%.1f%%", stats.getLateRate()));
            }
            
            System.out.println("\n=== Test completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
