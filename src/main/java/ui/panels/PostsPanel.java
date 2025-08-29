package ui.panels;

import dao.ClassDAO;
import model.Comment;
import model.Post;
import service.AuthService;
import service.AuthorizationService;
import service.PostService;
import ui.components.*;
import util.AuthUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Posts Panel for managing class posts with photo uploads, scheduling, and comment moderation
 */
public class PostsPanel extends JPanel {
    private final PostService postService;
    private final AuthService authService;
    private final AuthorizationService authorizationService;
    private final ClassDAO classDAO;
    private final int currentUserId;
    private final String currentUserRole;
    
    // UI Components
    private SearchPanel searchPanel;
    private DataTable postsTable;
    private FormBuilder postFormBuilder;
    private ButtonPanel postButtonPanel;
    private DataTable commentsTable;
    private FormBuilder commentFormBuilder;
    private ButtonPanel commentButtonPanel;
    private JTabbedPane mainTabbedPane;
    
    // Form field IDs
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_CLASS = "class";
    private static final String FIELD_VISIBILITY = "visibility";
    private static final String FIELD_SCHEDULED_DATE = "scheduled_date";
    private static final String FIELD_PHOTO = "photo";
    private static final String FIELD_COMMENT = "comment";
    
    // Selected items
    private Post selectedPost;
    private Comment selectedComment;
    private byte[] selectedPhotoData;
    private String selectedPhotoFilename;
    
    public PostsPanel(int currentUserId, String currentUserRole) {
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
        this.authService = new AuthService();
        this.authorizationService = this.authService.getAuthorizationService();
        this.postService = new PostService();
        this.classDAO = new ClassDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadPosts();
        loadComments();
    }
    
    private void initializeComponents() {
        // Create tabbed pane
        mainTabbedPane = new JTabbedPane();
        
        // Posts tab
        initializePostsTab();
        
        // Comments tab (only for teachers)
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            initializeCommentsTab();
        }
    }
    
    private void initializePostsTab() {
        // Search panel
        searchPanel = SearchPanel.createWithClear("Search posts:", searchTerm -> searchPosts(searchTerm), this::loadPosts);
        
        // Posts table
        String[] postColumns = {"ID", "Title", "Class", "Visibility", "Scheduled", "Comments", "Created"};
        postsTable = new DataTable(postColumns);
        postsTable.setRowSelectionHandler(this::onPostSelected);
        
        // Post form
        postFormBuilder = new FormBuilder("Post Information", 2);
        postFormBuilder.addTextField(FIELD_TITLE, "Title", true)
                      .addTextArea(FIELD_CONTENT, "Content", 4, true);
        
        // Only teachers can create posts
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            postFormBuilder.addComboBox(FIELD_CLASS, "Class", new String[]{"Loading..."}, false)
                          .addComboBox(FIELD_VISIBILITY, "Visibility", 
                                     new String[]{"ALL", "PARENTS_ONLY", "TEACHERS_ONLY"}, true)
                          .addDateField(FIELD_SCHEDULED_DATE, "Scheduled Date (optional)", false);
            
            // Photo upload button
            JButton photoButton = new JButton("Select Photo");
            photoButton.addActionListener(e -> selectPhoto());
            postFormBuilder.getField(FIELD_TITLE).add(photoButton);
            
            loadAvailableClasses();
            
            // Post management buttons
            postButtonPanel = ButtonPanel.createCrudPanel(
                e -> createPost(),
                e -> updatePost(),
                e -> deletePost(),
                e -> clearPostForm()
            );
        } else {
            // Parents can only view posts and add comments
            ButtonPanel parentButtonPanel = new ButtonPanel();
            parentButtonPanel.addButton("View Comments", e -> viewPostComments());
            postButtonPanel = parentButtonPanel;
        }
    }
    
    private void initializeCommentsTab() {
        // Comments table for moderation
        String[] commentColumns = {"ID", "Post", "Author", "Content", "Status", "Created"};
        commentsTable = new DataTable(commentColumns);
        commentsTable.setRowSelectionHandler(this::onCommentSelected);
        
        // Comment form for parent comment input
        commentFormBuilder = new FormBuilder("Add Comment", 1);
        commentFormBuilder.addTextArea(FIELD_COMMENT, "Your Comment", 3, true);
        
        // Comment management buttons
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            ButtonPanel teacherCommentPanel = new ButtonPanel();
            teacherCommentPanel.addStyledButton("Approve", e -> approveComment(), ButtonPanel.ButtonStyle.SUCCESS);
            teacherCommentPanel.addStyledButton("Reject", e -> rejectComment(), ButtonPanel.ButtonStyle.SECONDARY);
            teacherCommentPanel.addStyledButton("Delete", e -> deleteComment(), ButtonPanel.ButtonStyle.DANGER);
            teacherCommentPanel.addButton("Refresh", e -> loadComments());
            commentButtonPanel = teacherCommentPanel;
        } else {
            ButtonPanel parentCommentPanel = new ButtonPanel();
            parentCommentPanel.addButton("Add Comment", e -> addComment());
            commentButtonPanel = parentCommentPanel;
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Posts tab panel
        JPanel postsTabPanel = new JPanel(new BorderLayout());
        postsTabPanel.add(searchPanel, BorderLayout.NORTH);
        postsTabPanel.add(postsTable, BorderLayout.CENTER);
        
        JPanel postsBottomPanel = new JPanel(new BorderLayout());
        postsBottomPanel.add(postFormBuilder.build(), BorderLayout.CENTER);
        postsBottomPanel.add(postButtonPanel, BorderLayout.SOUTH);
        postsTabPanel.add(postsBottomPanel, BorderLayout.SOUTH);
        
        mainTabbedPane.addTab("Posts", postsTabPanel);
        
        // Comments tab panel (if available)
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            JPanel commentsTabPanel = new JPanel(new BorderLayout());
            commentsTabPanel.add(commentsTable, BorderLayout.CENTER);
            
            JPanel commentsBottomPanel = new JPanel(new BorderLayout());
            commentsBottomPanel.add(commentFormBuilder.build(), BorderLayout.CENTER);
            commentsBottomPanel.add(commentButtonPanel, BorderLayout.SOUTH);
            commentsTabPanel.add(commentsBottomPanel, BorderLayout.SOUTH);
            
            mainTabbedPane.addTab("Comment Moderation", commentsTabPanel);
        }
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Tab change handler to load appropriate data
        mainTabbedPane.addChangeListener(e -> {
            int selectedTab = mainTabbedPane.getSelectedIndex();
            if (selectedTab == 0) {
                loadPosts();
            } else if (selectedTab == 1) {
                loadComments();
            }
        });
    }
    
    private void loadData() {
        loadPosts();
    }
    
    private void loadPosts() {
        try {
            List<Post> posts;
            
            if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
                // Teachers see all their posts
                posts = postService.getPostsByTeacher(currentUserId);
            } else {
                // Parents see posts for their children's classes
                int[] classIds = authorizationService.getAccessibleClassIds();
                posts = java.util.Arrays.stream(classIds)
                    .boxed()
                    .flatMap(classId -> postService.getVisiblePostsForParents(classId).stream())
                    .distinct()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            updatePostsTable(posts);
            
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading posts: " + e.getMessage());
        }
    }
    
    private void updatePostsTable(List<Post> posts) {
        DefaultTableModel model = postsTable.getTableModel();
        model.setRowCount(0);
        
        for (Post post : posts) {
            Object[] rowData = {
                post.getId(),
                post.getTitle(),
                post.getClassName() != null ? post.getClassName() : "All Classes",
                post.getVisibilityDisplay(),
                post.getScheduledDate() != null ? post.getScheduledDate().toString() : "Immediate",
                post.getCommentCount(),
                post.getCreatedAt() != null ? post.getCreatedAt().toLocalDate().toString() : ""
            };
            model.addRow(rowData);
        }
    }
    
    private void loadComments() {
        if (!"TEACHER".equals(currentUserRole) && !"PRINCIPAL".equals(currentUserRole)) {
            return;
        }
        
        try {
            List<Comment> comments = postService.getPendingComments(currentUserId);
            updateCommentsTable(comments);
            
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading comments: " + e.getMessage());
        }
    }
    
    private void updateCommentsTable(List<Comment> comments) {
        DefaultTableModel model = commentsTable.getTableModel();
        model.setRowCount(0);
        
        for (Comment comment : comments) {
            Object[] rowData = {
                comment.getId(),
                comment.getPostTitle(),
                comment.getAuthorName(),
                comment.getContent().length() > 50 ? 
                    comment.getContent().substring(0, 50) + "..." : comment.getContent(),
                comment.getApprovalStatus(),
                comment.getCreatedAt() != null ? comment.getCreatedAt().toLocalDate().toString() : ""
            };
            model.addRow(rowData);
        }
    }
    
    private void onPostSelected(int row) {
        if (row >= 0) {
            int postId = (Integer) postsTable.getValueAt(row, 0);
            selectedPost = postService.getPostById(postId);
            
            if (selectedPost != null) {
                loadPostToForm(selectedPost);
                updatePostButtons();
            }
        }
    }
    
    private void onCommentSelected(int row) {
        if (row >= 0) {
            int commentId = (Integer) commentsTable.getValueAt(row, 0);
            // In a real implementation, you'd have a getCommentById method
            selectedComment = new Comment();
            selectedComment.setId(commentId);
            updateCommentButtons();
        }
    }
    
    private void loadPostToForm(Post post) {
        postFormBuilder.setValue(FIELD_TITLE, post.getTitle());
        postFormBuilder.setValue(FIELD_CONTENT, post.getContent());
        
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            if (post.getClassName() != null) {
                postFormBuilder.setValue(FIELD_CLASS, post.getClassName());
            }
            postFormBuilder.setValue(FIELD_VISIBILITY, post.getVisibility());
            
            if (post.getScheduledDate() != null) {
                postFormBuilder.setValue(FIELD_SCHEDULED_DATE, post.getScheduledDate().toString());
            }
        }
    }
    
    private void updatePostButtons() {
        boolean canEdit = selectedPost != null && postService.canUserEditPost(selectedPost, currentUserId);
        
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            postButtonPanel.setButtonEnabled("Update", canEdit);
            postButtonPanel.setButtonEnabled("Delete", canEdit);
        }
    }
    
    private void updateCommentButtons() {
        boolean hasSelection = selectedComment != null;
        
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            commentButtonPanel.setButtonEnabled("Approve", hasSelection);
            commentButtonPanel.setButtonEnabled("Reject", hasSelection);
            commentButtonPanel.setButtonEnabled("Delete", hasSelection);
        }
    }
    
    private void createPost() {
        if (!AuthUtil.checkPermissionWithMessage(authService,
                AuthorizationService.PERM_CREATE_POSTS, "create posts")) {
            return;
        }
        
        if (!postFormBuilder.validateRequired()) {
            return;
        }
        
        try {
            Post post = createPostFromForm();
            
            boolean success = postService.createPost(post);
            if (success) {
                DialogFactory.showSuccess(this, "Post created successfully!");
                loadPosts();
                clearPostForm();
            } else {
                DialogFactory.showError(this, "Failed to create post.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error creating post: " + e.getMessage());
        }
    }
    
    private void updatePost() {
        if (selectedPost == null) {
            DialogFactory.showError(this, "Please select a post to update.");
            return;
        }
        
        if (!postFormBuilder.validateRequired()) {
            return;
        }
        
        try {
            Post post = createPostFromForm();
            post.setId(selectedPost.getId());
            post.setAuthorId(selectedPost.getAuthorId());
            
            boolean success = postService.updatePost(post);
            if (success) {
                DialogFactory.showSuccess(this, "Post updated successfully!");
                loadPosts();
                clearPostForm();
            } else {
                DialogFactory.showError(this, "Failed to update post.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error updating post: " + e.getMessage());
        }
    }
    
    private void deletePost() {
        if (selectedPost == null) {
            DialogFactory.showError(this, "Please select a post to delete.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this post?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            boolean success = postService.deletePost(selectedPost.getId(), currentUserId);
            if (success) {
                DialogFactory.showSuccess(this, "Post deleted successfully!");
                loadPosts();
                clearPostForm();
            } else {
                DialogFactory.showError(this, "Failed to delete post.");
            }
        }
    }
    
    private void clearPostForm() {
        postFormBuilder.clearAll();
        selectedPost = null;
        selectedPhotoData = null;
        selectedPhotoFilename = null;
        postsTable.clearSelection();
        updatePostButtons();
    }
    
    private void addComment() {
        if (selectedPost == null) {
            DialogFactory.showError(this, "Please select a post to comment on.");
            return;
        }
        
        if (!commentFormBuilder.validateRequired()) {
            return;
        }
        
        try {
            String content = commentFormBuilder.getValue(FIELD_COMMENT).trim();
            
            Comment comment = new Comment(selectedPost.getId(), currentUserId, content);
            
            boolean success = postService.addComment(comment);
            if (success) {
                DialogFactory.showSuccess(this, "Comment added! It will be visible after teacher approval.");
                commentFormBuilder.clearAll();
                loadPosts(); // Refresh to update comment count
            } else {
                DialogFactory.showError(this, "Failed to add comment.");
            }
        } catch (Exception e) {
            DialogFactory.showError(this, "Error adding comment: " + e.getMessage());
        }
    }
    
    private void approveComment() {
        if (selectedComment == null) {
            DialogFactory.showError(this, "Please select a comment to approve.");
            return;
        }
        
        boolean success = postService.approveComment(selectedComment.getId());
        if (success) {
            DialogFactory.showSuccess(this, "Comment approved successfully!");
            loadComments();
        } else {
            DialogFactory.showError(this, "Failed to approve comment.");
        }
    }
    
    private void rejectComment() {
        if (selectedComment == null) {
            DialogFactory.showError(this, "Please select a comment to reject.");
            return;
        }
        
        boolean success = postService.rejectComment(selectedComment.getId());
        if (success) {
            DialogFactory.showSuccess(this, "Comment rejected successfully!");
            loadComments();
        } else {
            DialogFactory.showError(this, "Failed to reject comment.");
        }
    }
    
    private void deleteComment() {
        if (selectedComment == null) {
            DialogFactory.showError(this, "Please select a comment to delete.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this comment?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            boolean success = postService.deleteCommentByTeacher(selectedComment.getId(), currentUserId);
            if (success) {
                DialogFactory.showSuccess(this, "Comment deleted successfully!");
                loadComments();
            } else {
                DialogFactory.showError(this, "Failed to delete comment.");
            }
        }
    }
    
    private void viewPostComments() {
        if (selectedPost == null) {
            DialogFactory.showError(this, "Please select a post to view comments.");
            return;
        }
        
        // Switch to comments tab and show comments for this post
        if (mainTabbedPane.getTabCount() > 1) {
            mainTabbedPane.setSelectedIndex(1);
        }
        
        // In a full implementation, you'd filter comments by post
        loadComments();
    }
    
    private void searchPosts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPosts();
            return;
        }
        
        // Simple search implementation - in practice, you'd have a dedicated search method
        loadPosts();
    }
    
    private void selectPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image != null) {
                    // Resize image if too large
                    BufferedImage resizedImage = resizeImage(image, 800, 600);
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImage, "jpg", baos);
                    
                    selectedPhotoData = baos.toByteArray();
                    selectedPhotoFilename = selectedFile.getName();
                    
                    DialogFactory.showSuccess(this, "Photo selected: " + selectedPhotoFilename);
                } else {
                    DialogFactory.showError(this, "Invalid image file.");
                }
            } catch (IOException e) {
                DialogFactory.showError(this, "Error reading image file: " + e.getMessage());
            }
        }
    }
    
    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }
        
        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resized;
    }
    
    private Post createPostFromForm() throws Exception {
        String title = postFormBuilder.getValue(FIELD_TITLE).trim();
        String content = postFormBuilder.getValue(FIELD_CONTENT).trim();
        
        if (title.isEmpty() || content.isEmpty()) {
            throw new Exception("Title and content are required.");
        }
        
        Post post = new Post(title, content, currentUserId, null);
        
        if ("TEACHER".equals(currentUserRole) || "PRINCIPAL".equals(currentUserRole)) {
            String classSelection = postFormBuilder.getValue(FIELD_CLASS).trim();
            if (!classSelection.isEmpty() && !"All Classes".equals(classSelection)) {
                // Extract class ID from selection (you'd need to implement this based on your dropdown format)
                // For now, assume the class dropdown stores class IDs
                try {
                    int classId = extractClassIdFromSelection(classSelection);
                    if (classId > 0) {
                        post.setClassId(classId);
                    }
                } catch (Exception e) {
                    // Ignore class selection error, post will be for all classes
                }
            }
            
            String visibility = postFormBuilder.getValue(FIELD_VISIBILITY).trim();
            if (!visibility.isEmpty()) {
                post.setVisibility(visibility);
            }
            
            String scheduledDateStr = postFormBuilder.getValue(FIELD_SCHEDULED_DATE).trim();
            if (!scheduledDateStr.isEmpty()) {
                try {
                    LocalDate scheduledDate = LocalDate.parse(scheduledDateStr);
                    post.setScheduledDate(scheduledDate);
                } catch (DateTimeParseException e) {
                    throw new Exception("Invalid scheduled date format. Please use YYYY-MM-DD format.");
                }
            }
        }
        
        // Add photo if selected
        if (selectedPhotoData != null) {
            post.setPhotoAttachment(selectedPhotoData);
            post.setPhotoFilename(selectedPhotoFilename);
        }
        
        return post;
    }
    
    private void loadAvailableClasses() {
        try {
            java.util.List<model.Class> classes;
            
            if ("TEACHER".equals(currentUserRole)) {
                // Teachers see only their assigned classes
                classes = classDAO.findByTeacherId(currentUserId);
            } else {
                // Principals see all classes
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
            JComboBox<String> classCombo = (JComboBox<String>) postFormBuilder.getField(FIELD_CLASS).getInputComponent();
            classCombo.setModel(new DefaultComboBoxModel<>(classOptions));
            
        } catch (Exception e) {
            DialogFactory.showError(this, "Error loading classes: " + e.getMessage());
        }
    }
    
    private int extractClassIdFromSelection(String classSelection) {
        if (classSelection == null || classSelection.isEmpty() || "All Classes".equals(classSelection)) {
            return 0;
        }
        
        // Extract ID from format "ClassName (ID: 123)"
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
}
