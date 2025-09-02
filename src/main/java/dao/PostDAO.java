package dao;

import model.Post;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Post operations with photo support and scheduling
 */
public class PostDAO {
    
    /**
     * Create a new post
     */
    public boolean createPost(Post post) {
        String sql = """
            INSERT INTO posts (title, content, author_id, class_id, post_type, category,
                             photo_attachment, photo_filename, scheduled_date, event_date,
                             visibility, is_published, is_pinned)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setInt(3, post.getAuthorId());
            
            if (post.getClassId() != null) {
                stmt.setInt(4, post.getClassId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            // Set post type (this was missing!)
            stmt.setString(5, post.getPostType() != null ? post.getPostType() : Post.TYPE_CLASS_ACTIVITY);
            
            // Set category (for announcements)
            if (post.getCategory() != null) {
                stmt.setString(6, post.getCategory());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
            
            if (post.getPhotoAttachment() != null) {
                stmt.setBytes(7, post.getPhotoAttachment());
                stmt.setString(8, post.getPhotoFilename());
            } else {
                stmt.setNull(7, Types.BINARY);
                stmt.setNull(8, Types.VARCHAR);
            }
            
            if (post.getScheduledDate() != null) {
                stmt.setDate(9, Date.valueOf(post.getScheduledDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            
            // Set event date (for announcements)
            if (post.getEventDate() != null) {
                stmt.setDate(10, Date.valueOf(post.getEventDate()));
            } else {
                stmt.setNull(10, Types.DATE);
            }
            
            stmt.setString(11, post.getVisibility());
            stmt.setBoolean(12, post.isPublished());
            stmt.setBoolean(13, post.isPinned());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating post: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all posts for a specific class with author information
     */
    public List<Post> getPostsByClass(int classId) {
        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, u.username as author_name, c.name as class_name,
                   COUNT(cm.id) as comment_count
            FROM posts p
            JOIN users u ON p.author_id = u.id
            LEFT JOIN classes c ON p.class_id = c.id
            LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
            WHERE p.class_id = ? AND p.is_published = true
              AND (p.scheduled_date IS NULL OR p.scheduled_date <= CURRENT_DATE)
            GROUP BY p.id, u.username, c.name
            ORDER BY p.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting posts by class: " + e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Get all posts by a specific author (teacher)
     */
    public List<Post> getPostsByAuthor(int authorId) {
        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, u.username as author_name, c.name as class_name,
                   COUNT(cm.id) as comment_count
            FROM posts p
            JOIN users u ON p.author_id = u.id
            LEFT JOIN classes c ON p.class_id = c.id
            LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
            WHERE p.author_id = ?
            GROUP BY p.id, u.username, c.name
            ORDER BY p.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, authorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting posts by author: " + e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Get all visible posts for parents in a specific class
     */
    public List<Post> getVisiblePostsForParents(int classId) {
        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, u.username as author_name, c.name as class_name,
                   COUNT(cm.id) as comment_count
            FROM posts p
            JOIN users u ON p.author_id = u.id
            LEFT JOIN classes c ON p.class_id = c.id
            LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
            WHERE p.class_id = ? AND p.is_published = true
              AND p.visibility IN ('ALL', 'PARENTS_ONLY')
              AND (p.scheduled_date IS NULL OR p.scheduled_date <= CURRENT_DATE)
            GROUP BY p.id, u.username, c.name
            ORDER BY p.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting visible posts for parents: " + e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Get scheduled posts that should be published today
     */
    public List<Post> getScheduledPostsForToday() {
        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, u.username as author_name, c.name as class_name,
                   0 as comment_count
            FROM posts p
            JOIN users u ON p.author_id = u.id
            LEFT JOIN classes c ON p.class_id = c.id
            WHERE p.scheduled_date = CURRENT_DATE AND p.is_published = true
            ORDER BY p.created_at DESC
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Post post = mapResultSetToPost(rs);
                posts.add(post);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting scheduled posts: " + e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Update a post
     */
    public boolean updatePost(Post post) {
        String sql = """
            UPDATE posts 
            SET title = ?, content = ?, class_id = ?, post_type = ?, category = ?,
                photo_attachment = ?, photo_filename = ?, scheduled_date = ?, event_date = ?,
                visibility = ?, is_published = ?, is_pinned = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND author_id = ?
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            
            if (post.getClassId() != null) {
                stmt.setInt(3, post.getClassId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, post.getPostType() != null ? post.getPostType() : Post.TYPE_CLASS_ACTIVITY);
            
            if (post.getCategory() != null) {
                stmt.setString(5, post.getCategory());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            
            if (post.getPhotoAttachment() != null) {
                stmt.setBytes(6, post.getPhotoAttachment());
                stmt.setString(7, post.getPhotoFilename());
            } else {
                stmt.setNull(6, Types.BINARY);
                stmt.setNull(7, Types.VARCHAR);
            }
            
            if (post.getScheduledDate() != null) {
                stmt.setDate(8, Date.valueOf(post.getScheduledDate()));
            } else {
                stmt.setNull(8, Types.DATE);
            }
            
            if (post.getEventDate() != null) {
                stmt.setDate(9, Date.valueOf(post.getEventDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            
            stmt.setString(10, post.getVisibility());
            stmt.setBoolean(11, post.isPublished());
            stmt.setBoolean(12, post.isPinned());
            stmt.setInt(13, post.getId());
            stmt.setInt(14, post.getAuthorId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating post: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a post (only by the author)
     */
    public boolean deletePost(int postId, int authorId) {
        String sql = "DELETE FROM posts WHERE id = ? AND author_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            stmt.setInt(2, authorId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting post: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a post by ID with full details
     */
    public Post getPostById(int postId) {
        String sql = """
            SELECT p.*, u.username as author_name, c.name as class_name,
                   COUNT(cm.id) as comment_count
            FROM posts p
            JOIN users u ON p.author_id = u.id
            LEFT JOIN classes c ON p.class_id = c.id
            LEFT JOIN comments cm ON p.id = cm.post_id AND cm.is_approved = true
            WHERE p.id = ?
            GROUP BY p.id, u.username, c.name
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting post by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get post statistics for a teacher
     */
    public Map<String, Object> getPostStatistics(int authorId) {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(*) as total_posts,
                SUM(CASE WHEN is_published THEN 1 ELSE 0 END) as published_posts,
                SUM(CASE WHEN scheduled_date > CURRENT_DATE THEN 1 ELSE 0 END) as scheduled_posts,
                SUM(CASE WHEN photo_attachment IS NOT NULL THEN 1 ELSE 0 END) as posts_with_photos
            FROM posts 
            WHERE author_id = ?
        """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, authorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_posts", rs.getInt("total_posts"));
                    stats.put("published_posts", rs.getInt("published_posts"));
                    stats.put("scheduled_posts", rs.getInt("scheduled_posts"));
                    stats.put("posts_with_photos", rs.getInt("posts_with_photos"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting post statistics: " + e.getMessage());
            stats.put("total_posts", 0);
            stats.put("published_posts", 0);
            stats.put("scheduled_posts", 0);
            stats.put("posts_with_photos", 0);
        }
        
        return stats;
    }
    
    /**
     * Map ResultSet to Post object
     */
    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setAuthorId(rs.getInt("author_id"));
        
        int classId = rs.getInt("class_id");
        if (!rs.wasNull()) {
            post.setClassId(classId);
        }
        
        // Set post type (this was missing!)
        post.setPostType(rs.getString("post_type"));
        
        // Set category (for announcements)
        post.setCategory(rs.getString("category"));
        
        post.setPhotoAttachment(rs.getBytes("photo_attachment"));
        post.setPhotoFilename(rs.getString("photo_filename"));
        
        Date scheduledDate = rs.getDate("scheduled_date");
        if (scheduledDate != null) {
            post.setScheduledDate(scheduledDate.toLocalDate());
        }
        
        // Set event date (for announcements)
        Date eventDate = rs.getDate("event_date");
        if (eventDate != null) {
            post.setEventDate(eventDate.toLocalDate());
        }
        
        post.setVisibility(rs.getString("visibility"));
        post.setPublished(rs.getBoolean("is_published"));
        post.setPinned(rs.getBoolean("is_pinned")); // This was missing!
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            post.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Display fields
        post.setAuthorName(rs.getString("author_name"));
        post.setClassName(rs.getString("class_name"));
        post.setCommentCount(rs.getInt("comment_count"));
        
        return post;
    }
}
