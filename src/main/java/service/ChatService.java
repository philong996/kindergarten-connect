package service;

import dao.ChatMessageDAO;
import dao.ConversationDAO;
import dao.UserDAO;
import model.ChatMessage;
import model.Conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service class for chat functionality
 */
public class ChatService {
    private final ChatMessageDAO chatMessageDAO;
    private final ConversationDAO conversationDAO;
    private final UserDAO userDAO;
    
    public ChatService() {
        this.chatMessageDAO = new ChatMessageDAO();
        this.conversationDAO = new ConversationDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(int userId) {
        return conversationDAO.getUserConversations(userId);
    }
    
    /**
     * Get or create a conversation between two users
     */
    public Conversation getOrCreateConversation(int user1Id, int user2Id) {
        return conversationDAO.getOrCreateConversation(user1Id, user2Id);
    }
    
    /**
     * Send a message in a conversation
     */
    public boolean sendMessage(int conversationId, int senderId, String content, 
                              byte[] attachment, String attachmentFilename, String attachmentMimeType) {
        
        if (content == null || content.trim().isEmpty()) {
            if (attachment == null || attachment.length == 0) {
                return false; // Can't send empty message without attachment
            }
        }
        
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content != null ? content.trim() : "");
        message.setAttachment(attachment);
        message.setAttachmentFilename(attachmentFilename);
        message.setAttachmentMimeType(attachmentMimeType);
        message.setSentAt(LocalDateTime.now());
        
        boolean sent = chatMessageDAO.sendMessage(message);
        
        if (sent) {
            // Update conversation's last message
            String lastMessagePreview = content != null && !content.trim().isEmpty() ? 
                content.trim() : (attachment != null ? "📎 " + attachmentFilename : "");
            
            conversationDAO.updateLastMessage(conversationId, lastMessagePreview, message.getSentAt());
        }
        
        return sent;
    }
    
    /**
     * Get messages in a conversation
     */
    public List<ChatMessage> getConversationMessages(int conversationId) {
        return chatMessageDAO.getConversationMessages(conversationId);
    }
    
    /**
     * Get recent messages in a conversation (for initial load)
     */
    public List<ChatMessage> getRecentMessages(int conversationId, int limit) {
        return chatMessageDAO.getRecentMessages(conversationId, limit);
    }
    
    /**
     * Mark messages as read when user opens conversation
     */
    public boolean markMessagesAsRead(int conversationId, int userId) {
        return chatMessageDAO.markMessagesAsRead(conversationId, userId);
    }
    
    /**
     * Get unread message count for a conversation
     */
    public int getUnreadCount(int conversationId, int userId) {
        return chatMessageDAO.getUnreadCount(conversationId, userId);
    }
    
    /**
     * Get conversation by ID
     */
    public Conversation getConversationById(int conversationId) {
        return conversationDAO.getConversationById(conversationId);
    }
    
    /**
     * Get available users to start a conversation with (based on role)
     */
    public List<Map<String, Object>> getAvailableRecipients(String currentUserRole) {
        try {
            if ("teacher".equalsIgnoreCase(currentUserRole)) {
                return userDAO.getUsersByRole("parent");
            } else if ("parent".equalsIgnoreCase(currentUserRole)) {
                return userDAO.getUsersByRole("teacher");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }
    
    /**
     * Validate if users can chat with each other
     */
    public boolean canUsersChat(int user1Id, int user2Id) {
        try {
            Map<String, Object> user1 = userDAO.getUserById(user1Id);
            Map<String, Object> user2 = userDAO.getUserById(user2Id);
            
            if (user1 == null || user2 == null) {
                return false;
            }
            
            String role1 = (String) user1.get("role");
            String role2 = (String) user2.get("role");
            
            // Teachers can chat with parents and vice versa
            return ("teacher".equalsIgnoreCase(role1) && "parent".equalsIgnoreCase(role2)) ||
                   ("parent".equalsIgnoreCase(role1) && "teacher".equalsIgnoreCase(role2));
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
