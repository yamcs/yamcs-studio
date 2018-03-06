package org.yamcs.studio.css.theming;

public class ThemeFont {
    String label;
    String name;
    String style;
    int size;

    ThemeFont(String label, String name, String style, int size) {
        this.label = label;
        this.name = name;
        this.style = style;
        this.size = size;
    }
}