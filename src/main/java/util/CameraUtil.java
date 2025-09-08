package util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import ui.components.AppColor;
import ui.components.CustomButton;
import ui.components.CustomButton.accountType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for camera operations and image capture
 * Provides multiple portable methods that work across all platforms
 */
public class CameraUtil {
    
    /**
     * Checks if camera functionality is available (always true for file-based approach)
     */
    public static boolean isCameraAvailable() {
        return true; // File selection always works
    }
    
    /**
     * Captures an image using multiple methods - works on any laptop/OS
     * Returns the image as byte array or null if cancelled
     */
    public static byte[] captureImage(Component parent, String title) {
        CameraDialog dialog = new CameraDialog((JFrame) SwingUtilities.getWindowAncestor(parent), title);
        dialog.setVisible(true);
        return dialog.getCapturedImageBytes();
    }
    
    /**
     * Converts BufferedImage to byte array
     */
    public static byte[] imageToBytes(BufferedImage image) {
        if (image == null) return null;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Creates a mock image for demonstration purposes
     */
    public static byte[] createMockImage(String text, Color backgroundColor) {
        BufferedImage image = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, 200, 150);
        
        // Set text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (200 - fm.stringWidth(text)) / 2;
        int y = (150 + fm.getAscent()) / 2;
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return imageToBytes(image);
    }
    
    /**
     * Universal camera dialog that works on any laptop/OS
     * Offers multiple methods: file selection, camera app, and mock images
     */
    private static class CameraDialog extends JDialog {
        private byte[] capturedImageBytes;
        private boolean confirmed = false;
        
        public CameraDialog(JFrame parent, String title) {
            super(parent, title, true);
            initComponents();
            setSize(500, 400);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            setBackground(AppColor.getColor("lightViolet"));
            
            // Header
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(AppColor.getColor("darkViolet"));

            ImageIcon photoIcon = new ImageIcon("src/main/resources/images/camera.png");
            Image imgPhoto = photoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            ImageIcon scaledPhotoIcon = new ImageIcon(imgPhoto);
            JLabel headerLabel = new JLabel("Choose Photo Capture Method", scaledPhotoIcon, JLabel.CENTER);
            headerLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            headerPanel.add(headerLabel);
            add(headerPanel, BorderLayout.NORTH);
            
            // Main options panel
            JPanel optionsPanel = new JPanel(new GridBagLayout());
            optionsPanel.setBackground(AppColor.getColor("lightViolet"));   
            optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Option 1: Select existing photo file
            // Tạo icon và resize
            ImageIcon folderIcon = new ImageIcon("src/main/resources/images/folder.png");
            Image scaled = folderIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JButton selectFileButton = new JButton("<html>"
                + "<b>Select Photo File</b><br/>"
                + "<span style='font-size:8px;color:gray;'>Choose an existing photo from your computer</span>"
                + "</html>");
            selectFileButton.setIcon(scaledIcon);
            selectFileButton.setHorizontalAlignment(SwingConstants.LEFT);    
            selectFileButton.setHorizontalTextPosition(SwingConstants.RIGHT);  
            selectFileButton.setVerticalTextPosition(SwingConstants.CENTER);
            selectFileButton.setIconTextGap(8); 
            selectFileButton.setPreferredSize(new Dimension(300, 60)); 
            selectFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            selectFileButton.addActionListener(e -> selectPhotoFile());

            gbc.gridx = 0; gbc.gridy = 0;
            optionsPanel.add(selectFileButton, gbc);
            
            // Option 2: Use device camera
            ImageIcon cameraIcon = new ImageIcon("src/main/resources/images/takePhoto.png");
            Image imgCameraIcon = cameraIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            ImageIcon scaledCameraIcon = new ImageIcon(imgCameraIcon);
            JButton cameraButton = new JButton("<html>"
                + "<b>Take New Photo</b><br/>"
                + "<span style='font-size:8px;color:gray;'>Open camera app to take a new photo</span>"
                + "</html>");
            cameraButton.setIcon(scaledCameraIcon);
            cameraButton.setHorizontalAlignment(SwingConstants.LEFT);       
            cameraButton.setHorizontalTextPosition(SwingConstants.RIGHT);   
            cameraButton.setVerticalTextPosition(SwingConstants.CENTER);
            cameraButton.setIconTextGap(8); 
            cameraButton.setPreferredSize(new Dimension(300, 60)); 
            cameraButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cameraButton.addActionListener(e -> takeNewPhoto());
        
            gbc.gridy = 1;
            optionsPanel.add(cameraButton, gbc);
            
            // Option 3: Use mock image for testing
            
            JButton mockButton = createOptionButton(
                "🎭 Use Test Image", 
                "Generate a sample image for testing",
                new Color(156, 39, 176)
            );
            mockButton.addActionListener(e -> useMockImage());
            
            gbc.gridy = 2;
            // optionsPanel.add(mockButton, gbc);
            
            add(optionsPanel, BorderLayout.CENTER);
            
            // Bottom panel with cancel button
            JPanel bottomPanel = new JPanel(new FlowLayout());
            bottomPanel.setBackground(AppColor.getColor("lightViolet"));
            CustomButton cancelButton = new CustomButton("Cancel", accountType.TEACHER);
            cancelButton.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            bottomPanel.add(cancelButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private JButton createOptionButton(String title, String description, Color bgColor) {
            JButton button = new JButton();
            button.setLayout(new BorderLayout());
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            button.setPreferredSize(new Dimension(400, 80));
            
            JLabel titleLabel = new JLabel(title, JLabel.CENTER);
            // titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setFont(getFont().deriveFont(Font.BOLD, 14f));
            titleLabel.setForeground(Color.WHITE);
            
            JLabel descLabel = new JLabel(description, JLabel.CENTER);
            // descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            descLabel.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            descLabel.setForeground(Color.WHITE);
            
            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);
            textPanel.add(titleLabel, BorderLayout.CENTER);
            textPanel.add(descLabel, BorderLayout.SOUTH);
            
            button.add(textPanel, BorderLayout.CENTER);
            return button;
        }
        
        private void selectPhotoFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Photo File");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = fileChooser.getSelectedFile();
                    capturedImageBytes = Files.readAllBytes(selectedFile.toPath());
                    confirmed = true;
                    dispose();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error reading file: " + e.getMessage(), 
                        "File Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void takeNewPhoto() {
            try {
                // Open default camera app based on OS
                String os = System.getProperty("os.name").toLowerCase();
                
                if (os.contains("mac")) {
                    // macOS: Open Photo Booth or QuickTime
                    try {
                        new ProcessBuilder("open", "-a", "Photo Booth").start();
                    } catch (Exception e) {
                        // Fallback to QuickTime Player
                        new ProcessBuilder("open", "-a", "QuickTime Player").start();
                    }
                } else if (os.contains("win")) {
                    // Windows: Open Camera app
                    new ProcessBuilder("cmd", "/c", "start", "microsoft.windows.camera:").start();
                } else {
                    // Linux: Try to open camera app
                    try {
                        new ProcessBuilder("cheese").start(); // Common Linux camera app
                    } catch (Exception e) {
                        new ProcessBuilder("guvcview").start(); // Alternative
                    }
                }
                
                // Show instructions
                int choice = JOptionPane.showConfirmDialog(this,
                    "Camera app opened!\n\n" +
                    "Instructions:\n" +
                    "1. Take a photo with the camera app\n" +
                    "2. Save it to your Desktop or Downloads folder\n" +
                    "3. Click 'OK' to select the saved photo\n" +
                    "4. Or click 'Cancel' to go back\n\n" +
                    "Ready to select your photo?",
                    "Camera Instructions",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (choice == JOptionPane.OK_OPTION) {
                    selectPhotoFile(); // Let user select the photo they just took
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Could not open camera app: " + e.getMessage() + 
                    "\nPlease use 'Select Photo File' option instead.",
                    "Camera Error",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        private void useMockImage() {
            String[] options = {"Student Photo", "Profile Picture", "ID Photo", "Custom"};
            String choice = (String) JOptionPane.showInputDialog(this,
                "Choose mock image type:",
                "Mock Image Options",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choice != null) {
                Color bgColor;
                switch (choice) {
                    case "Student Photo": bgColor = new Color(135, 206, 235); break;
                    case "Profile Picture": bgColor = new Color(144, 238, 144); break;
                    case "ID Photo": bgColor = new Color(255, 218, 185); break;
                    default: bgColor = Color.LIGHT_GRAY;
                }
                
                capturedImageBytes = createMockImage(choice, bgColor);
                confirmed = true;
                dispose();
            }
        }
        
        public byte[] getCapturedImageBytes() {
            return confirmed ? capturedImageBytes : null;
        }
    }

    

}
