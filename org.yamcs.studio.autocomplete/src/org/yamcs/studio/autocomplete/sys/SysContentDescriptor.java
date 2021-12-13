/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sys;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.yamcs.studio.autocomplete.parser.ContentDescriptor;

public class SysContentDescriptor extends ContentDescriptor {

    private static Map<String, String> functions = new TreeMap<String, String>();
    static {
        functions.put("time", "Local date and time");
        functions.put("free_mb", "Free Java VM memory in MB");
        functions.put("used_mb", "Used Java VM memory in MB");
        functions.put("max_mb", "Maximum available Java VM memory in MB");
        functions.put("user", "User Name");
        functions.put("host_name", "Host name");
        functions.put("qualified_host_name", "Full Host Name");
        functions.put("system", "Any system property, e.g. \"sys://system.user.name\"");
        functions = Collections.unmodifiableMap(functions);
    }

    public static Collection<String> listFunctions() {
        return functions.keySet();
    }

    public static String getDescription(String function) {
        return functions.get(function);
    }
}
