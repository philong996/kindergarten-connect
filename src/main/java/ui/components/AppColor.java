package ui.components;

import java.awt.Color;
import java.io.FileInputStream;
import java.util.Properties;

public class AppColor {
    private static Properties colors = new Properties();

    static {
        try {
            colors.load(new FileInputStream("src/main/resources/colors.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Color getColor(String name) {
        String hex = colors.getProperty(name);
        if (hex != null) {
            return hexToColor(hex);
        }
        return Color.BLACK; // Default color if not found
    }

    private static Color hexToColor(String hex) {
        return new Color(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16));
    }

}
