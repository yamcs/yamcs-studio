/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import java.util.HashMap;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A factory, which provides convinience methods for the creation of Images and Fonts.
 *
 * All resources created via this factory get automatically disposed, when the application is stopped.
 */
public class CustomMediaFactory {

    final static public RGB COLOR_LIGHT_BLUE = new RGB(153, 186, 243);
    final static public RGB COLOR_BLUE = new RGB(0, 0, 255);
    final static public RGB COLOR_WHITE = new RGB(255, 255, 255);
    final static public RGB COLOR_GRAY = new RGB(200, 200, 200);
    final static public RGB COLOR_DARK_GRAY = new RGB(150, 150, 150);
    final static public RGB COLOR_BLACK = new RGB(0, 0, 0);
    final static public RGB COLOR_RED = new RGB(255, 0, 0);
    final static public RGB COLOR_GREEN = new RGB(0, 255, 0);
    final static public RGB COLOR_YELLOW = new RGB(255, 255, 0);
    final static public RGB COLOR_PINK = new RGB(255, 0, 255);
    final static public RGB COLOR_CYAN = new RGB(0, 255, 255);
    final static public RGB COLOR_ORANGE = new RGB(255, 128, 0);
    final static public RGB COLOR_PURPLE = new RGB(128, 0, 255);

    final static public RGB COLOR_X11_PURPLE = new RGB(160, 32, 240);

    /** the font Arial in height of 9 */
    final static public FontData FONT_ARIAL = new FontData("Arial", 9, SWT.NONE);

    private static CustomMediaFactory INSTANCE;

    private ColorRegistry colorRegistry;
    private ImageRegistry imageRegistry;
    private FontRegistry fontRegistry;

    /**
     * Map that holds the provided image descriptors.
     */
    private HashMap<String, Image> imageCache;

    /**
     * Private constructor to avoid instantiation.
     */
    private CustomMediaFactory() {
        var display = Display.getDefault();
        colorRegistry = new ColorRegistry(display);
        imageRegistry = new ImageRegistry(display);
        fontRegistry = new FontRegistry(display);

        imageCache = new HashMap<>();

        // dispose all images from the image cache, when the display is disposed
        display.addListener(SWT.Dispose, event -> {
            for (Image img : imageCache.values()) {
                img.dispose();
            }
        });

    }

    public static synchronized CustomMediaFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomMediaFactory();
        }

        return INSTANCE;
    }

    /**
     * Create the <code>Color</code> for the given color information.
     */
    public Color getColor(int r, int g, int b) {
        return getColor(new RGB(r, g, b));
    }

    /**
     * Create the <code>Color</code> for the given <code>RGB</code>.
     */
    public Color getColor(RGB rgb) {
        assert rgb != null : "rgb!=null";
        Color result = null;

        var key = String.valueOf(rgb.hashCode());

        if (!colorRegistry.hasValueFor(key)) {
            colorRegistry.put(key, rgb);
        }

        result = colorRegistry.get(key);

        return result;
    }

    /**
     * Create the <code>Font</code> for the given information.
     */
    public Font getFont(String name, int height, int style) {
        assert name != null : "name!=null";

        var fd = new FontData(name, height, style);

        var key = String.valueOf(fd.hashCode());
        if (!fontRegistry.hasValueFor(key)) {
            fontRegistry.put(key, new FontData[] { fd });
        }

        return fontRegistry.get(key);
    }

    /**
     * Create the <code>Font</code> for the given <code>FontData</code>.
     */
    public Font getFont(FontData[] fontData) {
        var f = fontData[0];
        return getFont(f.getName(), f.getHeight(), f.getStyle());
    }

    /**
     * Create the <code>Font</code> for the given <code>FontData</code> and the given style code.
     */
    public Font getFont(FontData[] fontData, int style) {
        var f = fontData[0];
        var font = getFont(f.getName(), f.getHeight(), style);
        return font;
    }

    /**
     * Create the <code>Font</code> for the given <code>FontData</code> and the given style code.
     */
    public Font getFont(FontData fontData) {
        return getFont(fontData.getName(), fontData.getHeight(), fontData.getStyle());
    }

    /**
     * Return the system's default font.
     *
     * @param style
     *            additional styles, e.g. SWT.Bold
     * @return The system's default font.
     */
    public Font getDefaultFont(int style) {
        // FIXME Die default Schriftart bzw. Schriftgr��e h�ngt vom
        // Betriebssystem ab
        return getFont("Arial", 10, style);
    }

    /**
     * Load the <code>Image</code> from the given path in the given plugin.
     *
     * @param pluginId
     *            The id of the plugin that contains the requested image.
     * @param relativePath
     *            The resource path of the requested image.
     * @return The <code>Image</code> from the given path in the given plugin.
     */
    public Image getImageFromPlugin(String pluginId, String relativePath) {
        var key = pluginId + "." + relativePath;

        // does image exist
        if (imageRegistry.get(key) == null) {
            var descr = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, relativePath);
            imageRegistry.put(key, descr);
        }

        return imageRegistry.get(key);
    }

    /**
     * Load the <code>Image</code> from the given path in the given plugin. Usually, this is the image found via the the
     * given plug-in relative path. But this implementation also supports a hack for testing: If no plugin is running,
     * because for example this is an SWT-only test, the path is used as is, i.e. relative to the current directory.
     *
     * @param plugin
     *            The plugin that contains the requested image.
     * @param pluginId
     *            The id of the plugin.
     * @param relativePath
     *            The image's relative path to the root of the plugin.
     * @return The <code>Image</code> from the given path in the given plugin.
     */
    public Image getImageFromPlugin(Plugin plugin, String pluginId, String relativePath) {
        var key = pluginId + "." + relativePath;
        // does image exist
        if (imageRegistry.get(key) == null) {
            if (plugin != null) {
                var descr = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, relativePath);
                imageRegistry.put(key, descr);
            } else {
                var display = Display.getCurrent();
                var img = new Image(display, relativePath);
                imageRegistry.put(key, ImageDescriptor.createFromImage(img));
            }
        }

        return imageRegistry.get(key);
    }

    /**
     * Load the <code>ImageDescriptor</code> from the given path in the given plugin.
     *
     * @param pluginId
     *            The id of the plugin that contains the requested image.
     * @param relativePath
     *            The resource path of the requested image.
     * @return The <code>ImageDescriptor</code> from the given path in the given plugin.
     */
    public ImageDescriptor getImageDescriptorFromPlugin(String pluginId, String relativePath) {
        var key = pluginId + "." + relativePath;

        // does image exist
        if (imageRegistry.get(key) == null) {
            var descr = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, relativePath);
            imageRegistry.put(key, descr);
        }

        return imageRegistry.getDescriptor(key);
    }
}
