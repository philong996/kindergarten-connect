package service;

import dao.ClassDAO;
import model.Class;
import java.util.List;

/**
 * Service class for class management operations with validation and business logic
 */
public class ClassService {
    private ClassDAO classDAO;
    
    // Common grade levels for Vietnamese kindergarten
    public static final String[] GRADE_LEVELS = {
        "Lớp Mầm (3-4 tuổi)", "Lớp Chồi (4-5 tuổi)", "Lớp Lá (5-6 tuổi)"
    };
    
    // Capacity constraints
    public static final int MIN_CAPACITY = 5;
    public static final int MAX_CAPACITY = 30;
    public static final int DEFAULT_CAPACITY = 20;
    
    public ClassService() {
        this.classDAO = new ClassDAO();
    }
    
    /**
     * Add new class with validation
     */
    public boolean addClass(Class clazz) {
        validateClass(clazz);
        
        // Set default values if not provided
        if (clazz.getCapacity() <= 0) {
            clazz.setCapacity(DEFAULT_CAPACITY);
        }
        
        return classDAO.create(clazz);
    }
    
    /**
     * Update existing class with validation
     */
    public boolean updateClass(Class clazz) {
        if (clazz.getId() <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        validateClass(clazz);
        
        // Check if reducing capacity would exceed current enrollment
        Class existingClass = classDAO.findById(clazz.getId());
        if (existingClass != null && clazz.getCapacity() < existingClass.getCurrentEnrollment()) {
            throw new IllegalArgumentException(
                String.format("Cannot reduce capacity to %d. Current enrollment is %d students.", 
                             clazz.getCapacity(), existingClass.getCurrentEnrollment()));
        }
        
        return classDAO.update(clazz);
    }
    
    /**
     * Delete class with validation
     */
    public boolean deleteClass(int classId) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        // Check if class has students
        Class clazz = classDAO.findById(classId);
        if (clazz != null && clazz.getCurrentEnrollment() > 0) {
            throw new IllegalArgumentException(
                String.format("Cannot delete class. %d students are still enrolled. " +
                             "Please transfer students to other classes first.", 
                             clazz.getCurrentEnrollment()));
        }
        
        return classDAO.delete(classId);
    }
    
    /**
     * Get class by ID
     */
    public Class getClassById(int id) {
        return classDAO.findById(id);
    }
    
    /**
     * Get all classes
     */
    public List<Class> getAllClasses() {
        return classDAO.findAll();
    }
    
    /**
     * Get classes by school
     */
    public List<Class> getClassesBySchool(int schoolId) {
        return classDAO.findBySchoolId(schoolId);
    }
    
    /**
     * Get classes by teacher
     */
    public List<Class> getClassesByTeacher(int teacherId) {
        return classDAO.findByTeacherId(teacherId);
    }
    
    /**
     * Search classes by name
     */
    public List<Class> searchClasses(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllClasses();
        }
        return classDAO.searchByName(name.trim());
    }
    
    /**
     * Assign teacher to class
     */
    public boolean assignTeacher(int classId, int teacherId) {
        if (classId <= 0 || teacherId <= 0) {
            throw new IllegalArgumentException("Invalid class ID or teacher ID");
        }
        
        // Check if teacher is already assigned to another class
        List<Class> teacherClasses = classDAO.findByTeacherId(teacherId);
        if (!teacherClasses.isEmpty()) {
            throw new IllegalArgumentException(
                "Teacher is already assigned to class: " + teacherClasses.get(0).getName());
        }
        
        return classDAO.assignTeacher(classId, teacherId);
    }
    
    /**
     * Remove teacher from class
     */
    public boolean removeTeacher(int classId) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class ID");
        }
        
        return classDAO.removeTeacher(classId);
    }
    
    /**
     * Get available teachers (not assigned to any class)
     */
    public List<Object[]> getAvailableTeachers() {
        return classDAO.findAvailableTeachers();
    }
    
    /**
     * Get all teachers (for reassignment purposes)
     */
    public List<Object[]> getAllTeachers() {
        return classDAO.findAllTeachers();
    }
    
    /**
     * Get class statistics
     */
    public ClassStatistics getClassStatistics() {
        Object[] stats = classDAO.getClassStatistics();
        return new ClassStatistics(
            (Integer) stats[0], // total classes
            (Integer) stats[1], // classes with teachers
            (Integer) stats[2], // total capacity
            (Integer) stats[3]  // total enrollment
        );
    }
    
    /**
     * Check if a class can accommodate more students
     */
    public boolean canAccommodateStudents(int classId, int numberOfStudents) {
        Class clazz = classDAO.findById(classId);
        if (clazz == null) {
            return false;
        }
        
        return clazz.getAvailableSpots() >= numberOfStudents;
    }
    
    /**
     * Get classes with available spots
     */
    public List<Class> getClassesWithAvailableSpots() {
        return getAllClasses().stream()
               .filter(clazz -> !clazz.isFull())
               .toList();
    }
    
    /**
     * Get classes by grade level
     */
    public List<Class> getClassesByGradeLevel(String gradeLevel) {
        return getAllClasses().stream()
               .filter(clazz -> gradeLevel.equals(clazz.getGradeLevel()))
               .toList();
    }
    
    /**
     * Validate class data
     */
    private void validateClass(Class clazz) {
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Class name cannot be empty");
        }
        
        if (clazz.getName().length() > 50) {
            throw new IllegalArgumentException("Class name cannot exceed 50 characters");
        }
        
        if (clazz.getGradeLevel() == null || clazz.getGradeLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Grade level is required");
        }
        
        if (clazz.getCapacity() < MIN_CAPACITY || clazz.getCapacity() > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                String.format("Class capacity must be between %d and %d students", 
                             MIN_CAPACITY, MAX_CAPACITY));
        }
        
        if (clazz.getSchoolId() <= 0) {
            throw new IllegalArgumentException("Valid school ID is required");
        }
    }
    
    /**
     * Inner class for class statistics
     */
    public static class ClassStatistics {
        private final int totalClasses;
        private final int classesWithTeachers;
        private final int totalCapacity;
        private final int totalEnrollment;
        
        public ClassStatistics(int totalClasses, int classesWithTeachers, int totalCapacity, int totalEnrollment) {
            this.totalClasses = totalClasses;
            this.classesWithTeachers = classesWithTeachers;
            this.totalCapacity = totalCapacity;
            this.totalEnrollment = totalEnrollment;
        }
        
        public int getTotalClasses() { return totalClasses; }
        public int getClassesWithTeachers() { return classesWithTeachers; }
        public int getClassesWithoutTeachers() { return totalClasses - classesWithTeachers; }
        public int getTotalCapacity() { return totalCapacity; }
        public int getTotalEnrollment() { return totalEnrollment; }
        public int getAvailableSpots() { return Math.max(0, totalCapacity - totalEnrollment); }
        
        public double getCapacityUtilization() {
            if (totalCapacity == 0) return 0.0;
            return (double) totalEnrollment / totalCapacity * 100.0;
        }
        
        public double getTeacherAssignmentRate() {
            if (totalClasses == 0) return 0.0;
            return (double) classesWithTeachers / totalClasses * 100.0;
        }
    }
}
