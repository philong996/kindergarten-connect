package model;

import java.time.LocalDateTime;

/**
 * ChatMessage model class for real-time messaging between teachers and parents
 * Designed for chat-style conversations rather than email-style messages
 */
public class ChatMessage {
    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private byte[] attachment;
    private String attachmentFilename;
    private String attachmentMimeType;
    private boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    
    // Additional fields for display purposes (from joins)
    private String senderName;
    private String senderRole;
    
    // Constructors
    public ChatMessage() {
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
    }
    
    public ChatMessage(int conversationId, int senderId, String content) {
        this();
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public byte[] getAttachment() {
        return attachment;
    }
    
    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }
    
    public String getAttachmentFilename() {
        return attachmentFilename;
    }
    
    public void setAttachmentFilename(String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }
    
    public String getAttachmentMimeType() {
        return attachmentMimeType;
    }
    
    public void setAttachmentMimeType(String attachmentMimeType) {
        this.attachmentMimeType = attachmentMimeType;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getSenderRole() {
        return senderRole;
    }
    
    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }
    
    public boolean hasAttachment() {
        return attachment != null && attachment.length > 0;
    }
    
    public boolean isFromCurrentUser(int currentUserId) {
        return this.senderId == currentUserId;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", hasAttachment=" + hasAttachment() +
                ", isRead=" + isRead +
                ", sentAt=" + sentAt +
                '}';
    }
}
