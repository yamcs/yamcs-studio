/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

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
        var regex = Pattern.quote(name);
        regex = regex.replaceAll(MULTI_REPLACE_CHAR, "\\\\E.*\\\\Q");
        regex = regex.replaceAll(SINGLE_REPLACE_CHAR, "\\\\E.\\\\Q");
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    /**
     * Remove all begining/ending wildcards.
     */
    public static String trimWildcards(String name) {
        var cleaned = name.replaceAll("^[\\*\\?]+", "");
        cleaned = cleaned.replaceAll("[\\*\\?]+$", "");
        return cleaned;
    }
}
