/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.ui.preferences;

import org.eclipse.core.runtime.Platform;
import org.yamcs.studio.autocomplete.AutoCompletePlugin;

public class Preferences {

    public static final String HISTORY_SIZE = "history_size";

    @SuppressWarnings("unused")
    private static String getString(String setting) {
        return getString(setting, null);
    }

    private static String getString(String setting, String default_value) {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return default_value;
        }
        return service.getString(AutoCompletePlugin.PLUGIN_ID, setting, default_value, null);
    }

    public static int getHistorySize() {
        var service = Platform.getPreferencesService();
        if (service == null) {
            return 100; // default
        }
        return service.getInt(AutoCompletePlugin.PLUGIN_ID, HISTORY_SIZE, 100, null);
    }
}
