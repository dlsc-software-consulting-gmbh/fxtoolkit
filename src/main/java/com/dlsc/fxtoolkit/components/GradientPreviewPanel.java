package com.dlsc.fxtoolkit.components;

import com.dlsc.fxtoolkit.model.GradientInfo;
import com.dlsc.fxtoolkit.model.GradientType;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * A panel that displays a larger preview of a gradient.
 * The preview is rendered in a rectangular area.
 */
public class GradientPreviewPanel extends JPanel {

    private final GradientInfo gradientInfo;
    private final int previewWidth;
    private final int previewHeight;

    public GradientPreviewPanel(GradientInfo info, int width, int height) {
        this.gradientInfo = info;
        this.previewWidth = width;
        this.previewHeight = height;
        // Set a preferred size for the panel, including borders
        setPreferredSize(new Dimension(width + 20, height + 20));
        setBorder(JBUI.Borders.customLine(JBColor.border(), 1));
        setBackground(JBColor.PanelBackground);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Fill the background with the panel
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        if (gradientInfo == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Center the preview rectangle within the panel
            int rectX = (getWidth() - previewWidth) / 2;
            int rectY = (getHeight() - previewHeight) / 2;
            Rectangle previewRect = new Rectangle(rectX, rectY, previewWidth, previewHeight);
            // Apply the gradient paint and fill the rectangle
            g2d.setPaint(getPaint(previewRect));
            g2d.fill(previewRect);

            // Draw a border around the preview rectangle
            g2d.setColor(JBColor.border());
            g2d.draw(previewRect);

        } finally {
            g2d.dispose();
        }
    }

    private @NotNull Paint getPaint(Rectangle previewRect) {
        Paint paint;
        if (gradientInfo.type == GradientType.LINEAR) {
            // For Linear Gradients, calculate start and end points relative to the preview rectangle
            Point start = new Point((int) (previewRect.x + previewRect.width * gradientInfo.startX),
                                    (int) (previewRect.y + previewRect.height * gradientInfo.startY));
            Point end = new Point((int) (previewRect.x + previewRect.width * gradientInfo.endX),
                                  (int) (previewRect.y + previewRect.height * gradientInfo.endY));
            paint = new LinearGradientPaint(start, end, gradientInfo.fractions, gradientInfo.colors);
        } else { // RADIAL
            // For Radial Gradients, the center is relative to the preview rectangle
            Point center = new Point((int) (previewRect.x + previewRect.width * gradientInfo.centerX),
                                     (int) (previewRect.y + previewRect.height * gradientInfo.centerY));
            // The radius is based on the average of the width and height of the preview area
            float radius = (float) ((previewRect.width + previewRect.height) / 2.0 * gradientInfo.radius / 2.0);
            paint = new RadialGradientPaint(center, radius, gradientInfo.fractions, gradientInfo.colors);
        }
        return paint;
    }
}
