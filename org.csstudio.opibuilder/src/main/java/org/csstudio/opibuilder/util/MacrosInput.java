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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.csstudio.java.string.StringSplitter;
import org.csstudio.opibuilder.properties.MacrosProperty;

/**
 * The value type definition for {@link MacrosProperty}, which describes the input for a Macros Property. TODO Combine
 * with MacroTable TODO Hide the actual map implementation TODO Why does the order of macros matter? For environment
 * vars, the order doesn't matter. Can still replace macros recursively as in $($(M))
 */
public class MacrosInput {

    private LinkedHashMap<String, String> macrosMap;

    private boolean include_parent_macros;

    private static final char ITEM_SEPARATOR = ',';
    private static final char MACRO_SEPARATOR = '=';
    private static final char QUOTE = '\"';

    public MacrosInput(LinkedHashMap<String, String> macros, boolean include_parent_macros) {
        macrosMap = macros;
        this.include_parent_macros = include_parent_macros;
    }

    /**
     * @return the macrosMap
     */
    public final LinkedHashMap<String, String> getMacrosMap() {
        return macrosMap;
    }

    /**
     * @param macrosMap
     *            the macrosMap to set
     */
    public void setMacrosMap(LinkedHashMap<String, String> macrosMap) {
        this.macrosMap = macrosMap;
    }

    /**
     * Add or replace a macro.
     *
     * @param macroName
     * @param macroValue
     */
    public void put(String macroName, String macroValue) {
        macrosMap.put(macroName, macroValue);
    }

    /**
     * @return the include_parent_macros
     */
    public final boolean isInclude_parent_macros() {
        return include_parent_macros;
    }

    /**
     * @param include_parent_macros
     *            the include_parent_macros to set
     */
    public void setInclude_parent_macros(boolean include_parent_macros) {
        this.include_parent_macros = include_parent_macros;
    }

    public MacrosInput getCopy() {
        var result = new MacrosInput(new LinkedHashMap<String, String>(), include_parent_macros);
        result.getMacrosMap().putAll(macrosMap);
        return result;
    }

    @Override
    public String toString() {
        return (include_parent_macros ? "{" + "Parent Macros" + "} " : "") + macrosMap.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(include_parent_macros, macrosMap, macrosMap.keySet().toArray());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MacrosInput) {
            var input = (MacrosInput) obj;
            if (include_parent_macros != input.isInclude_parent_macros()) {
                return false;
            }
            if (!macrosMap.equals(input.getMacrosMap())) {
                return false;
            }
            List<Object> keyList = Arrays.asList(macrosMap.keySet().toArray());
            List<Object> inputKeyList = Arrays.asList(input.getMacrosMap().keySet().toArray());
            if (keyList.equals(inputKeyList)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @return a String with format like this: "true", "macro1 = hello", "macro2 = hello2"
     */
    public String toPersistenceString() {
        var result = new StringBuilder();
        result.append(QUOTE + Boolean.toString(include_parent_macros) + QUOTE);
        for (var key : macrosMap.keySet()) {
            result.append(ITEM_SEPARATOR + "" + QUOTE + key + MACRO_SEPARATOR + macrosMap.get(key) + QUOTE);
        }
        return result.toString();
    }

    // TODO Offer a parser just for "macro1 = hello", "macro2 = hello2" without the inital "true", "false
    /**
     * Parse MacrosInput from persistence string.
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static MacrosInput recoverFromString(String s) throws Exception {
        var items = StringSplitter.splitIgnoreInQuotes(s, ITEM_SEPARATOR, true);
        var macrosInput = new MacrosInput(new LinkedHashMap<String, String>(), true);
        for (var i = 0; i < items.length; i++) {
            if (i == 0) {
                macrosInput.setInclude_parent_macros(Boolean.valueOf(items[i]));
            } else {
                var macro = StringSplitter.splitIgnoreInQuotes(items[i], MACRO_SEPARATOR, true);
                if (macro.length == 2) {
                    macrosInput.getMacrosMap().put(macro[0], macro[1]);
                } else if (macro.length == 1) {
                    macrosInput.getMacrosMap().put(macro[0], "");
                }
            }
        }
        return macrosInput;
    }
}
