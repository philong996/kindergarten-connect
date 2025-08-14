package service;

import dao.StudentDAO;
import model.Student;
import java.util.List;

/**
 * Service class for student management operations
 */
public class StudentService {
    private StudentDAO studentDAO;
    
    public StudentService() {
        this.studentDAO = new StudentDAO();
    }
    
    /**
     * Add new student
     */
    public boolean addStudent(Student student) {
        // Add validation logic here
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty");
        }
        
        if (student.getDob() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        
        return studentDAO.create(student);
    }
    
    /**
     * Update existing student
     */
    public boolean updateStudent(Student student) {
        if (student.getId() <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty");
        }
        
        return studentDAO.update(student);
    }
    
    /**
     * Delete student
     */
    public boolean deleteStudent(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        
        return studentDAO.delete(studentId);
    }
    
    /**
     * Get student by ID
     */
    public Student getStudentById(int id) {
        return studentDAO.findById(id);
    }
    
    /**
     * Get all students
     */
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }
    
    /**
     * Get students by class
     */
    public List<Student> getStudentsByClass(int classId) {
        return studentDAO.findByClassId(classId);
    }
    
    /**
     * Search students by name
     */
    public List<Student> searchStudents(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentDAO.searchByName(name.trim());
    }
    
    /**
     * Get student count for a class
     */
    public int getStudentCount(int classId) {
        return studentDAO.getStudentCountByClass(classId);
    }
}
