package ui.components;

import java.awt.*;
import javax.swing.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setLayout(new GridBagLayout());
    }

    // @Override
    // protected void paintComponent(Graphics g) {
    //     super.paintComponent(g);
    //     g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    // }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = backgroundImage.getWidth(this);
        int imgHeight = backgroundImage.getHeight(this);

        if (imgWidth > 0 && imgHeight > 0) {
            // Tính tỉ lệ scale giữ nguyên aspect ratio
            double scaleX = (double) panelWidth / imgWidth;
            double scaleY = (double) panelHeight / imgHeight;
            double scale = Math.min(scaleX, scaleY);

            int newWidth = (int) (imgWidth * scale);
            int newHeight = (int) (imgHeight * scale);

            // Canh giữa theo chiều ngang (x), canh dưới theo chiều dọc (y)
            int x = (panelWidth - newWidth) / 2;
            int y = panelHeight - newHeight;

            g.drawImage(backgroundImage, x, y, newWidth, newHeight, this);
        }
    }
}
