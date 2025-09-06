package ui.panels;

import dao.ClassDAO;
import model.Comment;
import model.Post;
import service.AuthService;
import service.AuthorizationService;
import service.PostService;
import ui.components.*;
import util.AuthUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Enhanced Posts Panel with card-based layout for Class Activities and School Announcements
 * Features: Card-based post display, inline comments, no comment moderation
 */
public class PostsPanel extends JPanel {
    private final PostService postService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;
    private final ClassDAO classDAO;
    private final int currentUserId;
    private final String currentUserRole;
    
    // UI Components
    private JTabbedPane mainTabbedPane;
    private JPanel classActivitiesContainer;
    private JPanel schoolAnnouncementsContainer;
    private JScrollPane classActivitiesScrollPane;
    private JScrollPane announcementsScrollPane;
    private String currentPostFilter = Post.TYPE_CLASS_ACTIVITY;
    
    // Form field IDs
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_CLASS = "class";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_VISIBILITY = "visibility";
    private static final String FIELD_SCHEDULED_DATE = "scheduled_date";
    private static final String FIELD_EVENT_DATE = "event_date";
    private static final String FIELD_IMAGE = "image";
    
    // Selected items
    private byte[] selectedImageData;
    private String selectedImageFilename;
    
    public PostsPanel(int currentUserId, String currentUserRole, AuthService authService) {
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
        this.authService = authService;
        this.authorizationService = this.authService.getAuthorizationService();
        this.postService = new PostService();
        this.classDAO = new ClassDAO();
        
        // Debug: Print user information
        System.out.println("=== PostsPanel Constructor ===");
        System.out.println("User ID: " + currentUserId);
        System.out.println("User Role: " + currentUserRole);
        System.out.println("Auth Service: " + (authService != null ? "Available" : "NULL"));
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadClassActivities();
        
        System.out.println("=== PostsPanel Initialization Complete ===");
    }
    
    private void initializeComponents() {
        mainTabbedPane = new JTabbedPane();
        
        // Initialize containers for card layout
        classActivitiesContainer = new JPanel();
        classActivitiesContainer.setLayout(new BoxLayout(classActivitiesContainer, BoxLayout.Y_AXIS));
        classActivitiesContainer.setBackground(Color.WHITE);
        
        schoolAnnouncementsContainer = new JPanel();
        schoolAnnouncementsContainer.setLayout(new BoxLayout(schoolAnnouncementsContainer, BoxLayout.Y_AXIS));
        schoolAnnouncementsContainer.setBackground(Color.WHITE);
        
        // Create scroll panes
        classActivitiesScrollPane = new JScrollPane(classActivitiesContainer);
        classActivitiesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        classActivitiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        classActivitiesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        announcementsScrollPane = new JScrollPane(schoolAnnouncementsContainer);
        announcementsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        announcementsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        announcementsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create tabs
        JPanel classActivitiesTab = createPostTab(classActivitiesScrollPane, "Class Activities");
        JPanel announcementsTab = createPostTab(announcementsScrollPane, "School Announcements");
        
        mainTabbedPane.addTab("📚 Class Activities", classActivitiesTab);
        
        // Only show announcements tab for principals or if there are announcements to view
        if ("PRINCIPAL".equals(currentUserRole) || canViewAnnouncements()) {
            mainTabbedPane.addTab("📢 School Announcements", announcementsTab);
        }
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createPostTab(JScrollPane scrollPane, String title) {
        System.out.println("Creating tab: " + title + " for user role: " + currentUserRole);
        
        JPanel tabPanel = new JPanel(new BorderLayout());
        
        // Header with title and controls
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel("<html><h2>" + title + "</h2></html>"));
        
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            JButton createPostButton = new JButton("➕ Create New Post");
            createPostButton.addActionListener(e -> showCreatePostDialog(title));
            headerPanel.add(createPostButton);
            
            JButton refreshButton = new JButton("🔄 Refresh");
            refreshButton.addActionListener(e -> refreshCurrentView());
            headerPanel.add(refreshButton);
        }
        
        tabPanel.add(headerPanel, BorderLayout.NORTH);
        tabPanel.add(scrollPane, BorderLayout.CENTER);
        
        return tabPanel;
    }
    
    private void setupEventHandlers() {
        mainTabbedPane.addChangeListener(e -> {
            int selectedTab = mainTabbedPane.getSelectedIndex();
            if (selectedTab == 0) {
                currentPostFilter = Post.TYPE_CLASS_ACTIVITY;
                loadClassActivities();
            } else if (selectedTab == 1) {
                currentPostFilter = Post.TYPE_SCHOOL_ANNOUNCEMENT;
                loadSchoolAnnouncements();
            }
        });
    }
    
    private void loadClassActivities() {
        try {
            List<Post> posts = getPostsByType(Post.TYPE_CLASS_ACTIVITY);
            displayPostsAsCards(posts, classActivitiesContainer);
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading class activities: " + e.getMessage());
        }
    }
    
    private void loadSchoolAnnouncements() {
        try {
            List<Post> posts = getPostsByType(Post.TYPE_SCHOOL_ANNOUNCEMENT);
            System.out.println("=== Loading School Announcements ===");
            System.out.println("Found " + posts.size() + " school announcement posts");
            for (Post post : posts) {
                System.out.println("- Post: " + post.getTitle() + " (Author: " + post.getAuthorId() + ", Type: " + post.getPostType() + ")");
            }
            displayPostsAsCards(posts, schoolAnnouncementsContainer);
        } catch (Exception e) {
            System.err.println("Error loading school announcements: " + e.getMessage());
            e.printStackTrace();
            DialogFactory.showError(this, "Error loading announcements: " + e.getMessage());
        }
    }
    
    private List<Post> getPostsByType(String postType) {
        List<Post> allPosts;
        
        System.out.println("=== Getting Posts by Type: " + postType + " ===");
        System.out.println("Current User ID: " + currentUserId + ", Role: " + currentUserRole);
        
        if (Post.TYPE_SCHOOL_ANNOUNCEMENT.equals(postType)) {
            // For school announcements, get all posts and filter by type
            // School announcements can be created by both teachers and principals and should be visible to all
            if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
                // Get posts by current user first
                allPosts = postService.getPostsByTeacher(currentUserId);
                System.out.println("Posts by current user (" + currentUserId + "): " + allPosts.size());
                
                // Also get posts from other teachers and principals for school announcements
                // This ensures teachers can see announcements created by other teachers/principals
                List<Post> otherPosts = new java.util.ArrayList<>();
                
                // Get principal's posts (user ID 1) if current user is not principal
                if (currentUserId != 1) {
                    List<Post> principalPosts = postService.getPostsByTeacher(1);
                    System.out.println("Posts by principal (1): " + principalPosts.size());
                    otherPosts.addAll(principalPosts);
                }
                
                // For a more complete solution, we could query all users with TEACHER/PRINCIPAL role
                // but for now, this covers the main use case
                allPosts.addAll(otherPosts);
                System.out.println("Total posts before filtering: " + allPosts.size());
            } else {
                // Parents see posts for their children's classes
                int[] classIds = authorizationService.getAccessibleClassIds();
                allPosts = java.util.Arrays.stream(classIds)
                    .boxed()
                    .flatMap(classId -> postService.getVisiblePostsForParents(classId).stream())
                    .collect(java.util.stream.Collectors.toList());
                
                // Also get school-wide announcements (posts with null class_id)
                // For now, we'll assume these are included in the above query
            }
        } else {
            // For class activities, use the existing logic
            if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
                allPosts = postService.getPostsByTeacher(currentUserId);
            } else {
                // Parents see posts for their children's classes
                int[] classIds = authorizationService.getAccessibleClassIds();
                allPosts = java.util.Arrays.stream(classIds)
                    .boxed()
                    .flatMap(classId -> postService.getVisiblePostsForParents(classId).stream())
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        List<Post> filteredPosts = allPosts.stream()
            .filter(post -> postType.equals(post.getPostType()))
            .sorted((p1, p2) -> {
                // Sort pinned posts first, then by creation date
                if (p1.isPinned() && !p2.isPinned()) return -1;
                if (!p1.isPinned() && p2.isPinned()) return 1;
                return p2.getCreatedAt().compareTo(p1.getCreatedAt());
            })
            .collect(java.util.stream.Collectors.toList());
            
        System.out.println("Filtered posts of type " + postType + ": " + filteredPosts.size());
        return filteredPosts;
    }
    
    private void displayPostsAsCards(List<Post> posts, JPanel container) {
        container.removeAll();
        
        if (posts.isEmpty()) {
            JLabel emptyLabel = new JLabel("<html><div style='text-align: center; color: gray;'>" +
                                         "<h3>No posts available</h3>" +
                                         "<p>No posts to display at this time.</p></div></html>");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            container.add(emptyLabel);
        } else {
            for (Post post : posts) {
                JPanel postCard = createPostCard(post);
                container.add(postCard);
                container.add(Box.createVerticalStrut(10)); // Spacing between cards
            }
        }
        
        container.revalidate();
        container.repaint();
    }
    
    private JPanel createPostCard(Post post) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        // Dynamic card sizing based on content
        boolean hasImage = post.getPhotoAttachment() != null && post.getPhotoAttachment().length > 0;
        int baseHeight = hasImage ? 500 : 250; // Larger height for posts with images
        
        // Set preferred and maximum size for proper display
        card.setPreferredSize(new Dimension(600, baseHeight));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, hasImage ? 800 : 400)); // Much larger max for images
        card.setMinimumSize(new Dimension(400, baseHeight));
        
        // Header with title and metadata
        JPanel headerPanel = createPostHeader(post);
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = createPostContent(post);
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Comments section
        JPanel commentsPanel = createCommentsSection(post);
        card.add(commentsPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createPostHeader(Post post) {
        JPanel header = new JPanel(new BorderLayout());
        
        // Title and pin indicator
        String titleText = post.getTitle();
        if (post.isPinned()) {
            titleText = "📌 " + titleText;
        }
        JLabel titleLabel = new JLabel("<html><h3>" + titleText + "</h3></html>");
        header.add(titleLabel, BorderLayout.WEST);
        
        // Metadata panel
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Post type badge
        JLabel typeLabel = new JLabel(post.getPostTypeDisplay());
        typeLabel.setOpaque(true);
        typeLabel.setBackground(post.isSchoolAnnouncement() ? new Color(255, 165, 0) : new Color(100, 149, 237));
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        metaPanel.add(typeLabel);
        
        // Category for announcements
        if (post.isSchoolAnnouncement() && post.getCategory() != null) {
            JLabel categoryLabel = new JLabel(post.getCategoryDisplay());
            categoryLabel.setOpaque(true);
            categoryLabel.setBackground(Color.GRAY);
            categoryLabel.setForeground(Color.WHITE);
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            metaPanel.add(categoryLabel);
        }
        
        // Class name for class activities
        if (post.isClassActivity() && post.getClassName() != null) {
            JLabel classLabel = new JLabel(post.getClassName());
            classLabel.setOpaque(true);
            classLabel.setBackground(new Color(60, 179, 113));
            classLabel.setForeground(Color.WHITE);
            classLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            metaPanel.add(classLabel);
        }
        
        header.add(metaPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createPostContent(Post post) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Check if post has image for layout decisions
        boolean hasImage = post.getPhotoAttachment() != null && post.getPhotoAttachment().length > 0;
        
        // Main content panel to hold text and potentially image
        JPanel mainContent = new JPanel(new BorderLayout());
        
        // Main content with proper text wrapping
        JTextArea contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setOpaque(false);
        contentArea.setFont(contentArea.getFont().deriveFont(14f)); // Slightly larger font
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Adjust text area size based on image presence
        if (hasImage) {
            contentArea.setPreferredSize(new Dimension(500, 40)); // Smaller for posts with images
            contentArea.setRows(2);
        } else {
            contentArea.setPreferredSize(new Dimension(500, 60));
            contentArea.setRows(3);
        }
        
        // Wrap in scroll pane if content is long
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScrollPane.setBorder(null);
        contentScrollPane.setOpaque(false);
        contentScrollPane.getViewport().setOpaque(false);
        contentScrollPane.setPreferredSize(new Dimension(500, hasImage ? 60 : 80));
        
        mainContent.add(contentScrollPane, BorderLayout.NORTH);
        
        // Add image if present
        if (hasImage) {
            JPanel imagePanel = createImagePanel(post.getPhotoAttachment());
            if (imagePanel != null) {
                mainContent.add(imagePanel, BorderLayout.CENTER);
            }
        }
        
        content.add(mainContent, BorderLayout.CENTER);
        
        // Metadata row with better spacing
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        metaRow.setOpaque(false);
        
        // Create colored labels for metadata
        JLabel dateLabel = createMetaLabel("📅 " + (post.getCreatedAt() != null ? 
                              post.getCreatedAt().toLocalDate().toString() : "Unknown"), 
                              new Color(100, 149, 237));
        metaRow.add(dateLabel);
        
        if (post.getEventDate() != null) {
            JLabel eventLabel = createMetaLabel("🎯 Event: " + post.getEventDate().toString(), 
                                              new Color(255, 140, 0));
            metaRow.add(eventLabel);
        }
        
        if (post.getScheduledDate() != null) {
            JLabel scheduleLabel = createMetaLabel("⏰ Scheduled: " + post.getScheduledDate().toString(), 
                                                 new Color(50, 205, 50));
            metaRow.add(scheduleLabel);
        }
        
        JLabel visibilityLabel = createMetaLabel("👁️ " + post.getVisibilityDisplay(), 
                                               new Color(128, 128, 128));
        metaRow.add(visibilityLabel);
        
        content.add(metaRow, BorderLayout.SOUTH);
        
        return content;
    }
    
    /**
     * Create an image panel from byte array data
     */
    private JPanel createImagePanel(byte[] imageData) {
        try {
            // Convert byte array to image
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                return null;
            }
            
            // Scale image if it's too large
            int maxWidth = 400;
            int maxHeight = 300;
            
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            
            // Calculate scaling
            double scaleX = (double) maxWidth / originalWidth;
            double scaleY = (double) maxHeight / originalHeight;
            double scale = Math.min(scaleX, scaleY);
            
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            
            // Create scaled image
            Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            
            // Create panel with image
            JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            imagePanel.setOpaque(true);
            imagePanel.setBackground(Color.WHITE);
            imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            imagePanel.add(imageLabel);
            
            // Set explicit size to ensure visibility
            imagePanel.setPreferredSize(new Dimension(scaledWidth + 20, scaledHeight + 20));
            imagePanel.setMinimumSize(new Dimension(scaledWidth + 20, scaledHeight + 20));
            
            return imagePanel;
            
        } catch (IOException e) {
            System.err.println("Error displaying image: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error in createImagePanel: " + e.getMessage());
            return null;
        }
    }
    
    private JLabel createMetaLabel(String text, Color backgroundColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(backgroundColor);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        label.setFont(label.getFont().deriveFont(11f));
        return label;
    }
    
    private JPanel createCommentsSection(Post post) {
        JPanel commentsSection = new JPanel(new BorderLayout());
        commentsSection.setBorder(BorderFactory.createTitledBorder("Comments"));
        
        // Load and display comments
        List<Comment> comments = new java.util.ArrayList<>();
        try {
            comments = postService.getApprovedComments(post.getId());
        } catch (Exception e) {
            // Keep empty list if error occurs
        }
        
        if (comments.isEmpty()) {
            JLabel noCommentsLabel = new JLabel("No comments yet. Be the first to comment!");
            noCommentsLabel.setForeground(Color.GRAY);
            commentsSection.add(noCommentsLabel, BorderLayout.CENTER);
        } else {
            JPanel commentsContainer = new JPanel();
            commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));
            
            for (Comment comment : comments) {
                JPanel commentPanel = createCommentPanel(comment);
                commentsContainer.add(commentPanel);
                commentsContainer.add(Box.createVerticalStrut(5));
            }
            
            JScrollPane commentsScrollPane = new JScrollPane(commentsContainer);
            commentsScrollPane.setPreferredSize(new Dimension(0, Math.min(150, comments.size() * 50)));
            commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            commentsSection.add(commentsScrollPane, BorderLayout.CENTER);
        }
        
        // Add comment form for parents or teachers
        if ("PARENT".equals(currentUserRole) || "TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            JPanel addCommentPanel = createAddCommentPanel(post);
            commentsSection.add(addCommentPanel, BorderLayout.SOUTH);
        }
        
        return commentsSection;
    }
    
    private JPanel createCommentPanel(Comment comment) {
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        commentPanel.setBackground(new Color(248, 249, 250));
        
        // Comment header with author and date
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        headerPanel.add(new JLabel("<html><b>" + comment.getAuthorName() + "</b></html>"));
        headerPanel.add(new JLabel("•"));
        headerPanel.add(new JLabel(comment.getCreatedAt() != null ? 
                                 comment.getCreatedAt().toLocalDate().toString() : "Unknown"));
        
        // Comment content
        JTextArea commentContent = new JTextArea(comment.getContent());
        commentContent.setEditable(false);
        commentContent.setWrapStyleWord(true);
        commentContent.setLineWrap(true);
        commentContent.setOpaque(false);
        
        commentPanel.add(headerPanel, BorderLayout.NORTH);
        commentPanel.add(commentContent, BorderLayout.CENTER);
        
        return commentPanel;
    }
    
    private JPanel createAddCommentPanel(Post post) {
        JPanel addCommentPanel = new JPanel(new BorderLayout());
        addCommentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JTextArea commentTextArea = new JTextArea(2, 0);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setLineWrap(true);
        commentTextArea.setBorder(BorderFactory.createLoweredBevelBorder());
        commentTextArea.setBackground(Color.WHITE);
        
        JButton addCommentButton = new JButton("Add Comment");
        addCommentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String commentText = commentTextArea.getText().trim();
                if (!commentText.isEmpty()) {
                    addComment(post, commentText);
                    commentTextArea.setText("");
                    refreshCurrentView(); // Refresh to show new comment
                }
            }
        });
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Add a comment:"), BorderLayout.NORTH);
        inputPanel.add(commentTextArea, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addCommentButton);
        
        addCommentPanel.add(inputPanel, BorderLayout.CENTER);
        addCommentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return addCommentPanel;
    }
    
    private void addComment(Post post, String content) {
        try {
            Comment comment = new Comment(post.getId(), currentUserId, content);
            
            boolean success = postService.addApprovedComment(comment);
            if (success) {
                DialogFactory.showSuccess(this, "Comment added successfully!");
            } else {
                DialogFactory.showError(this, "Failed to add comment.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error adding comment: " + e.getMessage());
        }
    }
    
    /**
     * Show create post dialog as a popup
     */
    private void showCreatePostDialog(String tabTitle) {
        if (!AuthUtil.checkPermissionWithMessage(authService,
                AuthorizationService.PERM_CREATE_POSTS, "create posts")) {
            return;
        }
        
        // Determine post type based on tab title
        String postType;
        if (tabTitle.contains("Class Activities")) {
            postType = Post.TYPE_CLASS_ACTIVITY;
        } else {
            postType = Post.TYPE_SCHOOL_ANNOUNCEMENT;
        }
        
        // Create FormBuilder for the popup
        FormBuilder dialogFormBuilder = createFormBuilderForPostType(tabTitle);
        
        // Create the dialog
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogFactory.FormDialog dialog = DialogFactory.showFormDialog(
            parentFrame, 
            "Create New " + (postType.equals(Post.TYPE_CLASS_ACTIVITY) ? "Class Activity" : "School Announcement"),
            dialogFormBuilder
        );
        
        // Check if user clicked OK
        if (dialog.isOkClicked()) {
            try {
                Post post = createPostFromFormBuilder(dialogFormBuilder, postType);
                
                boolean success = postService.createPost(post);
                if (success) {
                    DialogFactory.showSuccess(this, 
                        post.isSchoolAnnouncement() ? "Announcement created successfully!" : "Post created successfully!");
                    refreshCurrentView();
                } else {
                    DialogFactory.showError(this, "Failed to create post.");
                }
            } catch (Exception e) {
                DialogFactory.showError(this, "Error creating post: " + e.getMessage());
            }
        }
    }
    
    private void refreshCurrentView() {
        if (Post.TYPE_CLASS_ACTIVITY.equals(currentPostFilter)) {
            loadClassActivities();
        } else {
            loadSchoolAnnouncements();
        }
    }
    
    private int extractClassIdFromSelection(String classSelection) {
        if (classSelection == null || classSelection.isEmpty() || "All Classes".equals(classSelection)) {
            return 0;
        }
        
        int idStart = classSelection.lastIndexOf("ID: ");
        if (idStart != -1) {
            int idEnd = classSelection.lastIndexOf(")");
            if (idEnd > idStart) {
                try {
                    String idStr = classSelection.substring(idStart + 4, idEnd);
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    // Ignore parsing error
                }
            }
        }
        
        return 0;
    }
    
    private boolean canViewAnnouncements() {
        // Parents and teachers can view announcements
        return true;
    }
    
    /**
     * Create a FormBuilder instance configured for a specific post type
     */
    private FormBuilder createFormBuilderForPostType(String tabTitle) {
        String postType;
        if (tabTitle.contains("Class Activities")) {
            postType = Post.TYPE_CLASS_ACTIVITY;
        } else {
            postType = Post.TYPE_SCHOOL_ANNOUNCEMENT;
        }
        
        System.out.println("Creating FormBuilder for tab: " + tabTitle + ", postType: " + postType);
        
        FormBuilder formBuilder = new FormBuilder("Create New Post", 2);
        formBuilder.addTextField(FIELD_TITLE, "Title", true)
                  .addTextArea(FIELD_CONTENT, "Content", 4, true);
        
        // Add image upload field
        addImageUploadField(formBuilder);
        
        // Add form fields based on post type
        if (Post.TYPE_CLASS_ACTIVITY.equals(postType)) {
            formBuilder.addComboBox(FIELD_CLASS, "Class", new String[]{"Loading..."}, true);
            // Load classes for this specific form
            loadAvailableClassesForForm(formBuilder);
        } else {
            formBuilder.addComboBox(FIELD_CATEGORY, "Category", 
                                 new String[]{"GENERAL", "EVENT", "HOLIDAY", "SCHEDULE"}, true)
                      .addDateField(FIELD_EVENT_DATE, "Event Date (optional)", false);
        }
        
        formBuilder.addComboBox(FIELD_VISIBILITY, "Visibility", 
                             new String[]{"ALL", "PARENTS_ONLY", "TEACHERS_ONLY"}, true)
                  .addDateField(FIELD_SCHEDULED_DATE, "Scheduled Date (optional)", false);
        
        return formBuilder;
    }
    
    /**
     * Add image upload field to the form builder
     */
    private void addImageUploadField(FormBuilder formBuilder) {
        // Create a panel for image selection
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectImageButton = new JButton("📷 Select Image");
        JLabel imageStatusLabel = new JLabel("No image selected");
        imageStatusLabel.setForeground(Color.GRAY);
        
        // Clear previous image selection when creating new form
        selectedImageData = null;
        selectedImageFilename = null;
        
        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            // Set file filter for images
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif", "bmp");
            fileChooser.setFileFilter(filter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // Read and store image data
                    BufferedImage image = ImageIO.read(selectedFile);
                    if (image == null) {
                        DialogFactory.showError(this, "Invalid image file.");
                        return;
                    }
                    
                    // Convert to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String fileName = selectedFile.getName();
                    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                    
                    // Ensure we use a supported format
                    if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
                        extension = "png"; // Default to PNG
                    }
                    
                    ImageIO.write(image, extension, baos);
                    selectedImageData = baos.toByteArray();
                    selectedImageFilename = fileName;
                    
                    // Update status label
                    imageStatusLabel.setText("Selected: " + fileName);
                    imageStatusLabel.setForeground(Color.BLUE);
                    
                } catch (IOException ex) {
                    DialogFactory.showError(this, "Error reading image file: " + ex.getMessage());
                    selectedImageData = null;
                    selectedImageFilename = null;
                    imageStatusLabel.setText("Error loading image");
                    imageStatusLabel.setForeground(Color.RED);
                }
            }
        });
        
        imagePanel.add(selectImageButton);
        imagePanel.add(imageStatusLabel);
        
        // Add as a custom field to the form builder
        formBuilder.addCustomField(FIELD_IMAGE, "Post Image (optional)", imagePanel, false);
    }
    
    /**
     * Load available classes for a specific FormBuilder instance
     */
    private void loadAvailableClassesForForm(FormBuilder formBuilder) {
        try {
            java.util.List<model.Class> classes;
            
            if ("TEACHER".equals(currentUserRole)) {
                classes = classDAO.findByTeacherId(currentUserId);
            } else {
                classes = classDAO.findAll();
            }
            
            String[] classOptions = new String[classes.size() + 1];
            classOptions[0] = "All Classes";
            
            for (int i = 0; i < classes.size(); i++) {
                model.Class clazz = classes.get(i);
                classOptions[i + 1] = clazz.getName() + " (ID: " + clazz.getId() + ")";
            }
            
            // Update the combo box
            @SuppressWarnings("unchecked")
            JComboBox<String> classCombo = (JComboBox<String>) formBuilder.getField(FIELD_CLASS).getInputComponent();
            classCombo.setModel(new DefaultComboBoxModel<>(classOptions));
            
        } catch (Exception e) {
            System.err.println("Error loading classes for form: " + e.getMessage());
        }
    }
    
    /**
     * Create post from FormBuilder instance (works for both dialogs and tabs)
     */
    private Post createPostFromFormBuilder(FormBuilder formBuilder, String postType) throws Exception {
        String title = formBuilder.getValue(FIELD_TITLE).trim();
        String content = formBuilder.getValue(FIELD_CONTENT).trim();
        
        if (title.isEmpty() || content.isEmpty()) {
            throw new Exception("Title and content are required.");
        }
        
        Post post = new Post(title, content, currentUserId, null);
        post.setPostType(postType);
        
        // Handle class activities
        if (Post.TYPE_CLASS_ACTIVITY.equals(postType)) {
            String classSelection = formBuilder.getValue(FIELD_CLASS).trim();
            if (!classSelection.isEmpty() && !"All Classes".equals(classSelection)) {
                try {
                    int classId = extractClassIdFromSelection(classSelection);
                    if (classId > 0) {
                        post.setClassId(classId);
                    }
                } catch (Exception e) {
                    // Ignore class selection error, post will be for all classes
                }
            }
        }
        
        // Handle school announcements
        if (Post.TYPE_SCHOOL_ANNOUNCEMENT.equals(postType)) {
            String category = formBuilder.getValue(FIELD_CATEGORY).trim();
            if (!category.isEmpty()) {
                post.setCategory(category);
            }
            
            String eventDateStr = formBuilder.getValue(FIELD_EVENT_DATE).trim();
            if (!eventDateStr.isEmpty()) {
                try {
                    LocalDate eventDate = LocalDate.parse(eventDateStr);
                    post.setEventDate(eventDate);
                } catch (DateTimeParseException e) {
                    throw new Exception("Invalid event date format. Please use YYYY-MM-DD format.");
                }
            }
            
            // For now, default to not pinned (would need custom checkbox implementation)
            post.setPinned(false);
        }
        
        // Common fields
        String visibility = formBuilder.getValue(FIELD_VISIBILITY).trim();
        if (!visibility.isEmpty()) {
            post.setVisibility(visibility);
        }
        
        String scheduledDateStr = formBuilder.getValue(FIELD_SCHEDULED_DATE).trim();
        if (!scheduledDateStr.isEmpty()) {
            try {
                LocalDate scheduledDate = LocalDate.parse(scheduledDateStr);
                post.setScheduledDate(scheduledDate);
            } catch (DateTimeParseException e) {
                throw new Exception("Invalid scheduled date format. Please use YYYY-MM-DD format.");
            }
        }
        
        // Handle image attachment
        if (selectedImageData != null && selectedImageData.length > 0) {
            post.setPhotoAttachment(selectedImageData);
            post.setPhotoFilename(selectedImageFilename != null ? selectedImageFilename : "post_image.jpg");
        }
        
        return post;
    }
}
