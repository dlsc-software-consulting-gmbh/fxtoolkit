package com.dlsc.fxtoolkit.icon;

import com.intellij.ui.JBColor;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * An Icon that renders a simple diamond shape icon with a specified color.
 */
public class ColorIcon implements Icon {

    private final int size;
    private final Color color;

    public ColorIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Draw a diamond shape icon ◇ with the specified color.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Horizontal
            int[] xPoints = {x + size / 2, x + size, x + size / 2, x};
            // Vertical
            int[] yPoints = {y, y + size / 2, y + size, y + size / 2};

            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 4);
            g2d.setColor(JBColor.border());
            g2d.drawPolygon(xPoints, yPoints, 4);
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
