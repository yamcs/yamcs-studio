/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.preferences;

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
import org.osgi.service.prefs.BackingStoreException;

/**
 * This is the central place for preference related operations.
 * 
 * @author Xihui Chen
 *
 */
public class PreferencesHelper {

    public static final String COLOR_FILE = "color_file";
    public static final String FONT_FILE = "font_file";
    public static final String RUN_MACROS = "macros";
    public static final String AUTOSAVE = "auto_save";
    public static final String OPI_GUI_REFRESH_CYCLE = "opi_gui_refresh_cycle";
    public static final String NO_EDIT = "no_edit";
    public static final String DISABLE_ADVANCED_GRAPHICS = "disable_advanced_graphics";
    public static final String SCHEMA_OPI = "schema_opi";
    public static final String PYTHON_PATH = "python_path";
    public static final String SHOW_COMPACT_MODE_DIALOG = "show_compact_mode_dialog";
    public static final String SHOW_FULLSCREEN_DIALOG = "show_fullscreen_dialog";
    public static final String START_WINDOW_IN_COMPACT_MODE = "start_window_in_compact_mode";
    public static final String PULSING_ALARM_MINOR_PERIOD = "pulsing_alarm_minor_period";
    public static final String PULSING_ALARM_MAJOR_PERIOD = "pulsing_alarm_major_period";
    public static final String DEFAULT_TO_CLASSIC_STYLE = "default_to_classic_style";
    public static final String SHOW_OPI_RUNTIME_STACKS = "show_opi_runtime_stacks";
    public static final String FONT_DEFAULT_PIXELS_OR_POINTS = "font_default_pixels_or_points";

    // The widgets that are hidden from palette.
    public static final String HIDDEN_WIDGETS = "hidden_widgets";

    private static final char ROW_SEPARATOR = '|';

    public static final String ABOUT_SHOW_LINKS = "about_show_links";
    public static final String PIXELS = "pixels";
    public static final String POINTS = "points";

    /**
     * @param preferenceName
     *            Preference identifier
     * @return String from preference system, or <code>null</code>
     */
    protected static String getString(final String preferenceName) {
        return getString(preferenceName, null);
    }

    /**
     * @param preferenceName
     *            Preference identifier
     * @param defaultValue
     *            default value
     * @return String from preference system, or <code>defaultValue</code> if null
     */
    protected static String getString(final String preferenceName, final String defaultValue) {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getString(OPIBuilderPlugin.PLUGIN_ID, preferenceName, defaultValue, null);
    }

    /**
     * Get the color file path from preference store.
     * 
     * @return the color file path. null if not specified.
     */
    public static IPath getColorFilePath() {
        String colorFilePath = getString(COLOR_FILE);
        if (colorFilePath == null || colorFilePath.trim().isEmpty()) {
            return null;
        }
        return getAbsolutePathOnRepo(colorFilePath);
    }

    /**
     * Get the font file path from preference store.
     * 
     * @return the color file path. null if not specified.
     */
    public static IPath getFontFilePath() {
        String fontFilePath = getString(FONT_FILE);
        if (fontFilePath == null || fontFilePath.trim().isEmpty()) {
            return null;
        }
        return getAbsolutePathOnRepo(fontFilePath);
    }

    /**
     * Get the schema OPI path from preference store.
     * 
     * @return the schema OPI path. null if not specified.
     */
    public static IPath getSchemaOPIPath() {
        String schemaOPIPath = getString(SCHEMA_OPI);
        if (schemaOPIPath == null || schemaOPIPath.trim().isEmpty()) {
            return null;
        }
        return ResourceUtil.getPathFromString(schemaOPIPath);
    }

    public static boolean isAutoSaveBeforeRunning() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, AUTOSAVE, false, null);
    }

    public static boolean isNoEdit() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, NO_EDIT, false, null);
    }

    public static boolean isDefaultStyleClassic() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, DEFAULT_TO_CLASSIC_STYLE, true, null);
    }

    public static boolean showOpiRuntimeStacks() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, SHOW_OPI_RUNTIME_STACKS, false, null);
    }

    public static boolean isAdvancedGraphicsDisabled() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, DISABLE_ADVANCED_GRAPHICS, false, null);
    }

    public static Integer getGUIRefreshCycle() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, OPI_GUI_REFRESH_CYCLE, 100, null);
    }

    public static Integer getPulsingAlarmMinorPeriod() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, PULSING_ALARM_MINOR_PERIOD, 3000, null);
    }

    public static Integer getPulsingAlarmMajorPeriod() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, PULSING_ALARM_MAJOR_PERIOD, 1500, null);
    }

    public static boolean isDefaultFontSizeInPixels() {
        return getString(FONT_DEFAULT_PIXELS_OR_POINTS, POINTS).equals(PIXELS);
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
     * @return typeId of widgets that should be hidden from palette.
     * @throws Exception
     */
    public static String[] getHiddenWidgets() {
        String rawString = getString(HIDDEN_WIDGETS);

        if (rawString == null || rawString.trim().isEmpty()) {
            rawString = "";
        } else {
            rawString = "|" + rawString;
        }

        try {
            return StringSplitter.splitIgnoreInQuotes(rawString, ROW_SEPARATOR, true);
        } catch (Exception e) {
            ErrorHandlerUtil.handleError("Failed to parse hidden_widgets preference", e);
            return null;
        }
    }

    /**
     * @return the python path, null if this preference is not setted.
     * @throws Exception
     */
    public static Optional<String> getPythonPath() throws Exception {
        final String rawString = getString(PYTHON_PATH);
        if (rawString == null || rawString.isEmpty()) {
            return Optional.empty();
        }
        final String[] rawPaths = StringSplitter.splitIgnoreInQuotes(rawString, ROW_SEPARATOR, true);
        final StringBuilder sb = new StringBuilder();
        for (String rawPath : rawPaths) {
            if (sb.length() > 0) {
                sb.append(System.getProperty("path.separator"));
            }
            final IPath path = new Path(rawPath);
            final IPath location = ResourceUtil.workspacePathToSysPath(path);
            if (location != null) {
                sb.append(location.toOSString());
            } else {
                sb.append(rawPath);
            }
        }
        return Optional.of(sb.toString());
    }

    public static boolean isShowCompactModeDialog() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, SHOW_COMPACT_MODE_DIALOG, true, null);
    }

    public static void setShowCompactModeDialog(boolean show) {
        putBoolean(SHOW_COMPACT_MODE_DIALOG, show);
    }

    public static boolean isShowFullScreenDialog() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, SHOW_FULLSCREEN_DIALOG, true, null);
    }

    public static void setShowFullScreenDialog(boolean show) {
        putBoolean(SHOW_FULLSCREEN_DIALOG, show);
    }

    public static boolean isStartWindowInCompactMode() {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, START_WINDOW_IN_COMPACT_MODE, false, null);
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
     * 
     * @param pathString
     * @return
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
