package ui.components;

import java.awt.*;
import java.util.Enumeration;
import javax.swing.*;

public class AppStyle {
    // Đổi font toàn app
    public static void setUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }

    // Đổi màu chữ toàn app
    public static void setUIForeground(Color color) {
        // Các key thường dùng cho text
        UIManager.put("Label.foreground", color);
        UIManager.put("Button.foreground", color);
        UIManager.put("CheckBox.foreground", color);
        UIManager.put("RadioButton.foreground", color);
        UIManager.put("TextField.foreground", color);
        UIManager.put("PasswordField.foreground", color);
        UIManager.put("TextArea.foreground", color);
        UIManager.put("ComboBox.foreground", color);
        UIManager.put("Menu.foreground", color);
        UIManager.put("MenuItem.foreground", color);
        UIManager.put("TabbedPane.foreground", color);
        UIManager.put("Table.foreground", color);
        UIManager.put("List.foreground", color);
    }

}
