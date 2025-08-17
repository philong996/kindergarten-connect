package service;

import dao.ParentDAO;
import model.Student;
import dao.StudentDAO;
import java.util.List;
import java.util.Map;
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
}
