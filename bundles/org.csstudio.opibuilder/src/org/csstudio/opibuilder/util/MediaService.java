/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.util;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.preferences.NamedColor;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * A service help to maintain the color macros.
 *
 * @author Xihui Chen
 *
 */
public final class MediaService {

    private static final Logger log = Logger.getLogger(MediaService.class.getName());

    public static final String DEFAULT_FONT = "Default";
    public static final String DEFAULT_BOLD_FONT = "Default Bold";
    public static final String HEADER1 = "Header 1";
    public static final String HEADER2 = "Header 2";
    public static final String HEADER3 = "Header 3";
    public static final String FINE_PRINT = "Fine Print";

    private static MediaService instance;

    private Map<String, OPIColor> colorMap = new LinkedHashMap<>();
    private Map<String, OPIFont> fontMap = new LinkedHashMap<>();

    private static final RGB DEFAULT_UNKNOWN_COLOR = new RGB(67, 63, 61);
    public static final FontData DEFAULT_UNKNOWN_FONT = new FontData("Liberation Sans", 11, SWT.NONE);

    private MediaService() {
        loadBundledFonts();
        reloadColors();
        reloadFonts();
    }

    private void loadBundledFonts() {
        Display display = Display.getCurrent();
        if (!isFontAvailable(display, "Liberation Sans")) {
            // Load the font from within the bundle. It's much better though to have it
            // pre-installed on the system, because then it is also available in the
            // platform-specific FontDialog.
            try {
                Bundle bundle = OPIBuilderPlugin.getDefault().getBundle();

                URL url = FileLocator.find(bundle, new Path("fonts/LiberationSans-Regular.ttf"), null);
                String fontFile = FileLocator.toFileURL(url).getPath().toString();
                if (!display.loadFont(fontFile)) {
                    log.info("Could not load font 'Liberation Sans Regular'");
                }

                url = FileLocator.find(bundle, new Path("fonts/LiberationSans-Bold.ttf"), null);
                fontFile = FileLocator.toFileURL(url).getPath().toString();
                if (!display.loadFont(fontFile)) {
                    log.info("Could not load font 'Liberation Sans Bold'");
                }

                url = FileLocator.find(bundle, new Path("fonts/LiberationSans-BoldItalic.ttf"), null);
                fontFile = FileLocator.toFileURL(url).getPath().toString();
                if (!display.loadFont(fontFile)) {
                    log.info("Could not load font 'Liberation Sans Bold Italic'");
                }

                url = FileLocator.find(bundle, new Path("fonts/LiberationSans-Italic.ttf"), null);
                fontFile = FileLocator.toFileURL(url).getPath().toString();
                if (!display.loadFont(fontFile)) {
                    log.info("Could not load font 'Liberation Sans Italic'");
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Failed to load default fonts", e);
            }
        }
    }

    private static boolean isFontAvailable(Display display, String faceName) {
        FontData[] fontData = display.getFontList(faceName, true);
        return fontData == null || fontData.length == 0;
    }

    public synchronized static final MediaService getInstance() {
        if (instance == null) {
            instance = new MediaService();
        }
        return instance;
    }

    /**
     * Reload predefined colors from color file in a background job.
     */
    public synchronized void reloadColors() {
        colorMap.clear();
        colorMap.put(AlarmRepresentationScheme.MAJOR, new OPIColor(AlarmRepresentationScheme.MAJOR,
                CustomMediaFactory.COLOR_RED, true));
        colorMap.put(AlarmRepresentationScheme.MINOR, new OPIColor(AlarmRepresentationScheme.MINOR,
                CustomMediaFactory.COLOR_ORANGE, true));
        colorMap.put(AlarmRepresentationScheme.INVALID, new OPIColor(
                AlarmRepresentationScheme.INVALID, CustomMediaFactory.COLOR_PINK, true));
        colorMap.put(AlarmRepresentationScheme.DISCONNECTED, new OPIColor(
                AlarmRepresentationScheme.DISCONNECTED, CustomMediaFactory.COLOR_X11_PURPLE, true));

        for (NamedColor color : OPIBuilderPlugin.getDefault().loadColors()) {
            colorMap.put(color.name, new OPIColor(color.name, color.rgb, true));
        }
    }

    public synchronized void reloadFonts() {
        fontMap.clear();

        FontData defaultFont = DEFAULT_UNKNOWN_FONT;

        fontMap.put(DEFAULT_FONT, new OPIFont(DEFAULT_FONT, defaultFont));
        int height = defaultFont.getHeight();
        FontData defaultBoldFont = new FontData(defaultFont.getName(), height, SWT.BOLD);
        fontMap.put(DEFAULT_BOLD_FONT, new OPIFont(DEFAULT_BOLD_FONT, defaultBoldFont));
        FontData header1 = new FontData(defaultFont.getName(), height + 8, SWT.BOLD);
        fontMap.put(HEADER1, new OPIFont(HEADER1, header1));
        FontData header2 = new FontData(defaultFont.getName(), height + 4, SWT.BOLD);
        fontMap.put(HEADER2, new OPIFont(HEADER2, header2));
        FontData header3 = new FontData(defaultFont.getName(), height + 2, SWT.BOLD);
        fontMap.put(HEADER3, new OPIFont(HEADER3, header3));
        FontData finePrint = new FontData(defaultFont.getName(), height - 2, SWT.NORMAL);
        fontMap.put(FINE_PRINT, new OPIFont(FINE_PRINT, finePrint));

        for (OPIFont font : OPIBuilderPlugin.getDefault().loadFonts()) {
            fontMap.put(font.getFontMacroName(), font);
        }
    }

    /**
     * Get the color from the predefined color map, which is defined in the color file.
     *
     * @param name
     *            the predefined name of the color.
     * @return the RGB color, or the default RGB value if the name doesn't exist in the color file.
     */
    public RGB getColor(String name) {
        if (colorMap.containsKey(name)) {
            return colorMap.get(name).getRGBValue();
        }
        return DEFAULT_UNKNOWN_COLOR;
    }

    public OPIColor getOPIColor(String name) {
        return getOPIColor(name, DEFAULT_UNKNOWN_COLOR);
    }

    /**
     * Get OPIColor based on name. If no such name exist, use the rgb value as its color.
     */
    public OPIColor getOPIColor(String name, RGB rgb) {
        if (colorMap.containsKey(name)) {
            return colorMap.get(name);
        }
        return new OPIColor(name, rgb, true);
    }

    public OPIColor[] getAllPredefinedColors() {
        OPIColor[] result = new OPIColor[colorMap.size()];
        int i = 0;
        for (OPIColor c : colorMap.values()) {
            result[i++] = c;
        }
        return result;
    }

    public boolean isColorNameDefined(String name) {
        return colorMap.containsKey(name);
    }

    /**
     * Get a copy the OPIFont from the configured defaults based on name. Use the provided fontData if the name is not
     * in the cache.
     *
     * @param name
     *            of predefined font
     * @param fontData
     *            to use if name is not in cache
     */
    public OPIFont getOPIFont(String name, FontData fontData) {
        if (fontMap.containsKey(name)) {
            return new OPIFont(fontMap.get(name));
        }
        return new OPIFont(name, fontData);
    }

    /**
     * Get a copy of the OPIFont from the configured defaults based on name. Use {@link #DEFAULT_UNKNOWN_FONT} if the
     * name is not in the cache.
     *
     * @param name
     *            of predefined font
     */
    public OPIFont getOPIFont(String name) {
        return getOPIFont(name, DEFAULT_UNKNOWN_FONT);
    }

    public OPIFont[] getAllPredefinedFonts() {
        OPIFont[] result = new OPIFont[fontMap.size()];
        int i = 0;
        for (OPIFont c : fontMap.values()) {
            result[i++] = new OPIFont(c);
        }
        return result;
    }
}
