package org.yamcs.studio.theming;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleDefinition {
    
    private Map<String, Color> colors = new LinkedHashMap<>();
    private Map<String, Font> fonts = new LinkedHashMap<>();
    
    private static final Pattern COLOR_RULE = Pattern.compile("([^=]*[^=\\s])\\s?=\\s?([0-9]+)\\s?,\\s?([0-9]+)\\s?,\\s?([0-9]+)\\s?");
    private static final Pattern FONT_RULE = Pattern.compile("([^=]*[^=\\s])\\s?=\\s?(.*[^\\s])\\-(regular|italic|bold)\\-([0-9]+)\\s?");
    
    public static StyleDefinition fromFile(File file) throws FileNotFoundException, IOException {
        StyleDefinition def = new StyleDefinition();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
                    def.colors.put(label, new Color(r, g, b));
                } else if (fontMatcher.matches()) {
                    String label = fontMatcher.group(1);
                    String fontName = fontMatcher.group(2).trim();
                    String fontStyle = fontMatcher.group(3);
                    int fontSize = Integer.parseInt(fontMatcher.group(4));
                    def.fonts.put(label, new Font(fontName, fontStyle, fontSize));
                }
            }
        }
        return def;
    }
    
    private static class Color {
        int r;
        int g;
        int b;
        Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    private static class Font {
        String name;
        String style;
        int size;
        Font(String name, String style, int size) {
            this.name = name;
            this.style = style;
            this.size = size;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        colors.forEach((k, v) -> {
            buf.append(k).append(" = ").append(v.r).append(',').append(v.g).append(',').append(v.b).append("\n");
        });
        fonts.forEach((k, v) -> {
            buf.append(k).append(" = ").append(v.name).append('-').append(v.style).append('-').append(v.size).append("\n");
        });
        return buf.toString();
    }
    
    public static void main (String... args) throws FileNotFoundException, IOException {
        System.out.println(StyleDefinition.fromFile(new File("/Users/fdi/src/runtime-yamcs-studio.product/YSS Landing/color.def")));
    }
}
