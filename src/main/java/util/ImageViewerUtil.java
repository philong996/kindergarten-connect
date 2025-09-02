package util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Utility class for viewing attendance images
 */
public class ImageViewerUtil {
    
    /**
     * Shows an image in a dialog
     */
    public static void showImage(Component parent, byte[] imageData, String title) {
        if (imageData == null || imageData.length == 0) {
            JOptionPane.showMessageDialog(parent, "No image available", title, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                JOptionPane.showMessageDialog(parent, "Invalid image data", title, JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ImageDialog dialog = new ImageDialog((JFrame) SwingUtilities.getWindowAncestor(parent), title, image);
            dialog.setVisible(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Error loading image: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Creates a thumbnail icon from image bytes
     */
    public static ImageIcon createThumbnail(byte[] imageData, int width, int height) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) return null;
            
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
            
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Dialog for displaying images
     */
    private static class ImageDialog extends JDialog {
        public ImageDialog(JFrame parent, String title, BufferedImage image) {
            super(parent, title, true);
            initComponents(image);
            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        private void initComponents(BufferedImage image) {
            setLayout(new BorderLayout());
            
            // Scale image to fit reasonable dialog size
            int maxWidth = 600;
            int maxHeight = 400;
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            if (width > maxWidth || height > maxHeight) {
                double scaleX = (double) maxWidth / width;
                double scaleY = (double) maxHeight / height;
                double scale = Math.min(scaleX, scaleY);
                
                width = (int) (width * scale);
                height = (int) (height * scale);
            }
            
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(BorderFactory.createEtchedBorder());
            
            add(imageLabel, BorderLayout.CENTER);
            
            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }
}
