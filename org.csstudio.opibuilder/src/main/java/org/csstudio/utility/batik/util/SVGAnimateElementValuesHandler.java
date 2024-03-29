/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.utility.batik.util;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.batik.anim.dom.SVGOMAnimateElement;
import org.apache.batik.css.engine.CSSEngine;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.swt.graphics.Color;
import org.w3c.dom.svg.SVGAnimateElement;

/**
 * Manages the update of defined colors values of {@link SVGAnimateElement}. Always updates the original values.
 */
public class SVGAnimateElementValuesHandler implements ICSSHandler {

    private final SVGAnimateElement element;
    private final String originalValuesStr;

    public SVGAnimateElementValuesHandler(CSSEngine cssEngine, SVGAnimateElement element) {
        this.element = element;
        originalValuesStr = element.getAttribute("values");
    }

    @Override
    public void updateCSSColor(Color colorToChange, Color newColor) {
        if (colorToChange == null || newColor == null || colorToChange.equals(newColor)) {
            return;
        }
        try {
            var newValuesStr = replaceValues(originalValuesStr, colorToChange, newColor);
            element.setAttribute("values", newValuesStr);
            // Set context to null to force hot refresh of animation
            ((SVGOMAnimateElement) element).setSVGContext(null);
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    private String replaceValues(String originalValues, Color colorToChange, Color newColor) {
        var sb = new StringBuilder();
        var values = originalValues.split(";");
        var rgbPattern = Pattern.compile("rgb\\(([^\\)]+)\\)");
        for (var value : values) {
            var newValue = value.trim();
            var matcher = rgbPattern.matcher(value.trim());
            if (matcher.matches()) {
                var rgbStr = matcher.group(1).trim();
                var rgb = rgbStr.split(",");
                if (rgbStr.contains("%")) {
                    var cr = Math.round(colorToChange.getRed() / 255f * 100);
                    var cg = Math.round(colorToChange.getGreen() / 255f * 100);
                    var cb = Math.round(colorToChange.getBlue() / 255f * 100);
                    var or = Math.round(Float.valueOf(rgb[0].replace('%', ' ').trim()));
                    var og = Math.round(Float.valueOf(rgb[1].replace('%', ' ').trim()));
                    var ob = Math.round(Float.valueOf(rgb[2].replace('%', ' ').trim()));
                    if (or == cr && og == cg && ob == cb) {
                        var nr = Math.round(newColor.getRed() / 255f * 100);
                        var ng = Math.round(newColor.getGreen() / 255f * 100);
                        var nb = Math.round(newColor.getBlue() / 255f * 100);
                        newValue = "rgb(" + nr + "%," + ng + "%," + nb + "%)";
                    }
                } else {
                    var cr = colorToChange.getRed();
                    var cg = colorToChange.getGreen();
                    var cb = colorToChange.getBlue();
                    int or = Integer.valueOf(rgb[0].trim());
                    int og = Integer.valueOf(rgb[1].trim());
                    int ob = Integer.valueOf(rgb[2].trim());
                    if (or == cr && og == cg && ob == cb) {
                        var nr = newColor.getRed();
                        var ng = newColor.getGreen();
                        var nb = newColor.getBlue();
                        newValue = "rgb(" + nr + "," + ng + "," + nb + ")";
                    }
                }
            } else if (value.trim().startsWith("#")) {
                var svgOldColor = toHexString(colorToChange.getRed(), colorToChange.getGreen(),
                        colorToChange.getBlue());
                var svgNewColor = toHexString(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                if (svgOldColor.equals(value.trim())) {
                    newValue = svgNewColor;
                }
            }
            sb.append(newValue + ";");
        }
        if (sb.length() == 0) {
            return originalValues;
        }
        return sb.substring(0, sb.length() - 1).toString();
    }

    private String toHexString(int r, int g, int b) {
        return "#" + toSVGHexValue(r) + toSVGHexValue(g) + toSVGHexValue(b);
    }

    private String toSVGHexValue(int number) {
        var builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.insert(0, '0'); // pad with leading zero if needed
        }
        return builder.toString().toUpperCase();
    }

    @Override
    public void resetCSSStyle() {
        element.setAttribute("values", originalValuesStr);
        ((SVGOMAnimateElement) element).setSVGContext(null);
    }
}
