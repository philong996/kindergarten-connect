package ui.panels;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GrowthComparisonChart extends JPanel {
    private List<Integer> ages;
    private List<Double> heights, weights, bmis;
    private List<Double> trackerHeights, trackerWeights, trackerBMIs;
    private boolean isMale;

    public GrowthComparisonChart(
            List<Integer> ages,
            List<Double> heights,
            List<Double> weights,
            List<Double> bmis,
            List<Double> trackerHeights,
            List<Double> trackerWeights,
            List<Double> trackerBMIs,
            boolean isMale
    ) {
        this.ages = ages;
        this.heights = heights;
        this.weights = weights;
        this.bmis = bmis;
        this.trackerHeights = trackerHeights;
        this.trackerWeights = trackerWeights;
        this.trackerBMIs = trackerBMIs;
        this.isMale = isMale;
        setPreferredSize(new Dimension(800, 400));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (ages == null || ages.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 50;
        int w = getWidth() - 2 * padding;
        int h = getHeight() - 2 * padding;

        // Draw axes
        g2.setColor(Color.BLACK);
        g2.drawLine(padding, h + padding, padding + w, h + padding); // x-axis
        g2.drawLine(padding, padding, padding, h + padding); // y-axis

        // Scale function
        double minHeight = 80, maxHeight = 120;
        double minWeight = 10, maxWeight = 25;
        double minBMI = 14, maxBMI = 20;

        // Draw tracker and actual values
        for (int i = 0; i < ages.size() - 1; i++) {
            int x1 = padding + (i * w) / (ages.size() - 1);
            int x2 = padding + ((i + 1) * w) / (ages.size() - 1);

            // Tracker height (blue)
            g2.setColor(Color.BLUE);
            int y1h = padding + (int) (h - (trackerHeights.get(i) - minHeight) / (maxHeight - minHeight) * h);
            int y2h = padding + (int) (h - (trackerHeights.get(i + 1) - minHeight) / (maxHeight - minHeight) * h);
            g2.drawLine(x1, y1h, x2, y2h);

            // Tracker weight (green)
            g2.setColor(Color.GREEN.darker());
            int y1w = padding + (int) (h - (trackerWeights.get(i) - minWeight) / (maxWeight - minWeight) * h);
            int y2w = padding + (int) (h - (trackerWeights.get(i + 1) - minWeight) / (maxWeight - minWeight) * h);
            g2.drawLine(x1, y1w, x2, y2w);

            // Tracker BMI (magenta)
            g2.setColor(Color.MAGENTA);
            int y1b = padding + (int) (h - (trackerBMIs.get(i) - minBMI) / (maxBMI - minBMI) * h);
            int y2b = padding + (int) (h - (trackerBMIs.get(i + 1) - minBMI) / (maxBMI - minBMI) * h);
            g2.drawLine(x1, y1b, x2, y2b);
        }

        // Draw actual points
        for (int i = 0; i < ages.size(); i++) {
            int x = padding + (i * w) / (ages.size() - 1);

            // Height
            g2.setColor(Color.RED);
            int yH = padding + (int) (h - (heights.get(i) - minHeight) / (maxHeight - minHeight) * h);
            g2.fillOval(x - 4, yH - 4, 8, 8);

            // Weight
            g2.setColor(Color.ORANGE);
            int yW = padding + (int) (h - (weights.get(i) - minWeight) / (maxWeight - minWeight) * h);
            g2.fillOval(x - 4, yW - 4, 8, 8);

            // BMI
            g2.setColor(Color.PINK.darker());
            int yB = padding + (int) (h - (bmis.get(i) - minBMI) / (maxBMI - minBMI) * h);
            g2.fillOval(x - 4, yB - 4, 8, 8);
        }

        // Legend
        int legendX = getWidth() - 150;
        int legendY = 20;
        g2.setColor(Color.RED);
        g2.fillRect(legendX, legendY, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("Height", legendX + 15, legendY + 10);

        g2.setColor(Color.ORANGE);
        g2.fillRect(legendX, legendY + 15, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("Weight", legendX + 15, legendY + 25);

        g2.setColor(Color.PINK.darker());
        g2.fillRect(legendX, legendY + 30, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("BMI", legendX + 15, legendY + 40);

        g2.setColor(Color.BLUE);
        g2.fillRect(legendX, legendY + 45, 10, 2);
        g2.setColor(Color.BLACK);
        g2.drawString("Tracker Height", legendX + 15, legendY + 50);

        g2.setColor(Color.GREEN.darker());
        g2.fillRect(legendX, legendY + 55, 10, 2);
        g2.setColor(Color.BLACK);
        g2.drawString("Tracker Weight", legendX + 15, legendY + 60);

        g2.setColor(Color.MAGENTA);
        g2.fillRect(legendX, legendY + 65, 10, 2);
        g2.setColor(Color.BLACK);
        g2.drawString("Tracker BMI", legendX + 15, legendY + 70);
    }
}
