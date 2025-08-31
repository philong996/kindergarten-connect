package ui.components;

import java.awt.*;
import java.io.File;

public class CustomFont {
    private static Font  MonospacedFont;
    private static Font BalooFont;

    static {
        try {
            // Load the custom font from resources
            MonospacedFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/font/Monospaced.ttf")).deriveFont(14f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(MonospacedFont);
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            MonospacedFont = new Font("SansSerif", Font.PLAIN, 14); 
        }
    }

    static {
        try {
            // Load the custom font from resources
            BalooFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/font/Monospaced.ttf")).deriveFont(14f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(BalooFont);
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            BalooFont = new Font("SansSerif", Font.PLAIN, 14); 
        }
    }

    public static Font getMonospacedFont(float size) {
        return MonospacedFont.deriveFont(size);
    }

    public static Font getMonospacedFont(int style, float size) {
        return MonospacedFont.deriveFont(style, size);
    }

    public static Font getBalooFont(float size) {
        return MonospacedFont.deriveFont(size);
    }

    public static Font getBalooFont(int style, float size) {
        return MonospacedFont.deriveFont(style, size);
    }
}
