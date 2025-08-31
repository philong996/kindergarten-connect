package ui.components;

import javax.swing.*;
import java.awt.*;

public class CustomMessageDialog extends JDialog {

    public enum Type { INFO, SUCCESS, ERROR }

    private Color bgColor = AppColor.getColor("yellowOrange");
    private Color textColor = Color.BLACK;
    private Font font = new Font("Comic Sans MS", Font.PLAIN, 14);
    private int arc = 20;

    public CustomMessageDialog(JFrame parent, String title, String message, Type type) {
        super(parent, true);
        setUndecorated(true);
        setSize(350, 150);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setOpaque(false);

        // Icon tùy loại
        JLabel iconLabel = new JLabel();
        String iconPath = switch(type) {
            case SUCCESS -> "src/main/resources/images/star.png";
            case ERROR -> "src/main/resources/images/warning.png";
            case INFO -> "src/main/resources/images/info.png";
        };
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        iconLabel.setIcon(new ImageIcon(img));

        //Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(font);
        titleLabel.setForeground(textColor);

        // Text
        JLabel textLabel = new JLabel("<html><center>" + message + "</center></html>");
        textLabel.setForeground(textColor);
        textLabel.setFont(font);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Nút OK
        JButton okButton = new JButton("OK");
        okButton.setFont(font);
        okButton.setForeground(Color.BLACK);
        okButton.setFocusPainted(true);
        okButton.addActionListener(e -> dispose());

        // Panel chứa icon + text
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(iconLabel, BorderLayout.WEST);
        centerPanel.add(textLabel, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);
        content.add(okButton, BorderLayout.SOUTH);

        add(content);
    }

    public static void showMessage(JFrame parent, String title, String message, Type type) {
        CustomMessageDialog dialog = new CustomMessageDialog(parent, title, message, type);
        dialog.setVisible(true);
    }
}
