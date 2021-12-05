/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helper to handle wildcards.
 */
public class AutoCompleteHelper {

    private static String SINGLE_REPLACE_CHAR = "\\?";
    private static String MULTI_REPLACE_CHAR = "\\*";

    /**
     * Quote the name and return a pattern which handles only wildcards.
     */
    public static Pattern convertToPattern(String name) {
        String regex = Pattern.quote(name);
        regex = regex.replaceAll(MULTI_REPLACE_CHAR, "\\\\E.*\\\\Q");
        regex = regex.replaceAll(SINGLE_REPLACE_CHAR, "\\\\E.\\\\Q");
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    public static String convertToSQL(String name) {
        String sql = name.replaceAll(MULTI_REPLACE_CHAR, "%");
        sql = sql.replaceAll(SINGLE_REPLACE_CHAR, "_");
        sql = sql.replaceAll("'", "''"); // prevent SQL injection
        return sql;
    }

    /**
     * Remove all begining/ending wildcards.
     */
    public static String trimWildcards(String name) {
        String cleaned = name.replaceAll("^[\\*\\?]+", "");
        cleaned = cleaned.replaceAll("[\\*\\?]+$", "");
        return cleaned;
    }

    public static Set<String> retrievePVManagerSupported() {
        Set<String> items = new HashSet<>();
        try {
            Class<?> clazz = Class.forName("org.csstudio.utility.pvmanager.ConfigurationHelper");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) clazz.getMethod("configuredDataSources")
                    .invoke(null);
            items.addAll(parameters.keySet());
            AutoCompletePlugin.getLogger().config("Loading PVManager supported types: " + items);
            return items;
        } catch (Exception ex) {
            AutoCompletePlugin.getLogger().config("PVManager not found: " + ex.getMessage());
            return Collections.emptySet();
        }
    }

}
