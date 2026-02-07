package utils;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private Color borderColor = null;
    private int borderThickness = 2;

    /**
     * Costruttore di default. Crea un pannello con raggio angoli pari a 20.
     */
    public RoundedPanel() {
        this(20);
    }

    /**
     * Costruttore che permette di specificare il raggio degli angoli.
     * @param radius raggio degli angoli arrotondati
     */
    public RoundedPanel(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
    }

    /**
     * Disegna il pannello con angoli arrotondati e bordo opzionale.
     * @param g contesto grafico
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sfondo
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcs.width, arcs.height);

        // Bordo (solo se impostato)
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            int offset = borderThickness / 2;
            g2.drawRoundRect(offset, offset, getWidth() - borderThickness, getHeight() - borderThickness, arcs.width, arcs.height);
        }

        g2.dispose();
    }

    /**
     * Imposta il colore del bordo del pannello.
     * @param borderColor colore del bordo
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    /**
     * Imposta lo spessore del bordo del pannello.
     * @param thickness spessore del bordo
     */
    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
        repaint();
    }
}