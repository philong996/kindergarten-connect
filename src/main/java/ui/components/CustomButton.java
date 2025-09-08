package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomButton extends JButton {

    private Color backgroundColor = AppColor.getColor("coralRed"); // màu nền
    private Color hoverColor = AppColor.getColor("bubblegumPink");      // màu khi hover
    private Color textColor = Color.WHITE;                   // màu chữ
    private int arc = 20; // bo tròn

    public CustomButton(String text) {
        super(text);
        setContentAreaFilled(false); // tắt fill mặc định
        setOpaque(false); // tắt nền mặc định
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(textColor);
        setBackground(backgroundColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
    
        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
    
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(backgroundColor);
            }
        });
    }

    public CustomButton(String text, Color backgroundColor, Color hoverColor, Color textColor) {
        super(text);
        setContentAreaFilled(false); // tắt fill mặc định
        setOpaque(false); // tắt nền mặc định
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(textColor);
        setBackground(backgroundColor);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
    
        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
    
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(backgroundColor);
            }
        });
    }

    public CustomButton(String text, accountType type) {
        super(text);
        Color bg, hover, fg = Color.WHITE; // Default text color
        switch (type) {
            case ADMIN:
                bg = AppColor.getColor("violet");
                hover = bg.brighter();
                fg = Color.BLACK;
                break;
            case TEACHER:
                bg = AppColor.getColor("violet");
                hover = AppColor.getColor("lightViolet");
                fg = Color.BLACK;
                break;
            case PARENT:
                bg = AppColor.getColor("greenBlue");
                hover = bg.brighter();
                break;
            default:
                bg = Color.WHITE;
                hover = bg.darker();
                fg = UIManager.getColor("Button.foreground");
                break;
        }
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(fg);
        setBackground(bg);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(bg);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(getBackground() != null ? getBackground() : backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        // Draw text
        super.paintComponent(g2);
        g2.dispose();
    }

    public enum accountType {
        DEFAULT, TEACHER, ADMIN, PARENT
    }
    
}