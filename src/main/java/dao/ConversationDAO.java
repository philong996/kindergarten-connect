package dao;

import model.Conversation;
import util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing conversations between users
 */
public class ConversationDAO {
    
    /**
     * Get or create a conversation between two users
     */
    public Conversation getOrCreateConversation(int user1Id, int user2Id) {
        // Ensure consistent ordering (smaller ID first)
        int participant1Id = Math.min(user1Id, user2Id);
        int participant2Id = Math.max(user1Id, user2Id);
        
        String selectQuery = """
            SELECT c.id, c.participant1_id, c.participant2_id, c.last_message, 
                   c.last_message_at, c.is_active,
                   CASE 
                       WHEN u1.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u1.id LIMIT 1), 
                           u1.username
                       )
                       ELSE u1.username
                   END as participant1_name, u1.role as participant1_role,
                   CASE 
                       WHEN u2.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u2.id LIMIT 1), 
                           u2.username
                       )
                       ELSE u2.username
                   END as participant2_name, u2.role as participant2_role
            FROM conversations c
            JOIN users u1 ON c.participant1_id = u1.id
            JOIN users u2 ON c.participant2_id = u2.id
            WHERE c.participant1_id = ? AND c.participant2_id = ?
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
            
            stmt.setInt(1, participant1Id);
            stmt.setInt(2, participant2Id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            } else {
                // Create new conversation
                return createConversation(participant1Id, participant2Id);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a new conversation
     */
    private Conversation createConversation(int participant1Id, int participant2Id) {
        String insertQuery = """
            INSERT INTO conversations (participant1_id, participant2_id, is_active)
            VALUES (?, ?, true)
            RETURNING id
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            
            stmt.setInt(1, participant1Id);
            stmt.setInt(2, participant2Id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int conversationId = rs.getInt("id");
                return getConversationById(conversationId);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get conversation by ID with participant details
     */
    public Conversation getConversationById(int conversationId) {
        String query = """
            SELECT c.id, c.participant1_id, c.participant2_id, c.last_message, 
                   c.last_message_at, c.is_active,
                   CASE 
                       WHEN u1.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u1.id LIMIT 1), 
                           u1.username
                       )
                       ELSE u1.username
                   END as participant1_name, u1.role as participant1_role,
                   CASE 
                       WHEN u2.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u2.id LIMIT 1), 
                           u2.username
                       )
                       ELSE u2.username
                   END as participant2_name, u2.role as participant2_role
            FROM conversations c
            JOIN users u1 ON c.participant1_id = u1.id
            JOIN users u2 ON c.participant2_id = u2.id
            WHERE c.id = ?
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, conversationId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(int userId) {
        String query = """
            SELECT c.id, c.participant1_id, c.participant2_id, c.last_message, 
                   c.last_message_at, c.is_active,
                   CASE 
                       WHEN u1.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u1.id LIMIT 1), 
                           u1.username
                       )
                       ELSE u1.username
                   END as participant1_name, u1.role as participant1_role,
                   CASE 
                       WHEN u2.role = 'PARENT' THEN COALESCE(
                           (SELECT p.name FROM parents p WHERE p.user_id = u2.id LIMIT 1), 
                           u2.username
                       )
                       ELSE u2.username
                   END as participant2_name, u2.role as participant2_role,
                   COALESCE(unread.unread_count, 0) as unread_count
            FROM conversations c
            JOIN users u1 ON c.participant1_id = u1.id
            JOIN users u2 ON c.participant2_id = u2.id
            LEFT JOIN (
                SELECT conversation_id, COUNT(*) as unread_count
                FROM chat_messages
                WHERE is_read = false AND sender_id != ?
                GROUP BY conversation_id
            ) unread ON c.id = unread.conversation_id
            WHERE (c.participant1_id = ? OR c.participant2_id = ?) 
                  AND c.is_active = true
            ORDER BY c.last_message_at DESC NULLS LAST
            """;
            
        List<Conversation> conversations = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Conversation conv = mapResultSetToConversation(rs);
                conv.setUnreadCount(rs.getInt("unread_count"));
                conversations.add(conv);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return conversations;
    }
    
    /**
     * Update conversation's last message
     */
    public boolean updateLastMessage(int conversationId, String lastMessage, LocalDateTime timestamp) {
        String query = """
            UPDATE conversations 
            SET last_message = ?, last_message_at = ?
            WHERE id = ?
            """;
            
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, lastMessage);
            stmt.setTimestamp(2, Timestamp.valueOf(timestamp));
            stmt.setInt(3, conversationId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map ResultSet to Conversation object
     */
    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conversation = new Conversation();
        conversation.setId(rs.getInt("id"));
        conversation.setParticipant1Id(rs.getInt("participant1_id"));
        conversation.setParticipant2Id(rs.getInt("participant2_id"));
        conversation.setLastMessage(rs.getString("last_message"));
        
        Timestamp lastMessageAt = rs.getTimestamp("last_message_at");
        if (lastMessageAt != null) {
            conversation.setLastMessageAt(lastMessageAt.toLocalDateTime());
        }
        
        conversation.setActive(rs.getBoolean("is_active"));
        conversation.setParticipant1Name(rs.getString("participant1_name"));
        conversation.setParticipant1Role(rs.getString("participant1_role"));
        conversation.setParticipant2Name(rs.getString("participant2_name"));
        conversation.setParticipant2Role(rs.getString("participant2_role"));
        
        return conversation;
    }
}
