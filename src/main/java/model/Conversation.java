package model;

import java.time.LocalDateTime;

/**
 * Conversation model representing a chat conversation between two users
 */
public class Conversation {
    private int id;
    private int participant1Id;
    private int participant2Id;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private boolean isActive;
    
    // Additional fields for display (from joins)
    private String participant1Name;
    private String participant1Role;
    private String participant2Name;
    private String participant2Role;
    
    // Constructors
    public Conversation() {
        this.isActive = true;
        this.unreadCount = 0;
    }
    
    public Conversation(int participant1Id, int participant2Id) {
        this();
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getParticipant1Id() {
        return participant1Id;
    }
    
    public void setParticipant1Id(int participant1Id) {
        this.participant1Id = participant1Id;
    }
    
    public int getParticipant2Id() {
        return participant2Id;
    }
    
    public void setParticipant2Id(int participant2Id) {
        this.participant2Id = participant2Id;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getParticipant1Name() {
        return participant1Name;
    }
    
    public void setParticipant1Name(String participant1Name) {
        this.participant1Name = participant1Name;
    }
    
    public String getParticipant1Role() {
        return participant1Role;
    }
    
    public void setParticipant1Role(String participant1Role) {
        this.participant1Role = participant1Role;
    }
    
    public String getParticipant2Name() {
        return participant2Name;
    }
    
    public void setParticipant2Name(String participant2Name) {
        this.participant2Name = participant2Name;
    }
    
    public String getParticipant2Role() {
        return participant2Role;
    }
    
    public void setParticipant2Role(String participant2Role) {
        this.participant2Role = participant2Role;
    }
    
    /**
     * Get the other participant's name based on current user
     */
    public String getOtherParticipantName(int currentUserId) {
        if (currentUserId == participant1Id) {
            return participant2Name;
        } else {
            return participant1Name;
        }
    }
    
    /**
     * Get the other participant's role based on current user
     */
    public String getOtherParticipantRole(int currentUserId) {
        if (currentUserId == participant1Id) {
            return participant2Role;
        } else {
            return participant1Role;
        }
    }
    
    /**
     * Get the other participant's ID based on current user
     */
    public int getOtherParticipantId(int currentUserId) {
        if (currentUserId == participant1Id) {
            return participant2Id;
        } else {
            return participant1Id;
        }
    }
    
    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", participant1Id=" + participant1Id +
                ", participant2Id=" + participant2Id +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastMessageAt=" + lastMessageAt +
                ", unreadCount=" + unreadCount +
                ", isActive=" + isActive +
                '}';
    }
}
