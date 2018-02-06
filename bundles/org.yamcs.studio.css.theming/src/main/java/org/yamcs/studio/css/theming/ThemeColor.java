package org.yamcs.studio.css.theming;

import org.eclipse.swt.graphics.RGB;

public class ThemeColor {

    private String label;
    private RGB rgb;

    ThemeColor(String label, int r, int g, int b) {
        this.label = label;
        rgb = new RGB(r, g, b);
    }

    public String getLabel() {
        return label;
    }

    public RGB getRGB() {
        return rgb;
    }
}
