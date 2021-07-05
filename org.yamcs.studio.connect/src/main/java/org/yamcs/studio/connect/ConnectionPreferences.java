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

    private static final String KEY_LAST_USED_CONNECTION = "lastUsedConnection";
    private static final String KEY_CONNECTIONS = "confs";

    public static String getLastUsedConnection() {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String id = prefs.get(KEY_LAST_USED_CONNECTION, null);
        if (id != null && !id.isEmpty()) {
            return id;
        } else {
            return null;
        }
    }

    public static void setLastUsedConnection(String id) {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        prefs.remove("lastUsedConf"); // TEMP (clean-up legacy entry)

        if (id == null) {
            prefs.remove(KEY_LAST_USED_CONNECTION);
        } else {
            prefs.put(KEY_LAST_USED_CONNECTION, id);
        }
    }

    public static List<YamcsConfiguration> getConnections() {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String json = prefs.get(KEY_CONNECTIONS, null);
        if (json != null) {
            Type type = new TypeToken<List<YamcsConfiguration>>() {
            }.getType();
            List<YamcsConfiguration> confs = new Gson().fromJson(json, type);
            for (YamcsConfiguration conf : confs) {
                conf.init();
            }
            return confs;
        } else {
            return Collections.emptyList();
        }
    }

    public static void setConnections(List<YamcsConfiguration> confs) {
        Preferences prefs = Preferences.userNodeForPackage(Stub.class);
        String json = new Gson().toJson(confs);
        prefs.put(KEY_CONNECTIONS, json);
    }
}
