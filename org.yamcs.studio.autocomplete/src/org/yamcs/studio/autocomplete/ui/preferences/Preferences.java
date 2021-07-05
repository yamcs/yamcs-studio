package org.yamcs.studio.autocomplete.ui.preferences;

import org.yamcs.studio.autocomplete.AutoCompletePlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class Preferences {

    public static final String HISTORY_SIZE = "history_size";

    @SuppressWarnings("unused")
    private static String getString(String setting) {
        return getString(setting, null);
    }

    private static String getString(String setting, String default_value) {
        IPreferencesService service = Platform.getPreferencesService();
        if (service == null) {
            return default_value;
        }
        return service.getString(AutoCompletePlugin.PLUGIN_ID, setting, default_value,
                null);
    }

    public static int getHistorySize() {
        IPreferencesService service = Platform.getPreferencesService();
        if (service == null) {
            return 100; // default
        }
        return service.getInt(AutoCompletePlugin.PLUGIN_ID, HISTORY_SIZE, 100, null);
    }

}
