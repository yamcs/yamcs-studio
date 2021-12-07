/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.connect;

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
        var prefs = Preferences.userNodeForPackage(Stub.class);
        var id = prefs.get(KEY_LAST_USED_CONNECTION, null);
        if (id != null && !id.isEmpty()) {
            return id;
        } else {
            return null;
        }
    }

    public static void setLastUsedConnection(String id) {
        var prefs = Preferences.userNodeForPackage(Stub.class);
        prefs.remove("lastUsedConf"); // TEMP (clean-up legacy entry)

        if (id == null) {
            prefs.remove(KEY_LAST_USED_CONNECTION);
        } else {
            prefs.put(KEY_LAST_USED_CONNECTION, id);
        }
    }

    public static List<YamcsConfiguration> getConnections() {
        var prefs = Preferences.userNodeForPackage(Stub.class);
        var json = prefs.get(KEY_CONNECTIONS, null);
        if (json != null) {
            var type = new TypeToken<List<YamcsConfiguration>>() {
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
        var prefs = Preferences.userNodeForPackage(Stub.class);
        var json = new Gson().toJson(confs);
        prefs.put(KEY_CONNECTIONS, json);
    }
}
