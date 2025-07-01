package com.dlsc.fxtoolkit.util;

import com.dlsc.fxtoolkit.FxCssService;
import com.dlsc.fxtoolkit.model.GradientInfo;
import com.dlsc.fxtoolkit.model.GradientType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to parse JavaFX CSS gradient strings and convert them into renderable objects.
 * This class handles both linear and radial gradients.
 */
public class GradientConverter {

    private static final Pattern COLOR_STOP_PATTERN = Pattern.compile("(-?[\\w#-]+(?:\\([^)]*\\))?)\\s*(\\d*\\.?\\d*%?)?");

    @Nullable
    public static GradientInfo parse(@NotNull String gradientString, @NotNull Project project) {
        gradientString = gradientString.trim();
        final FxCssService service = FxCssService.getInstance(project);
        Function<String, Color> colorResolver = (colorStr) -> resolveColor(colorStr, service);

        if (gradientString.startsWith("linear-gradient")) {
            return parseLinearGradient(gradientString, colorResolver);
        } else if (gradientString.startsWith("radial-gradient")) {
            return parseRadialGradient(gradientString, colorResolver);
        }
        return null;
    }

    private static Color resolveColor(String colorStr, FxCssService service) {
        colorStr = colorStr.trim();
        if (colorStr.startsWith("-")) {
            String resolved = service.resolveConstantValue(colorStr.substring(1));
            return ColorConverter.parseOrDefault(resolved != null ? resolved : "#000000", JBColor.BLACK);
        }
        return ColorConverter.parseOrDefault(colorStr, JBColor.BLACK);
    }

    private static GradientInfo parseLinearGradient(String value, Function<String, Color> colorResolver) {
        try {
            String content = value.substring(value.indexOf('(') + 1, value.lastIndexOf(')'));

            float startX = 0f;
            float startY = 0f;
            float endX = 0f;
            float endY = 1f;
            if (content.contains("to right")) {
                startX = 0f;
                startY = 0.5f;
                endX = 1f;
                endY = 0.5f;
            } else if (content.contains("to left")) {
                startX = 1f;
                startY = 0.5f;
                endX = 0f;
                endY = 0.5f;
            } else if (content.contains("to top")) {
                startX = 0.5f;
                startY = 1f;
                endX = 0.5f;
                endY = 0f;
            } else if (content.contains("to bottom right")) {
                startX = 0f;
                startY = 0f;
            } else if (content.contains("to bottom left")) {
                startX = 1f;
                startY = 0f;
            } else if (content.contains("to top right")) {
                startX = 0f;
                startY = 1f;
                endX = 1f;
                endY = 0f;
            } else if (content.contains("to top left")) {
                startX = 1f;
                startY = 1f;
                endX = 0f;
                endY = 0f;
            }

            String stopsString = content.replaceAll("(?i)(to\\s+(?:left|right|top|bottom)(?:\\s+(?:left|right|top|bottom))?|from\\s+.*?\\s+to\\s+.*?|repeat|reflect)\\s*,?", "").trim();

            List<ColorStop> colorStops = parseColorStops(stopsString, colorResolver);
            if (colorStops.isEmpty()) {
                return null;
            }

            GradientInfo info = new GradientInfo(getColorsFromStops(colorStops), getFractionsFromStops(colorStops), GradientType.LINEAR);

            info.startX = startX;
            info.startY = startY;
            info.endX = endX;
            info.endY = endY;
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    private static GradientInfo parseRadialGradient(String value, Function<String, Color> colorResolver) {
        try {
            String content = value.substring(value.indexOf('(') + 1, value.lastIndexOf(')'));

            float radius = 1.0f;
            Pattern radiusPattern = Pattern.compile("radius\\s+([0-9.]+)%?");
            Matcher radiusMatcher = radiusPattern.matcher(content);
            if (radiusMatcher.find()) {
                radius = Float.parseFloat(radiusMatcher.group(1)) / 100f;
            }

            String stopsString = getStopsString(content);

            List<ColorStop> colorStops = parseColorStops(stopsString, colorResolver);
            if (colorStops.isEmpty()) return null;

            GradientInfo info = new GradientInfo(
                    getColorsFromStops(colorStops),
                    getFractionsFromStops(colorStops),
                    GradientType.RADIAL
            );
            info.radius = radius;
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    private static @NotNull String getStopsString(String content) {
        String stopsString = content
                .replaceAll("(?i)focus-angle\\s+[\\w.-]+\\s*,?", "")
                .replaceAll("(?i)focus-distance\\s+[\\d.]+%?\\s*,?", "")
                .replaceAll("(?i)center\\s+[\\d.]+%?\\s+[\\d.]+%?\\s*,?", "")
                .replaceAll("(?i)radius\\s+[\\w.]+%?\\s*,?", "")
                .replaceAll("(?i)(repeat|reflect)\\s*,?", "")
                .trim();

        if (stopsString.startsWith(",")) {
            stopsString = stopsString.substring(1).trim();
        }
        return stopsString;
    }

    private static List<ColorStop> parseColorStops(String stopsString, Function<String, Color> colorResolver) {
        List<ColorStop> colorStops = new ArrayList<>();
        // Split by comma but not inside parentheses
        String[] stops = stopsString.split(",(?![^()]*\\))");

        for (String stop : stops) {
            Matcher stopMatcher = COLOR_STOP_PATTERN.matcher(stop.trim());
            if (stopMatcher.matches()) {
                String colorPart = stopMatcher.group(1);
                String fractionPart = stopMatcher.group(2);

                Float fraction = null;
                if (fractionPart != null && !fractionPart.isEmpty()) {
                    fraction = Float.parseFloat(fractionPart.replace("%", "").trim()) / 100f;
                }
                colorStops.add(new ColorStop(colorResolver.apply(colorPart.trim()), fraction));
            }
        }
        return colorStops;
    }

    private static Color[] getColorsFromStops(List<ColorStop> stops) {
        return stops.stream().map(s -> s.color).toArray(Color[]::new);
    }

    private static float[] getFractionsFromStops(List<ColorStop> stops) {
        distributeFractions(stops);
        Collections.sort(stops);

        float[] fractions = new float[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            fractions[i] = stops.get(i).fraction;
        }

        for (int i = 1; i < fractions.length; i++) {
            if (fractions[i] <= fractions[i - 1]) {
                fractions[i] = Math.nextUp(fractions[i - 1]);
            }
        }
        // Ensure the last fraction is not greater than 1
        if (fractions.length > 0 && fractions[fractions.length - 1] > 1.0f) {
            fractions[fractions.length - 1] = 1.0f;
        }

        return fractions;
    }

    private static void distributeFractions(List<ColorStop> stops) {
        if (stops.isEmpty()) return;

        if (stops.get(0).fraction == null) {
            stops.get(0).fraction = 0f;
        }
        if (stops.get(stops.size() - 1).fraction == null) {
            stops.get(stops.size() - 1).fraction = 1f;
        }

        int lastDefinedIndex = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).fraction != null) {
                if (lastDefinedIndex != -1 && i > lastDefinedIndex + 1) {
                    float startFraction = stops.get(lastDefinedIndex).fraction;
                    float endFraction = stops.get(i).fraction;
                    int gapSize = i - lastDefinedIndex;
                    for (int j = 1; j < gapSize; j++) {
                        stops.get(lastDefinedIndex + j).fraction = startFraction + (endFraction - startFraction) * j / gapSize;
                    }
                }
                lastDefinedIndex = i;
            }
        }
    }

    private static class ColorStop implements Comparable<ColorStop> {
        Color color;
        /**
         * Use Float to allow nulls during parsing
         */
        Float fraction;

        ColorStop(Color color, Float fraction) {
            this.color = color;
            this.fraction = fraction;
        }

        @Override
        public int compareTo(@NotNull ColorStop other) {
            // Sort by fraction. If fractions are equal, the original order is preserved.
            if (this.fraction == null && other.fraction == null) {
                return 0;
            }
            if (this.fraction == null) {
                return -1;
            }
            if (other.fraction == null) {
                return 1;
            }
            return Float.compare(this.fraction, other.fraction);
        }
    }

}
