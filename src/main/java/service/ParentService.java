package service;

import dao.ParentDAO;
import model.Student;
import dao.StudentDAO;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Service class for parent-related operations
 */
public class ParentService {
    private ParentDAO parentDAO;
    private StudentDAO studentDAO;
    
    public ParentService() {
        this.parentDAO = new ParentDAO();
        this.studentDAO = new StudentDAO();
    }
    
    /**
     * Get children of a parent by user ID
     */
    public List<Student> getParentChildren(int userId) {
        List<Map<String, Object>> childrenData = parentDAO.getParentChildren(userId);
        List<Student> children = new ArrayList<>();
        
        for (Map<String, Object> childData : childrenData) {
            int studentId = (Integer) childData.get("id");
            Student student = studentDAO.findById(studentId);
            if (student != null) {
                children.add(student);
            }
        }
        
        return children;
    }
    
    /**
     * Get the first child of a parent (for parents with single children)
     */
    public Student getFirstChild(int userId) {
        List<Student> children = getParentChildren(userId);
        return children.isEmpty() ? null : children.get(0);
    }
    
    /**
     * Check if parent has any children
     */
    public boolean hasChildren(int userId) {
        List<Map<String, Object>> children = parentDAO.getParentChildren(userId);
        return !children.isEmpty();
    }
    
    /**
     * Get quick statistics for a specific child
     */
    public Map<String, Object> getChildQuickStats(int studentId) {
        return parentDAO.getChildQuickStats(studentId);
    }
    
    /**
     * Get overview statistics for all children of a parent
     */
    public Map<String, Object> getChildrenOverview(int userId) {
        return parentDAO.getParentChildrenOverview(userId);
    }
    
    /**
     * Get detailed child information including statistics
     */
    public Map<String, Object> getChildDetail(int userId, int studentId) {
        // Verify that this child belongs to the parent
        List<Student> children = getParentChildren(userId);
        Student targetChild = children.stream()
            .filter(child -> child.getId() == studentId)
            .findFirst()
            .orElse(null);
            
        if (targetChild == null) {
            return null; // Child doesn't belong to this parent
        }
        
        Map<String, Object> childDetail = new HashMap<>();
        childDetail.put("student", targetChild);
        childDetail.put("stats", getChildQuickStats(studentId));
        
        return childDetail;
    }
    
    /**
     * Get child profile information for display
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getChildProfile(int userId, int studentId) {
        Map<String, Object> childDetail = getChildDetail(userId, studentId);
        if (childDetail == null) {
            return null;
        }
        
        Student student = (Student) childDetail.get("student");
        Map<String, Object> stats = (Map<String, Object>) childDetail.get("stats");
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", student.getId());
        profile.put("name", student.getName());
        profile.put("age", student.getAge());
        profile.put("dob", student.getDob());
        profile.put("class_name", student.getClassName());
        profile.put("address", student.getAddress());
        profile.put("profile_image", student.getProfileImage());
        
        // Add statistics
        profile.put("attendance_rate", stats.get("attendance_rate"));
        profile.put("total_days", stats.get("total_days"));
        profile.put("present_days", stats.get("present_days"));
        profile.put("absent_days", stats.get("absent_days"));
        profile.put("late_days", stats.get("late_days"));
        profile.put("development_records", stats.get("development_records"));
        
        return profile;
    }
    
    /**
     * Update child profile image (with security validation)
     */
    public boolean updateChildProfileImage(int userId, int studentId, byte[] profileImageData) {
        // Verify that this child belongs to the parent
        List<Student> children = getParentChildren(userId);
        boolean isParentChild = children.stream()
            .anyMatch(child -> child.getId() == studentId);
            
        if (!isParentChild) {
            System.err.println("Unauthorized attempt to update profile image for student " + studentId + " by user " + userId);
            return false; // Child doesn't belong to this parent
        }
        
        // Update the profile image
        return studentDAO.updateProfileImage(studentId, profileImageData);
    }
}
