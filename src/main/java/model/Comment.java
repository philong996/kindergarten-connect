package model;

import java.time.LocalDateTime;

/**
 * Comment model class representing parent comments on teacher posts
 */
public class Comment {
    private int id;
    private int postId;
    private int authorId;
    private String content;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display purposes
    private String authorName; // For joining with users table
    private String postTitle; // For joining with posts table
    
    // Constructors
    public Comment() {
        this.isApproved = false; // Default to requiring approval
    }
    
    public Comment(int postId, int authorId, String content) {
        this();
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
    }
    
    public Comment(int id, int postId, int authorId, String content, boolean isApproved,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.isApproved = isApproved;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Display fields
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }
    
    // Helper methods
    public String getApprovalStatus() {
        return isApproved ? "Approved" : "Pending";
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", authorId=" + authorId +
                ", content='" + content + '\'' +
                ", isApproved=" + isApproved +
                ", createdAt=" + createdAt +
                '}';
    }
}
