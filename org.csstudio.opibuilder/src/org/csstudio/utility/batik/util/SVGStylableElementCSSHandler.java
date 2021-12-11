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

import org.apache.batik.anim.dom.SVGStylableElement;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.StyleDeclaration;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.RGBColorValue;
import org.apache.batik.css.engine.value.Value;
import org.eclipse.swt.graphics.Color;

/**
 * Manages the update of CSS defined colors of {@link SVGStylableElement}. Always updates the original style.
 */
public class SVGStylableElementCSSHandler implements ICSSHandler {

    private final CSSEngine cssEngine;
    private final SVGStylableElement element;
    private final CloneableStyleDeclaration originalStyle;
    private final String originalCSSText;

    public SVGStylableElementCSSHandler(CSSEngine cssEngine, SVGStylableElement element) {
        this.cssEngine = cssEngine;
        this.element = element;
        this.originalCSSText = element.getStyle().getCssText();
        var embedStyle = (SVGStylableElement.StyleDeclaration) element.getStyle();
        this.originalStyle = new CloneableStyleDeclaration(embedStyle.getStyleDeclaration());
    }

    @Override
    public void updateCSSColor(Color colorToChange, Color newColor) {
        if (colorToChange == null || newColor == null || colorToChange.equals(newColor)) {
            return;
        }

        var newRedValue = new FloatValue((short) 1, (float) newColor.getRed());
        var newGreenValue = new FloatValue((short) 1, (float) newColor.getGreen());
        var newBlueValue = new FloatValue((short) 1, (float) newColor.getBlue());
        var newRGBColorValue = new RGBColorValue(newRedValue, newGreenValue, newBlueValue);

        StyleDeclaration sd = originalStyle.clone();
        var sdlen = sd.size();
        for (var sdindex = 0; sdindex < sdlen; sdindex++) {
            var val = sd.getValue(sdindex);
            if (val instanceof RGBColorValue) {
                var colorVal = (RGBColorValue) val;
                if (isSameColor(colorVal, colorToChange)) {
                    sd.put(sdindex, newRGBColorValue, sd.getIndex(sdindex), sd.getPriority(sdindex));
                }
            }
        }
        element.getStyle().setCssText(sd.toString(cssEngine));
    }

    private boolean isSameColor(RGBColorValue colorVal, Color swtColor) {
        if (colorVal.getCssText().contains("%")) {
            var nr = Math.round(swtColor.getRed() / 255f * 100);
            var ng = Math.round(swtColor.getGreen() / 255f * 100);
            var nb = Math.round(swtColor.getBlue() / 255f * 100);
            var or = Math.round(colorVal.getRed().getFloatValue());
            var og = Math.round(colorVal.getGreen().getFloatValue());
            var ob = Math.round(colorVal.getBlue().getFloatValue());
            if (or == nr && og == ng && ob == nb) {
                return true;
            }
        } else if (colorVal.getRed().getFloatValue() == swtColor.getRed()
                && colorVal.getGreen().getFloatValue() == swtColor.getGreen()
                && colorVal.getBlue().getFloatValue() == swtColor.getBlue()) {
            return true;
        }
        return false;
    }

    @Override
    public void resetCSSStyle() {
        element.getStyle().setCssText(originalCSSText);
    }

    protected class CloneableStyleDeclaration extends StyleDeclaration {

        public CloneableStyleDeclaration(StyleDeclaration sd) {
            this.count = sd.size();
            this.values = new Value[count];
            for (var idx = 0; idx < count; idx++) {
                this.values[idx] = sd.getValue(idx);
            }
            this.indexes = new int[count];
            for (var idx = 0; idx < count; idx++) {
                this.indexes[idx] = sd.getIndex(idx);
            }
            this.priorities = new boolean[count];
            for (var idx = 0; idx < count; idx++) {
                this.priorities[idx] = sd.getPriority(idx);
            }
        }

        @Override
        public CloneableStyleDeclaration clone() {
            return new CloneableStyleDeclaration(this);
        }

    }

}
