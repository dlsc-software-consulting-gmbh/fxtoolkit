package com.dlsc.fxtoolkit.icon;

import com.dlsc.fxtoolkit.model.GradientInfo;
import com.dlsc.fxtoolkit.model.GradientType;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * An Icon that renders a preview of a CSS gradient (linear or radial).
 * The preview is drawn as a circle.
 */
public class GradientIcon implements Icon {

    private final int size;
    private final GradientInfo gradientInfo;

    public GradientIcon(int size, GradientInfo gradientInfo) {
        this.size = size;
        this.gradientInfo = gradientInfo;
    }

    public GradientInfo getGradientInfo() {
        return gradientInfo;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (gradientInfo == null || gradientInfo.colors.length == 0) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Ellipse2D.Double circle = new Ellipse2D.Double(x, y, size, size);
            Paint paint = getPaint(x, y);

            g2d.setPaint(paint);
            g2d.fill(circle);
            g2d.setColor(JBColor.GRAY);
            // g2d.drawOval(x, y, size - 1, size - 1);
        } finally {
            g2d.dispose();
        }

    }

    private @NotNull Paint getPaint(int x, int y) {
        Paint paint;
        if (gradientInfo.type == GradientType.LINEAR) {
            Point2D.Float start = new Point2D.Float(x + size * gradientInfo.startX, y + size * gradientInfo.startY);
            Point2D.Float end = new Point2D.Float(x + size * gradientInfo.endX, y + size * gradientInfo.endY);
            paint = new LinearGradientPaint(start, end, gradientInfo.fractions, gradientInfo.colors);
        } else { // RADIAL
            Point2D.Float center = new Point2D.Float(x + size * gradientInfo.centerX, y + size * gradientInfo.centerY);
            float radius = (float) (size / 2.0 * gradientInfo.radius);
            paint = new RadialGradientPaint(center, radius, gradientInfo.fractions, gradientInfo.colors);
        }
        return paint;
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
