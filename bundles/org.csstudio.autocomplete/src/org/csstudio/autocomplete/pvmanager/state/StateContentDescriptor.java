package org.csstudio.autocomplete.pvmanager.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.csstudio.autocomplete.parser.ContentDescriptor;

public class StateContentDescriptor extends ContentDescriptor {

    private static Map<String, String> functions = new TreeMap<String, String>();
    static {
        functions.put("yamcs.host", "Hostname used for connecting to Yamcs");
        functions.put("yamcs.instance", "Connected Yamcs instance");
        functions.put("yamcs.processor", "Connected Yamcs processor");
        functions.put("yamcs.serverId", "Server ID of the connected Yamcs Server");
        functions.put("yamcs.username", "Username at the connected Yamcs Server");
        functions.put("yamcs.version", "Version number of the connected Yamcs server");
        functions = Collections.unmodifiableMap(functions);
    }

    public static Collection<String> listFunctions() {
        return functions.keySet();
    }

    public static String getDescription(String function) {
        return functions.get(function);
    }

}
