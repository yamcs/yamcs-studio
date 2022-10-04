/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.script;

import java.util.LinkedHashMap;
import java.util.List;

import org.csstudio.opibuilder.util.MacrosInput;

/**
 * Utility class to facilitate Javascript programming for data operation. The basic data type such as int, double,
 * boolean and string are exchangeable between JavaScript and Java, which means these types of JavaScript variables can
 * be directly used as parameters of Java methods, but <b>array</b> is not exchangeable between JavaScript and Java.
 * This utility class provides methods to create Java array or convert JavaScript array to Java array.
 */
public class DataUtil {

    /**
     * Create a new int array with given size.
     *
     * @param size
     *            the size of the array
     * @return an int array with given size.
     */
    public final static int[] createIntArray(int size) {
        var result = new int[size];
        return result;
    }

    /**
     * Create a new double array with given size.
     *
     * @param size
     *            the size of the array
     * @return a double array with given size.
     */
    public final static double[] createDoubleArray(int size) {
        var result = new double[size];
        return result;
    }

    /**
     * Convert JavaScript array to Java int array.
     *
     * @param array
     *            JavaScript array
     * @return java int array.
     */
    public final static int[] toJavaIntArray(Object array) {
        var objArray = ((List<?>) array).toArray();
        var result = new int[objArray.length];
        for (var i = 0; i < objArray.length; i++) {
            if (objArray[i] instanceof Number) {
                result[i] = ((Number) objArray[i]).intValue();
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Convert JavaScript array to Java double array.
     *
     * @param array
     *            JavaScript array
     * @return java array.
     */
    public final static double[] toJavaDoubleArray(Object array) {
        var objArray = ((List<?>) array).toArray();
        var result = new double[objArray.length];
        for (var i = 0; i < objArray.length; i++) {
            if (objArray[i] instanceof Number) {
                result[i] = ((Number) objArray[i]).doubleValue();
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Create a MacrosInput, which can be used as the macros input for a container widget or display. New macro can be
     * added or replaced by <code>MacrosInput.put(String macroName, String macroValue);</code>
     *
     * @param include_parent_macros
     *            If parent macros should be included.
     * @return a new created MacrosInput.
     */
    public final static MacrosInput createMacrosInput(boolean include_parent_macros) {
        return new MacrosInput(new LinkedHashMap<String, String>(), include_parent_macros);
    }
}
