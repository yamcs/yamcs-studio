package org.yamcs.studio.connect;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import org.yamcs.studio.ui.connections.Stub;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Access point for UI preferences related to yamcs connections
 */
@SuppressWarnings("deprecation")
public class ConnectionPreferences {

    private static final String KEY_LAST_USED_CONFIGURATION = "lastUsedConf";
    private static final String KEY_CONFIGURATIONS = "confs";

    public static YamcsConfiguration getLastUsedConfiguration() {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String confString = prefs.get(KEY_LAST_USED_CONFIGURATION, null);
        if (confString != null) {
            return new Gson().fromJson(confString, YamcsConfiguration.class);
        } else {
            return null;
        }
    }

    public static void setLastUsedConfiguration(YamcsConfiguration conf) {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        prefs.put(KEY_LAST_USED_CONFIGURATION, new Gson().toJson(conf));
    }

    public static List<YamcsConfiguration> getConfigurations() {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String confsString = prefs.get(KEY_CONFIGURATIONS, null);
        if (confsString != null) {
            Type type = new TypeToken<List<YamcsConfiguration>>() {
            }.getType();
            List<YamcsConfiguration> confs = new Gson().fromJson(confsString, type);
            return confs;
        } else {
            return Collections.emptyList();
        }
    }

    public static void setConfigurations(List<YamcsConfiguration> confs) {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String confsString = new Gson().toJson(confs);
        prefs.put(KEY_CONFIGURATIONS, confsString);
    }
}
