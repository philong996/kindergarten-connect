package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomButton extends JButton {

    private Color backgroundColor = AppColor.getColor("pinkOrange"); // màu nền
    private Color hoverColor = AppColor.getColor("lightPink");      // màu khi hover
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
}