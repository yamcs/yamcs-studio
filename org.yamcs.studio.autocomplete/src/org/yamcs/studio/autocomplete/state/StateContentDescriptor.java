/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.yamcs.studio.autocomplete.parser.ContentDescriptor;

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
