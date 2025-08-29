package service;

import dao.PostDAO;
import dao.CommentDAO;
import dao.ClassDAO;
import model.Post;
import model.Comment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service class for post management with business logic for scheduling and visibility rules
 */
public class PostService {
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;
    private final ClassDAO classDAO;
    
    public PostService() {
        this.postDAO = new PostDAO();
        this.commentDAO = new CommentDAO();
        this.classDAO = new ClassDAO();
    }
    
    /**
     * Create a new post with validation
     */
    public boolean createPost(Post post) {
        if (!validatePost(post)) {
            return false;
        }
        
        // If no scheduled date is set, the post is published immediately
        if (post.getScheduledDate() == null) {
            post.setPublished(true);
        } else {
            // If scheduled for future, mark as published but won't be visible until date
            post.setPublished(true);
        }
        
        return postDAO.createPost(post);
    }
    
    /**
     * Update an existing post
     */
    public boolean updatePost(Post post) {
        if (!validatePost(post)) {
            return false;
        }
        
        return postDAO.updatePost(post);
    }
    
    /**
     * Delete a post (only by author)
     */
    public boolean deletePost(int postId, int authorId) {
        return postDAO.deletePost(postId, authorId);
    }
    
    /**
     * Get posts visible to parents in a specific class
     */
    public List<Post> getVisiblePostsForParents(int classId) {
        return postDAO.getVisiblePostsForParents(classId);
    }
    
    /**
     * Get all posts by a teacher (for management)
     */
    public List<Post> getPostsByTeacher(int teacherId) {
        return postDAO.getPostsByAuthor(teacherId);
    }
    
    /**
     * Get posts for a specific class (all visibility levels for teachers)
     */
    public List<Post> getPostsByClass(int classId) {
        return postDAO.getPostsByClass(classId);
    }
    
    /**
     * Get a specific post by ID
     */
    public Post getPostById(int postId) {
        return postDAO.getPostById(postId);
    }
    
    /**
     * Schedule a post for future publication
     */
    public boolean schedulePost(Post post, LocalDate scheduledDate) {
        if (scheduledDate.isBefore(LocalDate.now())) {
            return false; // Cannot schedule for the past
        }
        
        post.setScheduledDate(scheduledDate);
        post.setPublished(true); // Mark as published but won't be visible until scheduled date
        
        return postDAO.createPost(post);
    }
    
    /**
     * Get posts scheduled for today (for automated publishing)
     */
    public List<Post> getScheduledPostsForToday() {
        return postDAO.getScheduledPostsForToday();
    }
    
    /**
     * Add a comment to a post
     */
    public boolean addComment(Comment comment) {
        if (!validateComment(comment)) {
            return false;
        }
        
        // Comments require approval by default (set in Comment constructor)
        return commentDAO.createComment(comment);
    }
    
    /**
     * Get approved comments for a post
     */
    public List<Comment> getApprovedComments(int postId) {
        return commentDAO.getApprovedCommentsByPost(postId);
    }
    
    /**
     * Get all comments for a post (for teacher moderation)
     */
    public List<Comment> getAllComments(int postId) {
        return commentDAO.getAllCommentsByPost(postId);
    }
    
    /**
     * Get pending comments for teacher moderation
     */
    public List<Comment> getPendingComments(int teacherId) {
        return commentDAO.getPendingCommentsByTeacher(teacherId);
    }
    
    /**
     * Approve a comment
     */
    public boolean approveComment(int commentId) {
        return commentDAO.approveComment(commentId);
    }
    
    /**
     * Reject a comment
     */
    public boolean rejectComment(int commentId) {
        return commentDAO.rejectComment(commentId);
    }
    
    /**
     * Delete a comment by teacher (moderation)
     */
    public boolean deleteCommentByTeacher(int commentId, int teacherId) {
        return commentDAO.deleteCommentByTeacher(commentId, teacherId);
    }
    
    /**
     * Delete a comment by its author
     */
    public boolean deleteComment(int commentId, int authorId) {
        return commentDAO.deleteComment(commentId, authorId);
    }
    
    /**
     * Get post statistics for a teacher
     */
    public Map<String, Object> getPostStatistics(int teacherId) {
        return postDAO.getPostStatistics(teacherId);
    }
    
    /**
     * Get comment statistics for a teacher
     */
    public Map<String, Object> getCommentStatistics(int teacherId) {
        return commentDAO.getCommentStatistics(teacherId);
    }
    
    /**
     * Check if a user can view a specific post based on visibility rules
     */
    public boolean canUserViewPost(Post post, String userRole, int userId, int userClassId) {
        // Check if post is published and not scheduled for future
        if (!post.shouldBeVisible()) {
            return false;
        }
        
        // Check class access
        if (post.getClassId() != null && post.getClassId() != userClassId) {
            return false;
        }
        
        // Check visibility rules
        switch (post.getVisibility()) {
            case "TEACHERS_ONLY":
                return "TEACHER".equals(userRole) || "PRINCIPAL".equals(userRole);
            case "PARENTS_ONLY":
                return "PARENT".equals(userRole);
            case "ALL":
            default:
                return true;
        }
    }
    
    /**
     * Check if a user can edit a specific post
     */
    public boolean canUserEditPost(Post post, int userId) {
        return post.getAuthorId() == userId;
    }
    
    /**
     * Validate post data
     */
    private boolean validatePost(Post post) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return false;
        }
        
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            return false;
        }
        
        if (post.getAuthorId() <= 0) {
            return false;
        }
        
        // Validate visibility
        String visibility = post.getVisibility();
        if (!"ALL".equals(visibility) && !"PARENTS_ONLY".equals(visibility) && !"TEACHERS_ONLY".equals(visibility)) {
            return false;
        }
        
        // Validate class exists if specified
        if (post.getClassId() != null && post.getClassId() > 0) {
            try {
                return classDAO.findById(post.getClassId()) != null;
            } catch (Exception e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate comment data
     */
    private boolean validateComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return false;
        }
        
        if (comment.getPostId() <= 0 || comment.getAuthorId() <= 0) {
            return false;
        }
        
        // Validate that the post exists
        return postDAO.getPostById(comment.getPostId()) != null;
    }
    
    /**
     * Summary class for post dashboard
     */
    public static class PostSummary {
        private final Map<String, Object> postStats;
        private final Map<String, Object> commentStats;
        private final List<Comment> pendingComments;
        
        public PostSummary(Map<String, Object> postStats, Map<String, Object> commentStats, List<Comment> pendingComments) {
            this.postStats = postStats;
            this.commentStats = commentStats;
            this.pendingComments = pendingComments;
        }
        
        public Map<String, Object> getPostStats() { return postStats; }
        public Map<String, Object> getCommentStats() { return commentStats; }
        public List<Comment> getPendingComments() { return pendingComments; }
    }
    
    /**
     * Get comprehensive post summary for teacher dashboard
     */
    public PostSummary getPostSummary(int teacherId) {
        Map<String, Object> postStats = getPostStatistics(teacherId);
        Map<String, Object> commentStats = getCommentStatistics(teacherId);
        List<Comment> pendingComments = getPendingComments(teacherId);
        
        return new PostSummary(postStats, commentStats, pendingComments);
    }
}
