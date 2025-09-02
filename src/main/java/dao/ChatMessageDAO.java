package dao;

import model.ChatMessage;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing chat messages
 */
public class ChatMessageDAO {
    
    /**
     * Send a new chat message
     */
    public boolean sendMessage(ChatMessage message) {
        String query = """
            INSERT INTO chat_messages (conversation_id, sender_id, content, attachment, 
                                     attachment_filename, attachment_mime_type, sent_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, message.getConversationId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.setBytes(4, message.getAttachment());
            stmt.setString(5, message.getAttachmentFilename());
            stmt.setString(6, message.getAttachmentMimeType());
            stmt.setTimestamp(7, Timestamp.valueOf(message.getSentAt()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get all messages in a conversation
     */
    public List<ChatMessage> getConversationMessages(int conversationId) {
        String query = """
            SELECT cm.id, cm.conversation_id, cm.sender_id, cm.content, 
                   cm.attachment, cm.attachment_filename, cm.attachment_mime_type,
                   cm.is_read, cm.sent_at, cm.read_at,
                   CASE 
                       WHEN u.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                           u.username
                       )
                       ELSE u.username
                   END as sender_name, u.role as sender_role
            FROM chat_messages cm
            JOIN users u ON cm.sender_id = u.id
            WHERE cm.conversation_id = ?
            ORDER BY cm.sent_at ASC
            """;
            
        List<ChatMessage> messages = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, conversationId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToChatMessage(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Get recent messages in a conversation (limited count)
     */
    public List<ChatMessage> getRecentMessages(int conversationId, int limit) {
        String query = """
            SELECT cm.id, cm.conversation_id, cm.sender_id, cm.content, 
                   cm.attachment, cm.attachment_filename, cm.attachment_mime_type,
                   cm.is_read, cm.sent_at, cm.read_at,
                   CASE 
                       WHEN u.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                           u.username
                       )
                       ELSE u.username
                   END as sender_name, u.role as sender_role
            FROM chat_messages cm
            JOIN users u ON cm.sender_id = u.id
            WHERE cm.conversation_id = ?
            ORDER BY cm.sent_at DESC
            LIMIT ?
            """;
            
        List<ChatMessage> messages = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, conversationId);
            stmt.setInt(2, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToChatMessage(rs));
            }
            
            // Reverse to get chronological order
            messages.sort((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Mark messages as read
     */
    public boolean markMessagesAsRead(int conversationId, int userId) {
        String query = """
            UPDATE chat_messages 
            SET is_read = true, read_at = CURRENT_TIMESTAMP
            WHERE conversation_id = ? AND sender_id != ? AND is_read = false
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() >= 0; // Can be 0 if no unread messages
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get unread message count for a conversation
     */
    public int getUnreadCount(int conversationId, int userId) {
        String query = """
            SELECT COUNT(*) as unread_count
            FROM chat_messages
            WHERE conversation_id = ? AND sender_id != ? AND is_read = false
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("unread_count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get a specific message by ID
     */
    public ChatMessage getMessageById(int messageId) {
        String query = """
            SELECT cm.id, cm.conversation_id, cm.sender_id, cm.content, 
                   cm.attachment, cm.attachment_filename, cm.attachment_mime_type,
                   cm.is_read, cm.sent_at, cm.read_at,
                   CASE 
                       WHEN u.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u.id LIMIT 1), 
                           u.username
                       )
                       ELSE u.username
                   END as sender_name, u.role as sender_role
            FROM chat_messages cm
            JOIN users u ON cm.sender_id = u.id
            WHERE cm.id = ?
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, messageId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToChatMessage(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Map ResultSet to ChatMessage object
     */
    private ChatMessage mapResultSetToChatMessage(ResultSet rs) throws SQLException {
        ChatMessage message = new ChatMessage();
        message.setId(rs.getInt("id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setContent(rs.getString("content"));
        message.setAttachment(rs.getBytes("attachment"));
        message.setAttachmentFilename(rs.getString("attachment_filename"));
        message.setAttachmentMimeType(rs.getString("attachment_mime_type"));
        message.setRead(rs.getBoolean("is_read"));
        
        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) {
            message.setSentAt(sentAt.toLocalDateTime());
        }
        
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            message.setReadAt(readAt.toLocalDateTime());
        }
        
        message.setSenderName(rs.getString("sender_name"));
        message.setSenderRole(rs.getString("sender_role"));
        
        return message;
    }
}
