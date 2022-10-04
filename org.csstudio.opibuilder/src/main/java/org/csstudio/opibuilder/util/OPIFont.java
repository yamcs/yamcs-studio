/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.Objects;

import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * The dedicated font type which supports predefined font name in OPI builder font file. If the font name doesn't exist
 * in the font file, the system font will be adopted.
 */
public class OPIFont {

    public static final int POINTS_PER_INCH = 72;

    private String fontName;

    /**
     * FontData object. If sizeInPixels, the height represents height in pixels rather than the standard SWT
     * representation in points.
     */
    private FontData rawFontData;

    /**
     * Whether the font exists in the MediaService cache.
     */
    private boolean preDefined;

    /**
     * Whether to override the standard font size interpretation and use the font size to determine the number of
     * vertical pixels used in rendering the text.
     */
    private boolean sizeInPixels = false;

    public OPIFont(String name, FontData fontData) {
        fontName = name;
        rawFontData = fontData;
        preDefined = true;
    }

    public OPIFont(FontData fontData) {
        fontName = fontData.toString();
        rawFontData = fontData;
        preDefined = false;
    }

    public OPIFont(OPIFont opiFont) {
        this(opiFont.getFontMacroName(), opiFont.rawFontData);
        preDefined = opiFont.isPreDefined();
        sizeInPixels = opiFont.isSizeInPixels();
    }

    private int pixelsToPoints(int pixels) {
        var result = (float) pixels * POINTS_PER_INCH / Display.getDefault().getDPI().y;
        return Math.round(result);
    }

    /**
     * Returns the Macro Name of the OPIFont.
     *
     * @return the predefined font macro name or flattened font data string if it is not predefined.
     */
    public String getFontMacroName() {
        return fontName;
    }

    /**
     * Returns the name of the Font. On platforms that support font foundries, the return value will be the foundry
     * followed by a dash ("-") followed by the face name.
     *
     * @return the name of the font
     *
     */
    public String getFontName() {
        return getFontData().getName();
    }

    /**
     * Returns the height of the font in either fonts or pixels depending on the value of sizeInPixels.
     *
     * @return the height of the font.
     *
     */
    public int getHeight() {
        return getFontData().getHeight();
    }

    /**
     * Returns the style of the receiver which is a bitwise OR of one or more of the <code>SWT</code> constants
     * NORMAL(0), BOLD(2) and ITALIC(1).
     *
     * @return the style of the font.
     *
     */
    public int getStyle() {
        return getFontData().getStyle();
    }

    /**
     * Return the FontData, scaled according to whether its size represents pixels or points. This may be called only on
     * the UI thread, since it uses the SWT Display to do the scaling.
     *
     * @return the scaled FontData
     */
    public FontData getFontData() {
        var rawSize = rawFontData.getHeight();
        if (sizeInPixels) {
            rawSize = pixelsToPoints(rawSize);
        }
        return new FontData(rawFontData.getName(), rawSize, rawFontData.getStyle());
    }

    /**
     * Return the raw FontData, not scaled.
     *
     * @return the raw FontData.
     */
    public FontData getRawFontData() {
        return rawFontData;
    }

    /**
     * Return the appropriately-scaled SWT font.
     *
     * @return scaled SWT font
     */
    public Font getSWTFont() {
        return CustomMediaFactory.getInstance().getFont(getFontData());
    }

    /**
     * @return true if this font is predefined in font file, false otherwise.
     */
    public boolean isPreDefined() {
        return preDefined;
    }

    @Override
    public String toString() {
        return fontName;
    }

    @Override
    public int hashCode() {
        var prime = 31;
        var result = 1;
        result = prime * result + ((rawFontData == null) ? 0 : rawFontData.hashCode());
        result = prime * result + ((fontName == null) ? 0 : fontName.hashCode());
        result = prime * result + ((sizeInPixels) ? 0 : 1);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (OPIFont) obj;
        if (other.sizeInPixels != sizeInPixels) {
            return false;
        }
        if (!Objects.equals(rawFontData, other.rawFontData)) {
            return false;
        }
        if (!Objects.equals(fontName, other.fontName)) {
            return false;
        }
        return true;
    }

    public void setSizeInPixels(boolean sizeInPixels) {
        this.sizeInPixels = sizeInPixels;
    }

    public boolean isSizeInPixels() {
        return sizeInPixels;
    }
}
