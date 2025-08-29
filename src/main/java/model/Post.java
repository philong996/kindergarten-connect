package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Post model class representing teacher posts with scheduling and photo support
 */
public class Post {
    private int id;
    private String title;
    private String content;
    private int authorId;
    private Integer classId; // Nullable - post might be for all classes
    private byte[] photoAttachment; // Binary data for photo uploads
    private String photoFilename; // Original filename for the photo
    private LocalDate scheduledDate; // For scheduling posts in advance (null for immediate)
    private String visibility; // ALL, PARENTS_ONLY, TEACHERS_ONLY
    private boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display purposes
    private String authorName; // For joining with users table
    private String className; // For joining with classes table
    private int commentCount; // Count of approved comments
    
    // Constructors
    public Post() {
        this.visibility = "ALL";
        this.isPublished = true;
    }
    
    public Post(String title, String content, int authorId, Integer classId) {
        this();
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.classId = classId;
    }
    
    public Post(int id, String title, String content, int authorId, Integer classId, 
                byte[] photoAttachment, String photoFilename, LocalDate scheduledDate,
                String visibility, boolean isPublished, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.classId = classId;
        this.photoAttachment = photoAttachment;
        this.photoFilename = photoFilename;
        this.scheduledDate = scheduledDate;
        this.visibility = visibility != null ? visibility : "ALL";
        this.isPublished = isPublished;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    
    public byte[] getPhotoAttachment() { return photoAttachment; }
    public void setPhotoAttachment(byte[] photoAttachment) { this.photoAttachment = photoAttachment; }
    
    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }
    
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    
    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Display fields
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    
    // Helper methods
    public boolean hasPhoto() {
        return photoAttachment != null && photoAttachment.length > 0;
    }
    
    public boolean isScheduled() {
        return scheduledDate != null && scheduledDate.isAfter(LocalDate.now());
    }
    
    public boolean shouldBeVisible() {
        return isPublished && (scheduledDate == null || !scheduledDate.isAfter(LocalDate.now()));
    }
    
    public String getVisibilityDisplay() {
        switch (visibility) {
            case "PARENTS_ONLY": return "Parents Only";
            case "TEACHERS_ONLY": return "Teachers Only";
            default: return "All Users";
        }
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorId=" + authorId +
                ", classId=" + classId +
                ", visibility='" + visibility + '\'' +
                ", isPublished=" + isPublished +
                ", scheduledDate=" + scheduledDate +
                '}';
    }
}
