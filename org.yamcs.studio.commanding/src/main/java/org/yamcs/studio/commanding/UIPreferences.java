/*******************************************************************************
 * Copyright (c) 2025 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding;

public class UIPreferences {

    public final static String STACK_ENTRY_SPLIT = "stack_entry_split";

    /**
     * Converts a persisted String separated with commas to an integer array
     *
     * @param value
     *            the String value
     * @param cnt
     *            number of entries in the returned array
     * @return the preference values for the array, or null if the string cannot be parsed or doesn't have {@code cnt}
     *         elements, or any value is <= 0.
     */
    public static int[] stringToIntArray(String value, int cnt) {
        if (value == null) {
            return null;
        }
        var values = value.split(",");
        if (values.length != cnt) {
            return null;
        }
        var r = new int[cnt];
        for (var i = 0; i < values.length; i++) {
            try {
                var val = Integer.parseInt(values[i].trim());
                if (val <= 0) {
                    return null;
                }
                r[i] = val;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return r;
    }

    /**
     * Converts an integer array into a String separated by commas
     */
    public static String intArrayToString(int[] data) {
        var buf = new StringBuilder();
        for (var i = 0; i < data.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(data[i]);
        }
        return buf.toString();
    }
}
