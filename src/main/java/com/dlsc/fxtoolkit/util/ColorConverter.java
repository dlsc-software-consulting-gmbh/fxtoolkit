package com.dlsc.fxtoolkit.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ColorConverter {

    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("aliceblue", Color.decode("#f0f8ff"));
        NAMED_COLORS.put("antiquewhite", Color.decode("#faebd7"));
        NAMED_COLORS.put("aqua", Color.decode("#00ffff"));
        NAMED_COLORS.put("aquamarine", Color.decode("#7fffd4"));
        NAMED_COLORS.put("azure", Color.decode("#f0ffff"));
        NAMED_COLORS.put("beige", Color.decode("#f5f5dc"));
        NAMED_COLORS.put("bisque", Color.decode("#ffe4c4"));
        NAMED_COLORS.put("black", Color.decode("#000000"));
        NAMED_COLORS.put("blanchedalmond", Color.decode("#ffebcd"));
        NAMED_COLORS.put("blue", Color.decode("#0000ff"));
        NAMED_COLORS.put("blueviolet", Color.decode("#8a2be2"));
        NAMED_COLORS.put("brown", Color.decode("#a52a2a"));
        NAMED_COLORS.put("burlywood", Color.decode("#deb887"));
        NAMED_COLORS.put("cadetblue", Color.decode("#5f9ea0"));
        NAMED_COLORS.put("chartreuse", Color.decode("#7fff00"));
        NAMED_COLORS.put("chocolate", Color.decode("#d2691e"));
        NAMED_COLORS.put("coral", Color.decode("#ff7f50"));
        NAMED_COLORS.put("cornflowerblue", Color.decode("#6495ed"));
        NAMED_COLORS.put("cornsilk", Color.decode("#fff8dc"));
        NAMED_COLORS.put("crimson", Color.decode("#dc143c"));
        NAMED_COLORS.put("cyan", Color.decode("#00ffff"));
        NAMED_COLORS.put("darkblue", Color.decode("#00008b"));
        NAMED_COLORS.put("darkcyan", Color.decode("#008b8b"));
        NAMED_COLORS.put("darkgoldenrod", Color.decode("#b8860b"));
        NAMED_COLORS.put("darkgray", Color.decode("#a9a9a9"));
        NAMED_COLORS.put("darkgreen", Color.decode("#006400"));
        NAMED_COLORS.put("darkgrey", Color.decode("#a9a9a9"));
        NAMED_COLORS.put("darkkhaki", Color.decode("#bdb76b"));
        NAMED_COLORS.put("darkmagenta", Color.decode("#8b008b"));
        NAMED_COLORS.put("darkolivegreen", Color.decode("#556b2f"));
        NAMED_COLORS.put("darkorange", Color.decode("#ff8c00"));
        NAMED_COLORS.put("darkorchid", Color.decode("#9932cc"));
        NAMED_COLORS.put("darkred", Color.decode("#8b0000"));
        NAMED_COLORS.put("darksalmon", Color.decode("#e9967a"));
        NAMED_COLORS.put("darkseagreen", Color.decode("#8fbc8f"));
        NAMED_COLORS.put("darkslateblue", Color.decode("#483d8b"));
        NAMED_COLORS.put("darkslategray", Color.decode("#2f4f4f"));
        NAMED_COLORS.put("darkslategrey", Color.decode("#2f4f4f"));
        NAMED_COLORS.put("darkturquoise", Color.decode("#00ced1"));
        NAMED_COLORS.put("darkviolet", Color.decode("#9400d3"));
        NAMED_COLORS.put("deeppink", Color.decode("#ff1493"));
        NAMED_COLORS.put("deepskyblue", Color.decode("#00bfff"));
        NAMED_COLORS.put("dimgray", Color.decode("#696969"));
        NAMED_COLORS.put("dimgrey", Color.decode("#696969"));
        NAMED_COLORS.put("dodgerblue", Color.decode("#1e90ff"));
        NAMED_COLORS.put("firebrick", Color.decode("#b22222"));
        NAMED_COLORS.put("floralwhite", Color.decode("#fffaf0"));
        NAMED_COLORS.put("forestgreen", Color.decode("#228b22"));
        NAMED_COLORS.put("fuchsia", Color.decode("#ff00ff"));
        NAMED_COLORS.put("gainsboro", Color.decode("#dcdcdc"));
        NAMED_COLORS.put("ghostwhite", Color.decode("#f8f8ff"));
        NAMED_COLORS.put("gold", Color.decode("#ffd700"));
        NAMED_COLORS.put("goldenrod", Color.decode("#daa520"));
        NAMED_COLORS.put("gray", Color.decode("#808080"));
        NAMED_COLORS.put("green", Color.decode("#008000"));
        NAMED_COLORS.put("greenyellow", Color.decode("#adff2f"));
        NAMED_COLORS.put("grey", Color.decode("#808080"));
        NAMED_COLORS.put("honeydew", Color.decode("#f0fff0"));
        NAMED_COLORS.put("hotpink", Color.decode("#ff69b4"));
        NAMED_COLORS.put("indianred", Color.decode("#cd5c5c"));
        NAMED_COLORS.put("indigo", Color.decode("#4b0082"));
        NAMED_COLORS.put("ivory", Color.decode("#fffff0"));
        NAMED_COLORS.put("khaki", Color.decode("#f0e68c"));
        NAMED_COLORS.put("lavender", Color.decode("#e6e6fa"));
        NAMED_COLORS.put("lavenderblush", Color.decode("#fff0f5"));
        NAMED_COLORS.put("lawngreen", Color.decode("#7cfc00"));
        NAMED_COLORS.put("lemonchiffon", Color.decode("#fffacd"));
        NAMED_COLORS.put("lightblue", Color.decode("#add8e6"));
        NAMED_COLORS.put("lightcoral", Color.decode("#f08080"));
        NAMED_COLORS.put("lightcyan", Color.decode("#e0ffff"));
        NAMED_COLORS.put("lightgoldenrodyellow", Color.decode("#fafad2"));
        NAMED_COLORS.put("lightgray", Color.decode("#d3d3d3"));
        NAMED_COLORS.put("lightgreen", Color.decode("#90ee90"));
        NAMED_COLORS.put("lightgrey", Color.decode("#d3d3d3"));
        NAMED_COLORS.put("lightpink", Color.decode("#ffb6c1"));
        NAMED_COLORS.put("lightsalmon", Color.decode("#ffa07a"));
        NAMED_COLORS.put("lightseagreen", Color.decode("#20b2aa"));
        NAMED_COLORS.put("lightskyblue", Color.decode("#87cefa"));
        NAMED_COLORS.put("lightslategray", Color.decode("#778899"));
        NAMED_COLORS.put("lightslategrey", Color.decode("#778899"));
        NAMED_COLORS.put("lightsteelblue", Color.decode("#b0c4de"));
        NAMED_COLORS.put("lightyellow", Color.decode("#ffffe0"));
        NAMED_COLORS.put("lime", Color.decode("#00ff00"));
        NAMED_COLORS.put("limegreen", Color.decode("#32cd32"));
        NAMED_COLORS.put("linen", Color.decode("#faf0e6"));
        NAMED_COLORS.put("magenta", Color.decode("#ff00ff"));
        NAMED_COLORS.put("maroon", Color.decode("#800000"));
        NAMED_COLORS.put("mediumaquamarine", Color.decode("#66cdaa"));
        NAMED_COLORS.put("mediumblue", Color.decode("#0000cd"));
        NAMED_COLORS.put("mediumorchid", Color.decode("#ba55d3"));
        NAMED_COLORS.put("mediumpurple", Color.decode("#9370db"));
        NAMED_COLORS.put("mediumseagreen", Color.decode("#3cb371"));
        NAMED_COLORS.put("mediumslateblue", Color.decode("#7b68ee"));
        NAMED_COLORS.put("mediumspringgreen", Color.decode("#00fa9a"));
        NAMED_COLORS.put("mediumturquoise", Color.decode("#48d1cc"));
        NAMED_COLORS.put("mediumvioletred", Color.decode("#c71585"));
        NAMED_COLORS.put("midnightblue", Color.decode("#191970"));
        NAMED_COLORS.put("mintcream", Color.decode("#f5fffa"));
        NAMED_COLORS.put("mistyrose", Color.decode("#ffe4e1"));
        NAMED_COLORS.put("moccasin", Color.decode("#ffe4b5"));
        NAMED_COLORS.put("navajowhite", Color.decode("#ffdead"));
        NAMED_COLORS.put("navy", Color.decode("#000080"));
        NAMED_COLORS.put("oldlace", Color.decode("#fdf5e6"));
        NAMED_COLORS.put("olive", Color.decode("#808000"));
        NAMED_COLORS.put("olivedrab", Color.decode("#6b8e23"));
        NAMED_COLORS.put("orange", Color.decode("#ffa500"));
        NAMED_COLORS.put("orangered", Color.decode("#ff4500"));
        NAMED_COLORS.put("orchid", Color.decode("#da70d6"));
        NAMED_COLORS.put("palegoldenrod", Color.decode("#eee8aa"));
        NAMED_COLORS.put("palegreen", Color.decode("#98fb98"));
        NAMED_COLORS.put("paleturquoise", Color.decode("#afeeee"));
        NAMED_COLORS.put("palevioletred", Color.decode("#db7093"));
        NAMED_COLORS.put("papayawhip", Color.decode("#ffefd5"));
        NAMED_COLORS.put("peachpuff", Color.decode("#ffdab9"));
        NAMED_COLORS.put("peru", Color.decode("#cd853f"));
        NAMED_COLORS.put("pink", Color.decode("#ffc0cb"));
        NAMED_COLORS.put("plum", Color.decode("#dda0dd"));
        NAMED_COLORS.put("powderblue", Color.decode("#b0e0e6"));
        NAMED_COLORS.put("purple", Color.decode("#800080"));
        NAMED_COLORS.put("red", Color.decode("#ff0000"));
        NAMED_COLORS.put("rosybrown", Color.decode("#bc8f8f"));
        NAMED_COLORS.put("royalblue", Color.decode("#4169e1"));
        NAMED_COLORS.put("saddlebrown", Color.decode("#8b4513"));
        NAMED_COLORS.put("salmon", Color.decode("#fa8072"));
        NAMED_COLORS.put("sandybrown", Color.decode("#f4a460"));
        NAMED_COLORS.put("seagreen", Color.decode("#2e8b57"));
        NAMED_COLORS.put("seashell", Color.decode("#fff5ee"));
        NAMED_COLORS.put("sienna", Color.decode("#a0522d"));
        NAMED_COLORS.put("silver", Color.decode("#c0c0c0"));
        NAMED_COLORS.put("skyblue", Color.decode("#87ceeb"));
        NAMED_COLORS.put("slateblue", Color.decode("#6a5acd"));
        NAMED_COLORS.put("slategray", Color.decode("#708090"));
        NAMED_COLORS.put("slategrey", Color.decode("#708090"));
        NAMED_COLORS.put("snow", Color.decode("#fffafa"));
        NAMED_COLORS.put("springgreen", Color.decode("#00ff7f"));
        NAMED_COLORS.put("steelblue", Color.decode("#4682b4"));
        NAMED_COLORS.put("tan", Color.decode("#d2b48c"));
        NAMED_COLORS.put("teal", Color.decode("#008080"));
        NAMED_COLORS.put("thistle", Color.decode("#d8bfd8"));
        NAMED_COLORS.put("tomato", Color.decode("#ff6347"));
        NAMED_COLORS.put("turquoise", Color.decode("#40e0d0"));
        NAMED_COLORS.put("violet", Color.decode("#ee82ee"));
        NAMED_COLORS.put("wheat", Color.decode("#f5deb3"));
        NAMED_COLORS.put("white", Color.decode("#ffffff"));
        NAMED_COLORS.put("whitesmoke", Color.decode("#f5f5f5"));
        NAMED_COLORS.put("yellow", Color.decode("#ffff00"));
        NAMED_COLORS.put("yellowgreen", Color.decode("#9acd32"));
        NAMED_COLORS.put("transparent", new Color(0, 0, 0, 0));
    }

    /**
     * Parses a string representation of a color into a Color object.
     * Supports named colors, hex, rgb, rgba, hsb, and hsba formats.
     *
     * @param input The color string to parse.
     * @return an Optional containing the Color if parsing is successful, otherwise an empty Optional.
     */
    public static Optional<Color> parseColor(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmedInput = input.trim().toLowerCase();

        Color namedColor = NAMED_COLORS.get(trimmedInput);
        if (namedColor != null) {
            return Optional.of(namedColor);
        }

        if (trimmedInput.startsWith("#")) {
            return parseHexColor(trimmedInput);
        }
        if (trimmedInput.startsWith("rgb")) {
            return parseRgbColor(trimmedInput);
        }
        if (trimmedInput.startsWith("hsb")) {
            return parseHsbColor(trimmedInput);
        }

        return Optional.empty();
    }

    public static Color parseOrNull(String input) {
        return parseOrDefault(input, null);
    }

    public static Color parseOrDefault(String input, Color defaultValue) {
        return parseColor(input).orElse(defaultValue);
    }

    /**
     * A simplified validation method that leverages the parseColor method.
     *
     * @param input The color string to validate.
     * @return true if the string is a valid color representation, false otherwise.
     */
    public static boolean isValidColor(String input) {
        return parseColor(input).isPresent();
    }

    private static Optional<Color> parseHexColor(String hexInput) {
        String hex = hexInput.substring(1);
        if (hex.length() == 3) {
            char r = hex.charAt(0);
            char g = hex.charAt(1);
            char b = hex.charAt(2);
            hex = new String(new char[]{r, r, g, g, b, b});
        }

        if (hex.length() == 6) {
            try {
                return Optional.of(Color.decode("#" + hex));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else if (hex.length() == 8) {
            try {
                long value = Long.parseLong(hex, 16);
                int r = (int) ((value >> 24) & 0xFF);
                int g = (int) ((value >> 16) & 0xFF);
                int b = (int) ((value >> 8) & 0xFF);
                int a = (int) (value & 0xFF);
                return Optional.of(new Color(r, g, b, a));
            } catch (NumberFormatException e) {
                // System.out.println("Invalid hex color format: " + hexInput);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<Color> parseRgbColor(String rgbInput) {
        boolean hasAlpha = rgbInput.startsWith("rgba(");
        String content = extractContent(rgbInput);
        if (content == null) return Optional.empty();

        String[] parts = content.split(",");
        if ((hasAlpha && parts.length != 4) || (!hasAlpha && parts.length != 3)) {
            return Optional.empty();
        }

        try {
            int r = parseComponent(parts[0].trim());
            int g = parseComponent(parts[1].trim());
            int b = parseComponent(parts[2].trim());
            int a = 255;
            if (hasAlpha) {
                a = Math.round(Float.parseFloat(parts[3].trim()) * 255);
            }
            return Optional.of(new Color(r, g, b, a));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<Color> parseHsbColor(String hsbInput) {
        boolean hasAlpha = hsbInput.startsWith("hsba(");
        String content = extractContent(hsbInput);
        if (content == null) return Optional.empty();

        String[] parts = content.split(",");
        if ((hasAlpha && parts.length != 4) || (!hasAlpha && parts.length != 3)) {
            return Optional.empty();
        }

        try {
            float hue = Float.parseFloat(parts[0].trim()) / 360f;
            float saturation = parsePercentage(parts[1].trim());
            float brightness = parsePercentage(parts[2].trim());
            float alpha = 1.0f;
            if (hasAlpha) {
                alpha = Float.parseFloat(parts[3].trim());
            }
            Color base = Color.getHSBColor(hue, saturation, brightness);
            return Optional.of(new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.round(alpha * 255)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String extractContent(String functionInput) {
        int start = functionInput.indexOf('(');
        int end = functionInput.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return functionInput.substring(start + 1, end);
        }
        return null;
    }

    private static int parseComponent(String s) {
        if (s.endsWith("%")) {
            float percent = Float.parseFloat(s.substring(0, s.length() - 1).trim());
            return Math.round(percent / 100f * 255);
        } else {
            return Integer.parseInt(s.trim());
        }
    }

    private static float parsePercentage(String s) {
        if (s.endsWith("%")) {
            return Float.parseFloat(s.substring(0, s.length() - 1).trim()) / 100f;
        } else {
            return Float.parseFloat(s.trim());
        }
    }
}