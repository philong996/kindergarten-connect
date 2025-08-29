package dao;

import model.Comment;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Comment operations with moderation support
 */
public class CommentDAO {
    
    /**
     * Create a new comment
     */
    public boolean createComment(Comment comment) {
        String sql = """
            INSERT INTO comments (post_id, author_id, content, is_approved)
            VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getAuthorId());
            stmt.setString(3, comment.getContent());
            stmt.setBoolean(4, comment.isApproved());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        comment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating comment: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all approved comments for a specific post
     */
    public List<Comment> getApprovedCommentsByPost(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = """
            SELECT c.*, u.username as author_name, p.title as post_title
            FROM comments c
            JOIN users u ON c.author_id = u.id
            JOIN posts p ON c.post_id = p.id
            WHERE c.post_id = ? AND c.is_approved = true
            ORDER BY c.created_at ASC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = mapResultSetToComment(rs);
                    comments.add(comment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting approved comments by post: " + e.getMessage());
        }
        
        return comments;
    }
    
    /**
     * Get all comments for a specific post (including unapproved for moderation)
     */
    public List<Comment> getAllCommentsByPost(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = """
            SELECT c.*, u.username as author_name, p.title as post_title
            FROM comments c
            JOIN users u ON c.author_id = u.id
            JOIN posts p ON c.post_id = p.id
            WHERE c.post_id = ?
            ORDER BY c.created_at ASC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = mapResultSetToComment(rs);
                    comments.add(comment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all comments by post: " + e.getMessage());
        }
        
        return comments;
    }
    
    /**
     * Get all pending comments for moderation by teacher
     */
    public List<Comment> getPendingCommentsByTeacher(int teacherId) {
        List<Comment> comments = new ArrayList<>();
        String sql = """
            SELECT c.*, u.username as author_name, p.title as post_title
            FROM comments c
            JOIN users u ON c.author_id = u.id
            JOIN posts p ON c.post_id = p.id
            WHERE p.author_id = ? AND c.is_approved = false
            ORDER BY c.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = mapResultSetToComment(rs);
                    comments.add(comment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending comments by teacher: " + e.getMessage());
        }
        
        return comments;
    }
    
    /**
     * Get comments by a specific author (parent)
     */
    public List<Comment> getCommentsByAuthor(int authorId) {
        List<Comment> comments = new ArrayList<>();
        String sql = """
            SELECT c.*, u.username as author_name, p.title as post_title
            FROM comments c
            JOIN users u ON c.author_id = u.id
            JOIN posts p ON c.post_id = p.id
            WHERE c.author_id = ?
            ORDER BY c.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, authorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = mapResultSetToComment(rs);
                    comments.add(comment);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting comments by author: " + e.getMessage());
        }
        
        return comments;
    }
    
    /**
     * Approve a comment
     */
    public boolean approveComment(int commentId) {
        String sql = "UPDATE comments SET is_approved = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error approving comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reject/unapprove a comment
     */
    public boolean rejectComment(int commentId) {
        String sql = "UPDATE comments SET is_approved = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error rejecting comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a comment
     */
    public boolean deleteComment(int commentId, int authorId) {
        String sql = "DELETE FROM comments WHERE id = ? AND author_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setInt(2, authorId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a comment by teacher (moderation)
     */
    public boolean deleteCommentByTeacher(int commentId, int teacherId) {
        String sql = """
            DELETE FROM comments 
            WHERE id = ? AND post_id IN (
                SELECT id FROM posts WHERE author_id = ?
            )
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setInt(2, teacherId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting comment by teacher: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get comment statistics for moderation
     */
    public Map<String, Object> getCommentStatistics(int teacherId) {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(*) as total_comments,
                SUM(CASE WHEN c.is_approved THEN 1 ELSE 0 END) as approved_comments,
                SUM(CASE WHEN NOT c.is_approved THEN 1 ELSE 0 END) as pending_comments
            FROM comments c
            JOIN posts p ON c.post_id = p.id
            WHERE p.author_id = ?
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_comments", rs.getInt("total_comments"));
                    stats.put("approved_comments", rs.getInt("approved_comments"));
                    stats.put("pending_comments", rs.getInt("pending_comments"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting comment statistics: " + e.getMessage());
            stats.put("total_comments", 0);
            stats.put("approved_comments", 0);
            stats.put("pending_comments", 0);
        }
        
        return stats;
    }
    
    /**
     * Get a comment by ID
     */
    public Comment getCommentById(int commentId) {
        String sql = """
            SELECT c.*, u.username as author_name, p.title as post_title
            FROM comments c
            JOIN users u ON c.author_id = u.id
            JOIN posts p ON c.post_id = p.id
            WHERE c.id = ?
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting comment by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Map ResultSet to Comment object
     */
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setContent(rs.getString("content"));
        comment.setApproved(rs.getBoolean("is_approved"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            comment.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            comment.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Display fields
        comment.setAuthorName(rs.getString("author_name"));
        comment.setPostTitle(rs.getString("post_title"));
        
        return comment;
    }
}
