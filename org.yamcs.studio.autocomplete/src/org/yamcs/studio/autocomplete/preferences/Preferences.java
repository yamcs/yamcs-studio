/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.preferences;

import org.eclipse.core.runtime.Platform;
import org.yamcs.studio.autocomplete.AutoCompletePlugin;

/**
 * Read preferences
 * <p>
 * See preferences.ini for explanation of supported preferences.
 */
public class Preferences {

    final public static String DEFAULT_MAX_RESULTS = "default_max_results";
    final public static String PROVIDERS = "providers";
    final public static String SEPARATORS = "separators";
    final public static String MAX_TOP_RESULTS = "max_top_results";

    /**
     * @param setting
     *            Preference identifier
     * @return String from preference system, or <code>null</code>
     */
    private static String getString(String setting) {
        return getString(setting, null);
    }

    /**
     * @param setting
     *            Preference identifier
     * @param default_value
     *            Default value when preferences unavailable
     * @return String from preference system, or <code>null</code>
     */
    private static String getString(String setting, String default_value) {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return default_value;
        }
        return service.getString(AutoCompletePlugin.PLUGIN_ID, setting, default_value, null);
    }

    /** @return default list max size */
    public static int getDefaultMaxResults() {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return 100; // default
        }
        return service.getInt(AutoCompletePlugin.PLUGIN_ID, DEFAULT_MAX_RESULTS, 10, null);
    }

    /** @return providers settings */
    public static String getProviders(String type) {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return null; // default
        }
        return getString(PROVIDERS + "." + type);
    }

    /** @return top result separators */
    public static String getSeparators() {
        return getString(SEPARATORS);
    }

    /** @return max top results */
    public static int getMaxTopResults() {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return 0; // default
        }
        return service.getInt(AutoCompletePlugin.PLUGIN_ID, MAX_TOP_RESULTS, 0, null);
    }

}
