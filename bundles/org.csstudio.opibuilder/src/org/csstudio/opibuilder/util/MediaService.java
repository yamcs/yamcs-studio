/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A service help to maintain the color macros.
 *
 * @author Xihui Chen
 *
 */
public final class MediaService {

    public static final String DEFAULT_FONT = "Default";

    public static final String DEFAULT_BOLD_FONT = "Default Bold";

    public static final String HEADER1 = "Header 1";
    public static final String HEADER2 = "Header 2";
    public static final String HEADER3 = "Header 3";
    public static final String FINE_PRINT = "Fine Print";

    private static MediaService instance = null;

    private Map<String, OPIColor> colorMap;
    private Map<String, OPIFont> fontMap;

    private IPath colorFilePath;
    private IPath fontFilePath;

    public final static RGB DEFAULT_UNKNOWN_COLOR = new RGB(67, 63, 61);

    public final static FontData DEFAULT_UNKNOWN_FONT = CustomMediaFactory.FONT_ARIAL;

    public synchronized static final MediaService getInstance() {
        if (instance == null) {
            instance = new MediaService();
        }
        return instance;
    }

    public MediaService() {
        colorMap = new LinkedHashMap<>();
        fontMap = new LinkedHashMap<>();
        reloadColorFile();
        reloadFontFile();
    }

    private void loadPredefinedColors() {
        colorMap.put(AlarmRepresentationScheme.MAJOR, new OPIColor(AlarmRepresentationScheme.MAJOR,
                CustomMediaFactory.COLOR_RED, true));
        colorMap.put(AlarmRepresentationScheme.MINOR, new OPIColor(AlarmRepresentationScheme.MINOR,
                CustomMediaFactory.COLOR_ORANGE, true));
        colorMap.put(AlarmRepresentationScheme.INVALID, new OPIColor(
                AlarmRepresentationScheme.INVALID, CustomMediaFactory.COLOR_PINK, true));
        colorMap.put(AlarmRepresentationScheme.DISCONNECTED, new OPIColor(
                AlarmRepresentationScheme.DISCONNECTED, CustomMediaFactory.COLOR_PINK, true));
    }

    private void loadPredefinedFonts() {
        FontData defaultFont = Display.getDefault().getSystemFont().getFontData()[0];

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
    }

    /**
     * Reload color and font files. Should be called in UI thread.
     */
    public synchronized void reload() {
        reloadColorFile();
        reloadFontFile();
    }

    /**
     * Reload predefined colors from color file in a background job.
     */
    public synchronized void reloadColorFile() {
        colorFilePath = PreferencesHelper.getColorFilePath();

        Job job = new Job("Load Color File") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Connecting to " + colorFilePath, IProgressMonitor.UNKNOWN);
                colorMap.clear();
                loadColorFile();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();

    }

    /**
     * Reload predefined fonts from font file in a background job.
     */
    public synchronized void reloadFontFile() {
        fontMap.clear();
        final StringBuilder systemFontName = new StringBuilder();
        if (Display.getCurrent() != null) {
            loadPredefinedFonts();
            systemFontName.append(Display.getCurrent().getSystemFont().getFontData()[0]
                    .getName());
        } else {
            DisplayUtils.getDisplay().asyncExec(() -> loadPredefinedFonts());
            systemFontName.append("Verdana");
        }
        fontFilePath = PreferencesHelper.getFontFilePath();

        Job job = new Job("Load Font File") {
            @Override
            public IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Connecting to " + fontFilePath, IProgressMonitor.UNKNOWN);
                loadFontFile(systemFontName.toString());
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    /**
     * @return true if load successfully.
     */
    private void loadColorFile() {
        loadPredefinedColors();

        colorFilePath = PreferencesHelper.getColorFilePath();
        if (colorFilePath == null || colorFilePath.isEmpty()) {
            return;
        }

        try {
            // read file
            InputStream inputStream = ResourceUtil.pathToInputStream(colorFilePath, false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            // fill the color map.
            while ((line = reader.readLine()) != null) {
                // support comments
                if (line.trim().startsWith("#") || line.trim().startsWith("//")) {
                    continue;
                }
                int i;
                if ((i = line.indexOf('=')) != -1) {
                    String name = line.substring(0, i).trim();
                    try {
                        // Display builder allows both R, G, B
                        // NAME=255, 255, 255
                        // and
                        // NAME=255, 255, 255, 255
                        // with optional alpha value.
                        // This call handles both by ignoring the alpha value
                        RGB color = StringConverter.asRGB(line.substring(i + 1).trim());

                        colorMap.put(name, new OPIColor(name, color, true));
                    } catch (DataFormatException e) {
                        String message = "Format error in color definition file.";
                        OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                    }
                }
            }
            inputStream.close();
            reader.close();
        } catch (Exception e) {
            String message = "Failed to read color definition file.";
            OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
        }
    }

    private void loadFontFile(String systemFontName) {
        Map<String, OPIFont> rawFontMap = new LinkedHashMap<>();
        Set<String> trimmedNameSet = new LinkedHashSet<>();
        fontFilePath = PreferencesHelper.getFontFilePath();
        if (fontFilePath == null || fontFilePath.isEmpty()) {
            return;
        }

        try {
            // read file
            InputStream inputStream = ResourceUtil.pathToInputStream(fontFilePath, false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            // fill the font map.
            while ((line = reader.readLine()) != null) {
                // support comments
                if (line.trim().startsWith("#") || line.trim().startsWith("//")) {
                    continue;
                }
                int i;
                if ((i = line.indexOf('=')) != -1) {
                    boolean isPixels = false;
                    String name = line.substring(0, i).trim();
                    String trimmedName = name;
                    if (name.contains("(")) {
                        trimmedName = name.substring(0, name.indexOf("("));
                    }
                    trimmedNameSet.add(trimmedName);
                    try {
                        String trimmedLine = line.substring(i + 1).trim();
                        if (trimmedLine.endsWith("px")) {
                            isPixels = true;
                            trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 2);
                        } else if (line.endsWith("pt")) {
                            trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 2);
                        }

                        // BOY only handles "Liberation Sans-regular-12",
                        // while Display Builder allows additional spaces as in
                        // "Liberation Sans - regular - 12".
                        // Patch line to be upwards-compatible
                        trimmedLine = trimmedLine.replaceAll(" +- +", "-");
                        FontData fontdata = StringConverter.asFontData(trimmedLine);
                        if (fontdata.getName().equals("SystemDefault")) {
                            fontdata.setName(systemFontName);
                        }
                        OPIFont font = new OPIFont(trimmedName, fontdata);
                        font.setSizeInPixels(isPixels);
                        rawFontMap.put(name, font);
                    } catch (DataFormatException e) {
                        String message = "Format error in font definition file.";
                        OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                    }
                }
            }
            inputStream.close();
            reader.close();
        } catch (Exception e) {
            String message = "Failed to read font definition file.";
            OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
        }

        String osname = getOSName();
        for (String trimmedName : trimmedNameSet) {
            String equippedName = trimmedName + "(" + osname + ")";
            if (rawFontMap.containsKey(equippedName)) {
                fontMap.put(trimmedName, rawFontMap.get(equippedName));
            } else if (rawFontMap.containsKey(trimmedName)) {
                fontMap.put(trimmedName, rawFontMap.get(trimmedName));
            }
        }

    }

    private String getOSName() {
        String osname = System.getProperty("os.name").trim();
        String wsname = Util.getWS().trim();
        osname = StringConverter.removeWhiteSpaces(osname).toLowerCase();
        if (wsname != null && wsname.length() > 0) {
            wsname = StringConverter.removeWhiteSpaces(wsname).toLowerCase();
            osname = osname + "_" + wsname;
        }
        return osname;
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
     *
     * @param name
     *            name of OPIColor
     * @param rgb
     *            rgb value in case the name is not exist.
     * @return the OPIColor.
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

    /**
     * @param name
     * @return true if the OPI color is defined.
     */
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
     * @return new OPIFont
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
     * @return new OPIFont
     * @see #getOPIFont(String, FontData)
     */
    public OPIFont getOPIFont(String name) {
        return getOPIFont(name, DEFAULT_UNKNOWN_FONT);
    }

    /**
     * Return an array of a copy of all predefined fonts.
     * 
     * @return array of predefined fonts
     */
    public OPIFont[] getAllPredefinedFonts() {
        OPIFont[] result = new OPIFont[fontMap.size()];
        int i = 0;
        for (OPIFont c : fontMap.values()) {
            result[i++] = new OPIFont(c);
        }
        return result;
    }

}
