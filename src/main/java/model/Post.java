package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Post model class representing both class activities and school announcements
 */
public class Post {
    // Post types
    public static final String TYPE_CLASS_ACTIVITY = "CLASS_ACTIVITY";
    public static final String TYPE_SCHOOL_ANNOUNCEMENT = "SCHOOL_ANNOUNCEMENT";
    
    private int id;
    private String title;
    private String content;
    private int authorId;
    private Integer classId; // Nullable - post might be for all classes
    private String postType; // CLASS_ACTIVITY or SCHOOL_ANNOUNCEMENT
    private String category; // For announcements: EVENT, HOLIDAY, SCHEDULE, GENERAL
    private byte[] photoAttachment; // Binary data for photo uploads
    private String photoFilename; // Original filename for the photo
    private LocalDate scheduledDate; // For scheduling posts in advance (null for immediate)
    private LocalDate eventDate; // For announcements: when the event will happen
    private String visibility; // ALL, PARENTS_ONLY, TEACHERS_ONLY
    private boolean isPublished;
    private boolean isPinned; // For important announcements
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
        this.postType = TYPE_CLASS_ACTIVITY;
        this.isPinned = false;
    }
    
    public Post(String title, String content, int authorId, Integer classId) {
        this();
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.classId = classId;
    }
    
    // Constructor for school announcements
    public Post(String title, String content, int authorId, String category, LocalDate eventDate) {
        this();
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.postType = TYPE_SCHOOL_ANNOUNCEMENT;
        this.category = category;
        this.eventDate = eventDate;
        this.classId = null; // School announcements are not class-specific
    }
    
    public Post(int id, String title, String content, int authorId, Integer classId, 
                String postType, String category, byte[] photoAttachment, String photoFilename, 
                LocalDate scheduledDate, LocalDate eventDate, String visibility, boolean isPublished, 
                boolean isPinned, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.classId = classId;
        this.postType = postType != null ? postType : TYPE_CLASS_ACTIVITY;
        this.category = category;
        this.photoAttachment = photoAttachment;
        this.photoFilename = photoFilename;
        this.scheduledDate = scheduledDate;
        this.eventDate = eventDate;
        this.visibility = visibility != null ? visibility : "ALL";
        this.isPublished = isPublished;
        this.isPinned = isPinned;
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
    
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public byte[] getPhotoAttachment() { return photoAttachment; }
    public void setPhotoAttachment(byte[] photoAttachment) { this.photoAttachment = photoAttachment; }
    
    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }
    
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    
    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }
    
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    
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
    
    public boolean isClassActivity() {
        return TYPE_CLASS_ACTIVITY.equals(postType);
    }
    
    public boolean isSchoolAnnouncement() {
        return TYPE_SCHOOL_ANNOUNCEMENT.equals(postType);
    }
    
    public boolean isUpcomingEvent() {
        return eventDate != null && eventDate.isAfter(LocalDate.now());
    }
    
    public String getPostTypeDisplay() {
        switch (postType) {
            case TYPE_SCHOOL_ANNOUNCEMENT: 
                return "School Announcement";
            case TYPE_CLASS_ACTIVITY:
            default:
                return "Class Activity";
        }
    }
    
    public String getCategoryDisplay() {
        if (category == null) return "";
        switch (category) {
            case "EVENT": return "Event";
            case "HOLIDAY": return "Holiday";
            case "SCHEDULE": return "Schedule Change";
            case "GENERAL": return "General";
            default: return category;
        }
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
                ", postType='" + postType + '\'' +
                ", category='" + category + '\'' +
                ", visibility='" + visibility + '\'' +
                ", isPublished=" + isPublished +
                ", isPinned=" + isPinned +
                ", scheduledDate=" + scheduledDate +
                ", eventDate=" + eventDate +
                '}';
    }
}
