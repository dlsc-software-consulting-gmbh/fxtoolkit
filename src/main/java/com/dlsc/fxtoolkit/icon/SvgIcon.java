package com.dlsc.fxtoolkit.icon;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Icon that renders an SVG path data string.
 * It parses the path data and scales it to fit the icon's dimensions.
 * Supports M, L, H, V, C, S, Q, T, A, Z commands.
 */
public class SvgIcon implements Icon {

    private final String pathData;
    private final int width;
    private final int height;
    private Path2D.Float parsedPath;
    private boolean parseFailed = false;

    public SvgIcon(@NotNull String pathData, int width, int height) {
        this.pathData = pathData.trim().replaceAll("^\"|\"$", "");
        this.width = width;
        this.height = height;
        try {
            this.parsedPath = parsePath(this.pathData);
        } catch (Exception e) {
            this.parseFailed = true;
        }
    }

    public String getPathData() {
        return this.pathData;
    }

    public Rectangle2D getBounds() {
        return (parseFailed || parsedPath == null) ? null : parsedPath.getBounds2D();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (parseFailed || parsedPath == null) {
            g2d.setColor(JBColor.RED);
            g2d.drawString("!", x + (width / 2) - 2, y + (height / 2) + 4);
            return;
        }

        Rectangle2D bounds = parsedPath.getBounds2D();
        if (bounds.getWidth() == 0 || bounds.getHeight() == 0) {
            g2d.dispose();
            return;
        }

        AffineTransform transform = getAffineTransform(x, y, bounds);
        g2d.setColor(JBColor.foreground());
        g2d.fill(transform.createTransformedShape(parsedPath));
        g2d.dispose();
    }

    private @NotNull AffineTransform getAffineTransform(int x, int y, Rectangle2D bounds) {
        double margin = width > 16 ? 4.0 : 2.0;
        double availableWidth = width - margin;
        double availableHeight = height - margin;

        double scaleX = availableWidth / bounds.getWidth();
        double scaleY = availableHeight / bounds.getHeight();
        double scale = Math.min(scaleX, scaleY);

        double tx = x + (width - bounds.getWidth() * scale) / 2.0;
        double ty = y + (height - bounds.getHeight() * scale) / 2.0;

        AffineTransform transform = new AffineTransform();
        transform.translate(tx, ty);
        transform.scale(scale, scale);
        transform.translate(-bounds.getX(), -bounds.getY());
        return transform;
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    private Path2D.Float parsePath(String d) {
        Path2D.Float path = new Path2D.Float();
        Pattern pattern = Pattern.compile("([MmLlHhVvCcSsQqTtAaZz])|(-?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?)");
        Matcher matcher = pattern.matcher(d);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        Point2D.Float currentPoint = new Point2D.Float(0, 0);
        Point2D.Float ctrlPoint = new Point2D.Float(0, 0);
        char lastCommand = ' ';
        int i = 0;

        while (i < tokens.size()) {
            String token = tokens.get(i);
            char command;

            if (token.matches("[A-Za-z]")) {
                command = token.charAt(0);
                i++;
            } else {
                command = (lastCommand == 'M') ? 'L' : (lastCommand == 'm') ? 'l' : lastCommand;
            }

            boolean isRelative = Character.isLowerCase(command);

            try {
                switch (Character.toUpperCase(command)) {
                    case 'M':
                        currentPoint = getNextPoint(tokens, i, currentPoint, isRelative);
                        path.moveTo(currentPoint.x, currentPoint.y);
                        i += 2;
                        break;
                    case 'L':
                        currentPoint = getNextPoint(tokens, i, currentPoint, isRelative);
                        path.lineTo(currentPoint.x, currentPoint.y);
                        i += 2;
                        break;
                    case 'H':
                        currentPoint.x = isRelative ? currentPoint.x + Float.parseFloat(tokens.get(i)) : Float.parseFloat(tokens.get(i));
                        path.lineTo(currentPoint.x, currentPoint.y);
                        i++;
                        break;
                    case 'V':
                        currentPoint.y = isRelative ? currentPoint.y + Float.parseFloat(tokens.get(i)) : Float.parseFloat(tokens.get(i));
                        path.lineTo(currentPoint.x, currentPoint.y);
                        i++;
                        break;
                    case 'C':
                        Point2D.Float p1 = getNextPoint(tokens, i, currentPoint, isRelative);
                        Point2D.Float p2 = getNextPoint(tokens, i + 2, currentPoint, isRelative);
                        Point2D.Float end = getNextPoint(tokens, i + 4, currentPoint, isRelative);
                        path.curveTo(p1.x, p1.y, p2.x, p2.y, end.x, end.y);
                        currentPoint = end;
                        ctrlPoint = p2;
                        i += 6;
                        break;
                    case 'S':
                        Point2D.Float reflection = reflectControlPoint(currentPoint, ctrlPoint, lastCommand);
                        Point2D.Float p2s = getNextPoint(tokens, i, currentPoint, isRelative);
                        Point2D.Float ends = getNextPoint(tokens, i + 2, currentPoint, isRelative);
                        path.curveTo(reflection.x, reflection.y, p2s.x, p2s.y, ends.x, ends.y);
                        currentPoint = ends;
                        ctrlPoint = p2s;
                        i += 4;
                        break;
                    case 'Q':
                        Point2D.Float p1q = getNextPoint(tokens, i, currentPoint, isRelative);
                        Point2D.Float endq = getNextPoint(tokens, i + 2, currentPoint, isRelative);
                        path.quadTo(p1q.x, p1q.y, endq.x, endq.y);
                        currentPoint = endq;
                        ctrlPoint = p1q;
                        i += 4;
                        break;
                    case 'T':
                        Point2D.Float reflectionT = reflectControlPoint(currentPoint, ctrlPoint, lastCommand);
                        Point2D.Float endt = getNextPoint(tokens, i, currentPoint, isRelative);
                        path.quadTo(reflectionT.x, reflectionT.y, endt.x, endt.y);
                        currentPoint = endt;
                        ctrlPoint = reflectionT;
                        i += 2;
                        break;
                    case 'A':
                        float rx = Float.parseFloat(tokens.get(i));
                        float ry = Float.parseFloat(tokens.get(i + 1));
                        float xAxisRotation = Float.parseFloat(tokens.get(i + 2));
                        boolean largeArcFlag = Float.parseFloat(tokens.get(i + 3)) != 0;
                        boolean sweepFlag = Float.parseFloat(tokens.get(i + 4)) != 0;
                        Point2D.Float endA = getNextPoint(tokens, i + 5, currentPoint, isRelative);
                        arcTo(path, currentPoint.x, currentPoint.y, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, endA.x, endA.y);
                        currentPoint = endA;
                        i += 7;
                        break;
                    case 'Z':
                        path.closePath();
                        break;
                }
                lastCommand = command;
            } catch (Exception e) {
                // Parsing failed, return what we have
                return path;
            }
        }
        return path;
    }

    private Point2D.Float getNextPoint(List<String> tokens, int i, Point2D.Float current, boolean isRelative) {
        float x = Float.parseFloat(tokens.get(i));
        float y = Float.parseFloat(tokens.get(i + 1));
        if (isRelative) {
            return new Point2D.Float(current.x + x, current.y + y);
        }
        return new Point2D.Float(x, y);
    }

    private Point2D.Float reflectControlPoint(Point2D.Float current, java.awt.geom.Point2D.Float ctrl, char lastCmd) {
        if ("CSQT".indexOf(Character.toUpperCase(lastCmd)) == -1) {
            return current;
        }
        return new Point2D.Float(2 * current.x - ctrl.x, 2 * current.y - ctrl.y);
    }

    private static void arcTo(Path2D path, double x0, double y0, double rx, double ry, double angle, boolean largeArcFlag, boolean sweepFlag, double x, double y) {
        if (rx == 0 || ry == 0) {
            path.lineTo(x, y);
            return;
        }

        double phi = Math.toRadians(angle);
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        double x1p = cosPhi * (x0 - x) / 2.0 + sinPhi * (y0 - y) / 2.0;
        double y1p = -sinPhi * (x0 - x) / 2.0 + cosPhi * (y0 - y) / 2.0;

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        double rx_sq = rx * rx;
        double ry_sq = ry * ry;
        double x1p_sq = x1p * x1p;
        double y1p_sq = y1p * y1p;

        double lambda = x1p_sq / rx_sq + y1p_sq / ry_sq;
        if (lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
            rx_sq = rx * rx;
            ry_sq = ry * ry;
        }

        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double num = rx_sq * ry_sq - rx_sq * y1p_sq - ry_sq * x1p_sq;
        double den = rx_sq * y1p_sq + ry_sq * x1p_sq;
        double c_radicand = Math.max(0, num / den);
        double c = sign * Math.sqrt(c_radicand);

        double cxp = c * (rx * y1p / ry);
        double cyp = c * -(ry * x1p / rx);

        double cx = cosPhi * cxp - sinPhi * cyp + (x0 + x) / 2.0;
        double cy = sinPhi * cxp + cosPhi * cyp + (y0 + y) / 2.0;

        double ux = (x1p - cxp) / rx;
        double uy = (y1p - cyp) / ry;
        double vx = (-x1p - cxp) / rx;
        double vy = (-y1p - cyp) / ry;

        double startAngle = Math.toDegrees(Math.atan2(uy, ux));
        double angleDiff = Math.toDegrees(angleBetween(ux, uy, vx, vy));

        if (!sweepFlag && angleDiff > 0) {
            angleDiff -= 360;
        } else if (sweepFlag && angleDiff < 0) {
            angleDiff += 360;
        }

        int segments = (int) Math.ceil(Math.abs(angleDiff) / 90.0);
        double angleIncrement = Math.toRadians(angleDiff / segments);
        double currentAngle = Math.toRadians(startAngle);

        for (int i = 0; i < segments; i++) {
            double nextAngle = currentAngle + angleIncrement;
            double t = (4.0 / 3.0) * Math.tan(angleIncrement / 4.0);

            double x1 = Math.cos(currentAngle) - t * Math.sin(currentAngle);
            double y1 = Math.sin(currentAngle) + t * Math.cos(currentAngle);
            double x2 = Math.cos(nextAngle) + t * Math.sin(nextAngle);
            double y2 = Math.sin(nextAngle) - t * Math.cos(nextAngle);

            Point2D.Double p1 = new Point2D.Double(cx + rx * (cosPhi * x1 - sinPhi * y1), cy + ry * (sinPhi * x1 + cosPhi * y1));
            Point2D.Double p2 = new Point2D.Double(cx + rx * (cosPhi * x2 - sinPhi * y2), cy + ry * (sinPhi * x2 + cosPhi * y2));
            Point2D.Double pEnd = new Point2D.Double(cx + rx * Math.cos(nextAngle) * cosPhi - ry * Math.sin(nextAngle) * sinPhi,
                    cy + rx * Math.cos(nextAngle) * sinPhi + ry * Math.sin(nextAngle) * cosPhi);

            path.curveTo(p1.x, p1.y, p2.x, p2.y, pEnd.x, pEnd.y);
            currentAngle = nextAngle;
        }
    }

    private static double angleBetween(double ux, double uy, double vx, double vy) {
        double dot = ux * vx + uy * vy;
        double lenSq = (ux * ux + uy * uy) * (vx * vx + vy * vy);
        double angle = Math.acos(Math.max(-1, Math.min(1, dot / Math.sqrt(lenSq))));
        if (ux * vy - uy * vx < 0) {
            return -angle;
        }
        return angle;
    }
}
