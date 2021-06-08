/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.csstudio.java.string.StringSplitter;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This is the central place for preference related operations.
 * 
 * @author Xihui Chen
 *
 */
public class PreferencesHelper {

    public static final String COLORS = "colors.list";
    public static final String FONTS = "fonts.list";
    public static final String RUN_MACROS = "macros";
    public static final String AUTOSAVE = "auto_save";
    public static final String OPI_GUI_REFRESH_CYCLE = "opi_gui_refresh_cycle";
    public static final String PROBE_OPI = "probe_opi";
    public static final String SCHEMA_OPI = "schema_opi";
    public static final String PYTHON_PATH = "python_path";
    public static final String SHOW_FULLSCREEN_DIALOG = "show_fullscreen_dialog";
    public static final String PULSING_ALARM_MINOR_PERIOD = "pulsing_alarm_minor_period";
    public static final String PULSING_ALARM_MAJOR_PERIOD = "pulsing_alarm_major_period";

    // The widgets that are hidden from palette.
    public static final String HIDDEN_WIDGETS = "hidden_widgets";

    private static final char ROW_SEPARATOR = '|';

    protected static String getString(String preferenceName) {
        return getString(preferenceName, null);
    }

    protected static String getString(String preferenceName, String defaultValue) {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getString(OPIBuilderPlugin.PLUGIN_ID, preferenceName, defaultValue, null);
    }

    public static IPath getProbeOPIPath() {
        String probeOPIPath = getString(PROBE_OPI);
        if (probeOPIPath == null || probeOPIPath.trim().isEmpty()) {
            return null;
        }
        return ResourceUtil.getPathFromString(probeOPIPath);
    }

    public static IPath getSchemaOPIPath() {
        String schemaOPIPath = getString(SCHEMA_OPI);
        if (schemaOPIPath == null || schemaOPIPath.trim().isEmpty()) {
            return null;
        }
        return ResourceUtil.getPathFromString(schemaOPIPath);
    }

    public static void setSchemaOPIPath(IPath path) {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(OPIBuilderPlugin.PLUGIN_ID);
        if (path == null) {
            prefs.put(SCHEMA_OPI, IPreferenceStore.STRING_DEFAULT_DEFAULT);
        } else {
            prefs.put(SCHEMA_OPI, path.toString());
        }
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, "Failed to store preferences.", e);
        }
    }

    public static boolean isAutoSaveBeforeRunning() {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, AUTOSAVE, false, null);
    }

    public static Integer getGUIRefreshCycle() {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, OPI_GUI_REFRESH_CYCLE, 100, null);
    }

    public static Integer getPulsingAlarmMinorPeriod() {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, PULSING_ALARM_MINOR_PERIOD, 3000, null);
    }

    public static Integer getPulsingAlarmMajorPeriod() {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, PULSING_ALARM_MAJOR_PERIOD, 1500, null);
    }

    /**
     * Get the macros map from preference store.
     * 
     * @return the macros map. null if failed to get macros from preference store.
     */
    public static LinkedHashMap<String, String> getMacros() {
        if (getString(RUN_MACROS) != null) {
            try {
                LinkedHashMap<String, String> macros = new LinkedHashMap<>();
                List<String[]> items = StringTableFieldEditor.decodeStringTable(getString(RUN_MACROS));
                for (String[] item : items) {
                    if (item.length == 2) {
                        macros.put(item[0], item[1]);
                    }
                }
                return macros;

            } catch (Exception e) {
                OPIBuilderPlugin.getLogger().log(Level.WARNING, "Macro error", e);
                return new LinkedHashMap<>();
            }
        }
        return new LinkedHashMap<>();
    }

    /**
     * @return typeId of widgets that should be hidden from the palette.
     */
    public static List<String> getHiddenWidgets() {
        String rawString = getString(HIDDEN_WIDGETS);

        if (rawString == null || rawString.trim().isEmpty()) {
            rawString = "";
        } else {
            rawString = "|" + rawString;
        }

        // a hack to hide deprecated native widgets
        // FDI: Only remove if the example projects no longer make use of it either.
        rawString = "org.csstudio.opibuilder.widgets.NativeButton|org.csstudio.opibuilder.widgets.NativeText"
                + rawString;

        try {
            String[] parts = StringSplitter.splitIgnoreInQuotes(rawString, ROW_SEPARATOR, true);
            return Arrays.asList(parts);
        } catch (Exception e) {
            ErrorHandlerUtil.handleError("Failed to parse hidden_widgets preference", e);
            return Collections.emptyList();
        }
    }

    public static Optional<String> getPythonPath() throws Exception {
        String rawString = getString(PYTHON_PATH);
        if (rawString == null || rawString.isEmpty()) {
            return Optional.empty();
        }
        String[] rawPaths = StringSplitter.splitIgnoreInQuotes(rawString, ROW_SEPARATOR, true);
        StringBuilder sb = new StringBuilder();
        for (String rawPath : rawPaths) {
            if (sb.length() > 0) {
                sb.append(System.getProperty("path.separator"));
            }
            IPath path = new Path(rawPath);
            IPath location = ResourceUtil.workspacePathToSysPath(path);
            if (location != null) {
                sb.append(location.toOSString());
            } else {
                sb.append(rawPath);
            }
        }
        return Optional.of(sb.toString());
    }

    public static boolean isShowFullScreenDialog() {
        IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, SHOW_FULLSCREEN_DIALOG, true, null);
    }

    public static void setShowFullScreenDialog(boolean show) {
        putBoolean(SHOW_FULLSCREEN_DIALOG, show);
    }

    private static void putBoolean(String name, boolean value) {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(OPIBuilderPlugin.PLUGIN_ID);
        prefs.putBoolean(name, value);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, "Failed to store preferences.", e);
        }
    }

    /**
     * Return the absolute path based on OPI Repository.
     */
    protected static IPath getAbsolutePathOnRepo(String pathString) {
        IPath opiPath = ResourceUtil.getPathFromString(pathString);
        if (opiPath == null) {
            return null;
        }
        if (opiPath.isAbsolute()) {
            return opiPath;
        }
        return opiPath;
    }
}
