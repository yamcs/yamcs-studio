package org.yamcs.studio.css.theming;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleDefinition {

    private List<ThemeColor> colors = new ArrayList<>();
    private List<ThemeFont> fonts = new ArrayList<>();

    private static final Pattern COLOR_RULE = Pattern
            .compile("([^=]*[^=\\s])\\s?=\\s?([0-9]+)\\s?,\\s?([0-9]+)\\s?,\\s?([0-9]+)\\s?");
    private static final Pattern FONT_RULE = Pattern
            .compile("([^=]*[^=\\s])\\s?=\\s?(.*[^\\s])\\-(regular|italic|bold)\\-([0-9]+)\\s?");

    public static StyleDefinition from(InputStream in) throws FileNotFoundException, IOException {
        StyleDefinition def = new StyleDefinition();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            Matcher colorMatcher = COLOR_RULE.matcher("");
            Matcher fontMatcher = FONT_RULE.matcher("");

            String line;
            while ((line = reader.readLine()) != null) {
                colorMatcher.reset(line);
                fontMatcher.reset(line);

                if (colorMatcher.matches()) {
                    String label = colorMatcher.group(1);
                    int r = Integer.parseInt(colorMatcher.group(2));
                    int g = Integer.parseInt(colorMatcher.group(3));
                    int b = Integer.parseInt(colorMatcher.group(4));
                    def.colors.add(new ThemeColor(label, r, g, b));
                } else if (fontMatcher.matches()) {
                    String label = fontMatcher.group(1);
                    String fontName = fontMatcher.group(2).trim();
                    String fontStyle = fontMatcher.group(3);
                    int fontSize = Integer.parseInt(fontMatcher.group(4));
                    def.fonts.add(new ThemeFont(label, fontName, fontStyle, fontSize));
                }
            }
        }
        return def;
    }

    public List<ThemeColor> getColors() {
        return colors;
    }

    public List<ThemeFont> getFonts() {
        return fonts;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        colors.forEach(c -> {
            buf.append(c.getLabel()).append(" = ").append(c.getRGB().red).append(',').append(c.getRGB().green)
                    .append(',').append(c.getRGB().blue).append("\n");
        });
        fonts.forEach(f -> {
            buf.append(f.label).append(" = ").append(f.name).append('-').append(f.style).append('-').append(f.size)
                    .append("\n");
        });
        return buf.toString();
    }
}
