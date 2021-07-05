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
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class OPIBuilderPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.opibuilder";
    public static final String EXTPOINT_WIDGET = PLUGIN_ID + ".widget";
    public static final String EXTPOINT_FEEDBACK_FACTORY = PLUGIN_ID + ".graphicalFeedbackFactory";

    private static final Logger log = Logger.getLogger(PLUGIN_ID);

    private static OPIBuilderPlugin plugin;

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

        getPreferenceStore().addPropertyChangeListener(event -> {
            if (event.getProperty().equals(PreferencesHelper.COLORS)) {
                MediaService.getInstance().reloadColors();
            } else if (event.getProperty().equals(PreferencesHelper.FONTS)) {
                MediaService.getInstance().reloadFonts();
            } else if (event.getProperty().equals(PreferencesHelper.OPI_GUI_REFRESH_CYCLE)) {
                GUIRefreshThread.getInstance(true).reLoadGUIRefreshCycle();
            } else if (event.getProperty().equals(PreferencesHelper.SCHEMA_OPI)) {
                SchemaService.getInstance().reload();
            }
        });

        // Reload the schema if the change file is somehow related to the active schema
        ResourcesPlugin.getWorkspace().addResourceChangeListener(event -> {
            IPath schemaPath = PreferencesHelper.getSchemaOPIPath();
            if (schemaPath != null) {
                IResourceDelta delta = event.getDelta();
                if (delta != null) {
                    List<IPath> allPaths = findAllDeltaPaths(delta);
                    if (allPaths.contains(schemaPath)) {
                        SchemaService.getInstance().reload();
                    }
                }
            }
        });
    }

    private List<IPath> findAllDeltaPaths(IResourceDelta delta) {
        List<IPath> combined = new ArrayList<>();
        combined.add(delta.getFullPath());
        for (IResourceDelta child : delta.getAffectedChildren()) {
            combined.addAll(findAllDeltaPaths(child));
        }
        return combined;
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
    }

    public static Logger getLogger() {
        return log;
    }
}
