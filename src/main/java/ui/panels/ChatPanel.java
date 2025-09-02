package ui.panels;

import model.ChatMessage;
import model.Conversation;
import service.AuthService;
import service.ChatService;
import ui.components.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Modern Chat Panel - WhatsApp/Telegram style messaging interface
 * Features: Conversation list, chat window, message bubbles, real-time feel
 */
public class ChatPanel extends JPanel {
    private final ChatService chatService;
    private final int currentUserId;
    private final String currentUserRole;
    
    // UI Components
    private JPanel conversationListPanel;
    private JPanel chatAreaPanel;
    private JScrollPane conversationScrollPane;
    private JScrollPane chatScrollPane;
    private JPanel messagesContainer;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton attachButton;
    private JLabel chatHeaderLabel;
    private JButton newChatButton;
    private JButton refreshButton;
    private JPanel inputPanel;
    
    // Current state
    private Conversation currentConversation;
    private File selectedAttachment;
    
    // Date formatters
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Colors and styling
    private static final Color SENT_BUBBLE_COLOR = new Color(0, 132, 255);
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 240, 240);
    private static final Color CHAT_BACKGROUND = Color.WHITE;
    private static final Color SIDEBAR_COLOR = new Color(248, 249, 250);
    
    public ChatPanel(int currentUserId, String currentUserRole, AuthService authService) {
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
        this.chatService = new ChatService();
        
        System.out.println("=== ChatPanel Constructor ===");
        System.out.println("User ID: " + currentUserId);
        System.out.println("User Role: " + currentUserRole);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadConversations();
        
        // Set initial header without refresh button
        updateChatHeader("Select a conversation to start chatting", false);
        
        System.out.println("=== ChatPanel Initialization Complete ===");
    }
    
    private void initializeComponents() {
        // Conversation list panel (left sidebar)
        conversationListPanel = new JPanel();
        conversationListPanel.setLayout(new BoxLayout(conversationListPanel, BoxLayout.Y_AXIS));
        conversationListPanel.setBackground(SIDEBAR_COLOR);
        
        conversationScrollPane = new JScrollPane(conversationListPanel);
        conversationScrollPane.setPreferredSize(new Dimension(300, 0));
        conversationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        conversationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        conversationScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        
        // Chat area panel (right side)
        chatAreaPanel = new JPanel(new BorderLayout());
        chatAreaPanel.setBackground(CHAT_BACKGROUND);
        
        // Messages container
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(CHAT_BACKGROUND);
        
        chatScrollPane = new JScrollPane(messagesContainer);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Chat header
        chatHeaderLabel = new JLabel("Select a conversation to start chatting", SwingConstants.CENTER);
        chatHeaderLabel.setFont(CustomFont.getMonospacedFont(Font.BOLD, 16f));
        chatHeaderLabel.setOpaque(true);
        chatHeaderLabel.setBackground(new Color(245, 245, 245));
        chatHeaderLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Message input area
        inputPanel = createMessageInputPanel();
        
        // New chat button
        newChatButton = new CustomButton("+ New Chat");
        
        // Refresh button
        refreshButton = new CustomButton("🔄 Refresh");
        refreshButton.setToolTipText("Refresh messages and conversations");
        
        // Setup chat area
        chatAreaPanel.add(chatHeaderLabel, BorderLayout.NORTH);
        chatAreaPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatAreaPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Initially hide input panel
        inputPanel.setVisible(false);
    }
    
    private JPanel createMessageInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        inputPanel.setBackground(CHAT_BACKGROUND);
        
        // Attachment button
        attachButton = new JButton("📎");
        attachButton.setPreferredSize(new Dimension(40, 35));
        attachButton.setToolTipText("Attach file");
        
        // Message input field
        messageInputField = new JTextField();
        messageInputField.setFont(CustomFont.getMonospacedFont(14f));
        messageInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Send button
        sendButton = new CustomButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 35));
        
        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(attachButton, BorderLayout.WEST);
        leftPanel.add(messageInputField, BorderLayout.CENTER);
        
        inputPanel.add(leftPanel, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Left sidebar with conversations
        JPanel leftSidebar = new JPanel(new BorderLayout());
        leftSidebar.setBackground(SIDEBAR_COLOR);
        
        // Sidebar header
        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setBackground(SIDEBAR_COLOR);
        sidebarHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Chats");
        titleLabel.setFont(CustomFont.getMonospacedFont(Font.BOLD, 18f));
        
        sidebarHeader.add(titleLabel, BorderLayout.WEST);
        sidebarHeader.add(newChatButton, BorderLayout.EAST);
        
        leftSidebar.add(sidebarHeader, BorderLayout.NORTH);
        leftSidebar.add(conversationScrollPane, BorderLayout.CENTER);
        
        // Main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(leftSidebar);
        splitPane.setRightComponent(chatAreaPanel);
        splitPane.setResizeWeight(0.0); // Keep left sidebar fixed width
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // New chat button
        newChatButton.addActionListener(e -> showNewChatDialog());
        
        // Send button
        sendButton.addActionListener(e -> sendMessage());
        
        // Attach button
        attachButton.addActionListener(e -> selectAttachment());
        
        // Refresh button
        refreshButton.addActionListener(e -> refreshChat());
        
        // Enter key to send message
        messageInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }
    
    private void loadConversations() {
        SwingUtilities.invokeLater(() -> {
            conversationListPanel.removeAll();
            
            try {
                List<Conversation> conversations = chatService.getUserConversations(currentUserId);
                
                if (conversations.isEmpty()) {
                    JLabel emptyLabel = new JLabel("<html><center>No conversations yet<br>Click 'New Chat' to start</center></html>", SwingConstants.CENTER);
                    emptyLabel.setFont(CustomFont.getMonospacedFont(14f));
                    emptyLabel.setForeground(Color.GRAY);
                    conversationListPanel.add(Box.createVerticalGlue());
                    conversationListPanel.add(emptyLabel);
                    conversationListPanel.add(Box.createVerticalGlue());
                } else {
                    for (Conversation conversation : conversations) {
                        JPanel conversationCard = createConversationCard(conversation);
                        conversationListPanel.add(conversationCard);
                    }
                }
                
                conversationListPanel.revalidate();
                conversationListPanel.repaint();
                
            } catch (Exception e) {
                e.printStackTrace();
                DialogFactory.showError(this, "Failed to load conversations: " + e.getMessage());
            }
        });
    }
    
    private JPanel createConversationCard(Conversation conversation) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(SIDEBAR_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Participant info
        String participantName = conversation.getOtherParticipantName(currentUserId);
        String participantRole = conversation.getOtherParticipantRole(currentUserId);
        
        JLabel nameLabel = new JLabel(participantName);
        nameLabel.setFont(CustomFont.getMonospacedFont(Font.BOLD, 14f));
        
        JLabel roleLabel = new JLabel(participantRole);
        roleLabel.setFont(CustomFont.getMonospacedFont(12f));
        roleLabel.setForeground(Color.GRAY);
        
        // Last message preview
        String lastMessage = conversation.getLastMessage();
        if (lastMessage == null || lastMessage.trim().isEmpty()) {
            lastMessage = "No messages yet";
        }
        if (lastMessage.length() > 40) {
            lastMessage = lastMessage.substring(0, 40) + "...";
        }
        
        JLabel messageLabel = new JLabel(lastMessage);
        messageLabel.setFont(CustomFont.getMonospacedFont(12f));
        messageLabel.setForeground(Color.GRAY);
        
        // Time and unread count
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        if (conversation.getLastMessageAt() != null) {
            JLabel timeLabel = new JLabel(conversation.getLastMessageAt().format(TIME_FORMATTER));
            timeLabel.setFont(CustomFont.getMonospacedFont(11f));
            timeLabel.setForeground(Color.GRAY);
            rightPanel.add(timeLabel, BorderLayout.NORTH);
        }
        
        if (conversation.getUnreadCount() > 0) {
            JLabel unreadLabel = new JLabel(String.valueOf(conversation.getUnreadCount()));
            unreadLabel.setFont(CustomFont.getMonospacedFont(Font.BOLD, 11f));
            unreadLabel.setForeground(Color.WHITE);
            unreadLabel.setOpaque(true);
            unreadLabel.setBackground(new Color(255, 59, 48));
            unreadLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            unreadLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rightPanel.add(unreadLabel, BorderLayout.SOUTH);
        }
        
        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(nameLabel, BorderLayout.WEST);
        topPanel.add(roleLabel, BorderLayout.EAST);
        
        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(messageLabel, BorderLayout.SOUTH);
        
        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        // Click handler
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openConversation(conversation);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(230, 230, 230));
                card.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(SIDEBAR_COLOR);
                card.repaint();
            }
        });
        
        return card;
    }
    
    private void openConversation(Conversation conversation) {
        this.currentConversation = conversation;
        
        // Update header with participant info and refresh button
        String participantName = conversation.getOtherParticipantName(currentUserId);
        String participantRole = conversation.getOtherParticipantRole(currentUserId);
        updateChatHeader(participantName + " (" + participantRole + ")", true);
        
        // Show input panel
        inputPanel.setVisible(true);
        inputPanel.revalidate();
        inputPanel.repaint();
        
        // Refresh the chat area
        chatAreaPanel.revalidate();
        chatAreaPanel.repaint();
        
        // Load messages
        loadChatMessages();
        
        // Mark messages as read
        chatService.markMessagesAsRead(conversation.getId(), currentUserId);
    }
    
    private void loadChatMessages() {
        SwingUtilities.invokeLater(() -> {
            messagesContainer.removeAll();
            
            if (currentConversation == null) {
                return;
            }
            
            try {
                List<ChatMessage> messages = chatService.getRecentMessages(currentConversation.getId(), 50);
                
                if (messages.isEmpty()) {
                    JLabel emptyLabel = new JLabel("No messages yet. Start the conversation!", SwingConstants.CENTER);
                    emptyLabel.setFont(CustomFont.getMonospacedFont(14f));
                    emptyLabel.setForeground(Color.GRAY);
                    messagesContainer.add(Box.createVerticalGlue());
                    messagesContainer.add(emptyLabel);
                    messagesContainer.add(Box.createVerticalGlue());
                } else {
                    for (ChatMessage message : messages) {
                        JPanel messagePanel = createMessageBubble(message);
                        messagesContainer.add(messagePanel);
                        messagesContainer.add(Box.createVerticalStrut(8));
                    }
                }
                
                messagesContainer.revalidate();
                messagesContainer.repaint();
                
                // Scroll to bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
                    verticalBar.setValue(verticalBar.getMaximum());
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                DialogFactory.showError(this, "Failed to load messages: " + e.getMessage());
            }
        });
    }
    
    private JPanel createMessageBubble(ChatMessage message) {
        boolean isFromCurrentUser = message.isFromCurrentUser(currentUserId);
        
        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // Message content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(isFromCurrentUser ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Rounded corners effect (simplified)
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isFromCurrentUser ? SENT_BUBBLE_COLOR : RECEIVED_BUBBLE_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Message text
        JTextArea messageText = new JTextArea(message.getContent());
        messageText.setEditable(false);
        messageText.setOpaque(false);
        messageText.setWrapStyleWord(true);
        messageText.setLineWrap(true);
        messageText.setFont(CustomFont.getMonospacedFont(13f));
        messageText.setForeground(isFromCurrentUser ? Color.WHITE : Color.BLACK);
        messageText.setBorder(null);
        
        contentPanel.add(messageText, BorderLayout.CENTER);
        
        // Attachment handling
        if (message.hasAttachment()) {
            JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
            attachmentPanel.setOpaque(false);
            
            JLabel attachmentIcon = new JLabel("📎 " + message.getAttachmentFilename());
            attachmentIcon.setFont(CustomFont.getMonospacedFont(12f));
            attachmentIcon.setForeground(isFromCurrentUser ? Color.WHITE : Color.BLUE);
            attachmentIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            attachmentIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    downloadAttachment(message);
                }
            });
            
            attachmentPanel.add(attachmentIcon);
            contentPanel.add(attachmentPanel, BorderLayout.SOUTH);
        }
        
        // Time label
        JLabel timeLabel = new JLabel(message.getSentAt().format(TIME_FORMATTER));
        timeLabel.setFont(CustomFont.getMonospacedFont(10f));
        timeLabel.setForeground(isFromCurrentUser ? new Color(255, 255, 255, 180) : Color.GRAY);
        
        JPanel timePanel = new JPanel(new FlowLayout(isFromCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 2));
        timePanel.setOpaque(false);
        timePanel.add(timeLabel);
        
        contentPanel.add(timePanel, BorderLayout.SOUTH);
        
        // Wrapper for alignment
        JPanel wrapper = new JPanel(new FlowLayout(isFromCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(contentPanel);
        
        // Limit bubble width
        contentPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        contentPanel.setPreferredSize(new Dimension(
            Math.min(400, Math.max(200, messageText.getPreferredSize().width + 30)),
            contentPanel.getPreferredSize().height
        ));
        
        bubblePanel.add(wrapper, BorderLayout.CENTER);
        
        return bubblePanel;
    }
    
    private void sendMessage() {
        if (currentConversation == null) {
            DialogFactory.showWarning(this, "Please select a conversation first");
            return;
        }
        
        String content = messageInputField.getText().trim();
        
        if (content.isEmpty() && selectedAttachment == null) {
            return; // Nothing to send
        }
        
        try {
            byte[] attachmentData = null;
            String attachmentFilename = null;
            String attachmentMimeType = null;
            
            if (selectedAttachment != null) {
                attachmentData = Files.readAllBytes(selectedAttachment.toPath());
                attachmentFilename = selectedAttachment.getName();
                attachmentMimeType = Files.probeContentType(selectedAttachment.toPath());
                if (attachmentMimeType == null) {
                    attachmentMimeType = "application/octet-stream";
                }
            }
            
            boolean success = chatService.sendMessage(
                currentConversation.getId(),
                currentUserId,
                content,
                attachmentData,
                attachmentFilename,
                attachmentMimeType
            );
            
            if (success) {
                messageInputField.setText("");
                selectedAttachment = null;
                attachButton.setText("📎");
                loadChatMessages();
                loadConversations(); // Refresh to update last message
            } else {
                DialogFactory.showError(this, "Failed to send message");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            DialogFactory.showError(this, "Error reading attachment: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showError(this, "Failed to send message: " + e.getMessage());
        }
    }
    
    private void selectAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Common files", "pdf", "doc", "docx", "txt", "jpg", "jpeg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedAttachment = fileChooser.getSelectedFile();
            attachButton.setText("📎 " + selectedAttachment.getName());
        }
    }
    
    private void downloadAttachment(ChatMessage message) {
        if (!message.hasAttachment()) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(message.getAttachmentFilename()));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File saveFile = fileChooser.getSelectedFile();
                Files.write(saveFile.toPath(), message.getAttachment());
                DialogFactory.showSuccess(this, "Attachment saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                DialogFactory.showError(this, "Failed to save attachment: " + e.getMessage());
            }
        }
    }
    
    private void showNewChatDialog() {
        JDialog newChatDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Start New Chat", true);
        newChatDialog.setSize(400, 300);
        newChatDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Instructions
        JLabel instructionLabel = new JLabel("Select a " + 
            (currentUserRole.equalsIgnoreCase("teacher") ? "parent" : "teacher") + " to start chatting with:");
        instructionLabel.setFont(CustomFont.getMonospacedFont(14f));
        
        // User list
        JList<Map<String, Object>> userList = new JList<>();
        DefaultListModel<Map<String, Object>> listModel = new DefaultListModel<>();
        
        List<Map<String, Object>> availableUsers = chatService.getAvailableRecipients(currentUserRole);
        for (Map<String, Object> user : availableUsers) {
            listModel.addElement(user);
        }
        
        userList.setModel(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Map) {
                    Map<String, Object> user = (Map<String, Object>) value;
                    setText(user.get("full_name") + " (" + user.get("username") + ")");
                }
                return this;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(userList);
        listScrollPane.setPreferredSize(new Dimension(350, 150));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startChatButton = new CustomButton("Start Chat");
        JButton cancelButton = new CustomButton("Cancel");
        
        startChatButton.addActionListener(e -> {
            Map<String, Object> selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                int otherUserId = (Integer) selectedUser.get("id");
                Conversation conversation = chatService.getOrCreateConversation(currentUserId, otherUserId);
                if (conversation != null) {
                    newChatDialog.dispose();
                    loadConversations();
                    openConversation(conversation);
                } else {
                    DialogFactory.showError(newChatDialog, "Failed to create conversation");
                }
            } else {
                DialogFactory.showWarning(newChatDialog, "Please select a user");
            }
        });
        
        cancelButton.addActionListener(e -> newChatDialog.dispose());
        
        buttonPanel.add(startChatButton);
        buttonPanel.add(cancelButton);
        
        contentPanel.add(instructionLabel, BorderLayout.NORTH);
        contentPanel.add(listScrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        newChatDialog.add(contentPanel);
        newChatDialog.setVisible(true);
    }
    
    private void updateChatHeader(String title, boolean showRefreshButton) {
        // Remove any existing header components from NORTH position
        Component northComponent = ((BorderLayout) chatAreaPanel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (northComponent != null) {
            chatAreaPanel.remove(northComponent);
        }
        
        if (showRefreshButton) {
            // Create a header panel with title and refresh button
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(true);
            headerPanel.setBackground(new Color(245, 245, 245));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(CustomFont.getMonospacedFont(Font.BOLD, 16f));
            
            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(refreshButton, BorderLayout.EAST);
            
            chatAreaPanel.add(headerPanel, BorderLayout.NORTH);
        } else {
            // Use the simple label
            chatHeaderLabel.setText(title);
            chatAreaPanel.add(chatHeaderLabel, BorderLayout.NORTH);
        }
        
        chatAreaPanel.revalidate();
        chatAreaPanel.repaint();
    }
    
    private void refreshChat() {
        // Show immediate feedback
        String originalText = refreshButton.getText();
        refreshButton.setText("🔄 Refreshing...");
        refreshButton.setEnabled(false);
        
        // Refresh conversation list
        loadConversations();
        
        // If a conversation is currently open, refresh its messages
        if (currentConversation != null) {
            loadChatMessages();
            // Mark messages as read again after refresh
            chatService.markMessagesAsRead(currentConversation.getId(), currentUserId);
        }
        
        // Show completion feedback after a brief delay
        Timer timer = new Timer(800, e -> {
            refreshButton.setText("✓ Refreshed");
            Timer restoreTimer = new Timer(1200, event -> {
                refreshButton.setText(originalText);
                refreshButton.setEnabled(true);
            });
            restoreTimer.setRepeats(false);
            restoreTimer.start();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
