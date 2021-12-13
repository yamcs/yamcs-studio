/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.java.string.StringSplitter;

/**
 * A table of macros that's initialized from a string or a hash map, keeping the macro names and values in a hash
 */
public class MacroTable implements IMacroTableProvider {
    /** Map of macro names to values */
    final private Map<String, String> macros;

    /**
     * Initialize
     * 
     * @param macros
     *            Map with macro name/value entries
     */
    public MacroTable(Map<String, String> macros) {
        if (macros == null) {
            this.macros = new HashMap<>(0);
        } else {
            this.macros = macros;
        }
    }

    /**
     * Initialize
     * 
     * @param names_and_values
     *            String of the form "macro=value, macro=value"
     * @throws Exception
     *             on malformed input
     */
    public MacroTable(String names_and_values) throws Exception {
        macros = new HashMap<>();
        var pairs = StringSplitter.splitIgnoreInQuotes(names_and_values, ',', true);
        for (var pair : pairs) {
            var name_value = StringSplitter.splitIgnoreInQuotes(pair, '=', true);
            if (name_value.length != 2) {
                throw new Exception("Input '" + pair + "' does not match 'name=value'");
            }
            macros.put(name_value[0], name_value[1]);
        }
    }

    @Override
    public String getMacroValue(String name) {
        return macros.get(name);
    }

    /** @return String representation for debugging */
    @Override
    public String toString() {
        var buf = new StringBuilder();
        var names = macros.keySet().toArray(new String[macros.size()]);
        for (var name : names) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(name + "=\"" + getMacroValue(name) + "\"");
        }
        return buf.toString();
    }
}
