/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.csstudio.opibuilder.preferences.NamedColor;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.util.GUIRefreshThread;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.util.SchemaService;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Xihui Chen
 *
 */
@SuppressWarnings("deprecation")
public class OPIBuilderPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.opibuilder";

    /**
     * The ID of the widget extension point.
     */
    public static final String EXTPOINT_WIDGET = PLUGIN_ID + ".widget";

    /**
     * The ID of the widget extension point.
     */
    public static final String EXTPOINT_FEEDBACK_FACTORY = PLUGIN_ID + ".graphicalFeedbackFactory";

    public static final String OPI_FILE_EXTENSION = "opi";

    private static final Logger logger = Logger.getLogger(PLUGIN_ID);

    private static OPIBuilderPlugin plugin;

    private IPropertyChangeListener preferenceListener;

    public OPIBuilderPlugin() {
        plugin = this;
    }

    public static OPIBuilderPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        // set this to resolve Xincludes in XMLs
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        ScriptService.getInstance();

        preferenceListener = event -> {
            if (event.getProperty().equals(PreferencesHelper.COLORS)) {
                MediaService.getInstance().reloadColors();
            } else if (event.getProperty().equals(PreferencesHelper.FONTS)) {
                MediaService.getInstance().reloadFonts();
            } else if (event.getProperty().equals(PreferencesHelper.OPI_GUI_REFRESH_CYCLE)) {
                GUIRefreshThread.getInstance(true).reLoadGUIRefreshCycle();
            } else if (event.getProperty().equals(PreferencesHelper.SCHEMA_OPI)) {
                SchemaService.getInstance().reLoad();
            }
        };

        getPluginPreferences().addPropertyChangeListener(preferenceListener);
    }

    public List<NamedColor> loadColors() {
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getString(PreferencesHelper.COLORS);
        return composeColors(joined);
    }

    public List<NamedColor> loadDefaultColors() {
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getDefaultString(PreferencesHelper.COLORS);
        return composeColors(joined);
    }

    public List<NamedColor> composeColors(String joined) {
        if (joined.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
            return new ArrayList<>(0);
        }

        List<NamedColor> colors = new ArrayList<>();
        for (String colorString : joined.split(";")) {
            String[] parts = colorString.split("@");
            RGB rgb = StringConverter.asRGB(parts[1]);
            colors.add(new NamedColor(parts[0], rgb));
        }
        return colors;
    }

    public void storeColors(List<NamedColor> colors) {
        IPreferenceStore store = getPreferenceStore();

        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (NamedColor color : colors) {
            if (!first) {
                buf.append(";"); // Same ENTRY_SEPARATOR as used in jface PreferenceConverter
            }
            first = false;
            buf.append(color.name);
            buf.append("@").append(StringConverter.asString(color.rgb));
        }

        store.setValue(PreferencesHelper.COLORS, buf.toString());
    }

    public List<OPIFont> loadFonts() {
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getString(PreferencesHelper.FONTS);
        return composeFonts(joined);
    }

    public List<OPIFont> loadDefaultFonts() {
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getDefaultString(PreferencesHelper.COLORS);
        return composeFonts(joined);
    }

    public List<OPIFont> composeFonts(String joined) {
        if (joined.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
            return new ArrayList<>(0);
        }

        List<OPIFont> fonts = new ArrayList<>();
        for (String colorString : joined.split(";")) {
            String[] parts = colorString.split("@");
            FontData fontData = StringConverter.asFontData(parts[1]);
            OPIFont font = new OPIFont(parts[0], fontData);
            font.setSizeInPixels(false); // Trying to get rid of pixels (pt is swt default)
            fonts.add(font);
        }
        return fonts;
    }

    public void storeFonts(List<OPIFont> fonts) {
        IPreferenceStore store = getPreferenceStore();

        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (OPIFont font : fonts) {
            if (!first) {
                buf.append(";"); // Same ENTRY_SEPARATOR as used in jface PreferenceConverter
            }
            first = false;
            buf.append(font.getFontMacroName());
            buf.append("@").append(StringConverter.asString(font.getFontData()));
        }

        store.setValue(PreferencesHelper.FONTS, buf.toString());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        getPluginPreferences().removePropertyChangeListener(preferenceListener);
    }

    public static Logger getLogger() {
        return logger;
    }
}
