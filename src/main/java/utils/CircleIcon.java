package utils;

import javax.swing.*;
import java.awt.*;

/**
 * Icona vettoriale personalizzata per disegnare un pallino di stato colorato.
 */
public class CircleIcon implements Icon {
    private final Color color;

    public CircleIcon(Color color) { this.color = color; }

    @Override public int getIconWidth() { return 12; }
    @Override public int getIconHeight() { return 12; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y + 3, 12, 12);
        g2.dispose();
    }
}