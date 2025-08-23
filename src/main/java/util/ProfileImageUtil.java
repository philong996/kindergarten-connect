package util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Utility class for handling profile image operations with binary data
 */
public class ProfileImageUtil {
    
    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png", "gif"};
    private static final int MAX_IMAGE_SIZE = 300; // Maximum width/height in pixels
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes
    
    /**
     * Open file chooser dialog for selecting an image
     */
    public static File selectImageFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Image");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Set file filter for image files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files (*.jpg, *.jpeg, *.png, *.gif)", SUPPORTED_FORMATS);
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate file
            if (isValidImageFile(selectedFile)) {
                return selectedFile;
            } else {
                JOptionPane.showMessageDialog(parent, 
                    "Please select a valid image file (JPG, PNG, GIF) under 2MB.", 
                    "Invalid File", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    /**
     * Validate if the file is a valid image file
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            return false;
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        for (String format : SUPPORTED_FORMATS) {
            if (fileName.endsWith("." + format)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Convert image file to binary data
     */
    public static byte[] convertImageToByteArray(File imageFile) throws IOException {
        if (!isValidImageFile(imageFile)) {
            throw new IllegalArgumentException("Invalid image file");
        }
        
        // Read and resize image if needed
        BufferedImage originalImage = ImageIO.read(imageFile);
        BufferedImage processedImage = resizeImageIfNeeded(originalImage);
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = getImageFormat(imageFile.getName());
        ImageIO.write(processedImage, format, baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Resize image if it's larger than maximum size
     */
    private static BufferedImage resizeImageIfNeeded(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Check if resizing is needed
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return originalImage;
        }
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) width / height;
        int newWidth, newHeight;
        
        if (width > height) {
            newWidth = MAX_IMAGE_SIZE;
            newHeight = (int) (MAX_IMAGE_SIZE / aspectRatio);
        } else {
            newHeight = MAX_IMAGE_SIZE;
            newWidth = (int) (MAX_IMAGE_SIZE * aspectRatio);
        }
        
        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * Get image format from filename
     */
    private static String getImageFormat(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            // Convert jpeg to jpg for ImageIO
            return extension.equals("jpeg") ? "jpg" : extension;
        }
        return "jpg"; // Default format
    }
    
    /**
     * Load profile image from binary data as ImageIcon
     */
    public static ImageIcon loadProfileImageFromBytes(byte[] imageData, int width, int height) {
        if (imageData == null || imageData.length == 0) {
            return createDefaultProfileIcon(width, height);
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage img = ImageIO.read(bais);
            if (img != null) {
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (IOException e) {
            System.err.println("Error loading profile image from bytes: " + e.getMessage());
        }
        
        return createDefaultProfileIcon(width, height);
    }
    
    /**
     * Create a default profile icon when no image is available
     */
    public static ImageIcon createDefaultProfileIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set background with gradient
        GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 245, 245), 
                                                   0, height, new Color(230, 230, 230));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Draw border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(1, 1, width - 2, height - 2);
        
        // Draw person icon
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(3));
        
        // Head (circle)
        int headSize = Math.min(width, height) / 3;
        int headX = (width - headSize) / 2;
        int headY = height / 4;
        g2d.drawOval(headX, headY, headSize, headSize);
        
        // Body (arc for shoulders)
        int bodyWidth = (int)(width * 0.6);
        int bodyHeight = height / 3;
        int bodyX = (width - bodyWidth) / 2;
        int bodyY = headY + headSize + 10;
        g2d.drawArc(bodyX, bodyY, bodyWidth, bodyHeight, 0, 180);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    /**
     * Get image dimensions from binary data
     */
    public static Dimension getImageDimensions(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return new Dimension(0, 0);
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage img = ImageIO.read(bais);
            if (img != null) {
                return new Dimension(img.getWidth(), img.getHeight());
            }
        } catch (IOException e) {
            System.err.println("Error getting image dimensions: " + e.getMessage());
        }
        
        return new Dimension(0, 0);
    }
}
