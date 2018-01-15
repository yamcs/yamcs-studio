/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.util;

import java.util.Collection;

/**
 * Utility class to handle null values.
 *
 * @author carcassi
 */
public class NullUtils {

    /**
     * Checks whether the collection contains a null value.
     *
     * @param args a collection; can't be null
     * @return true if one of the value is null
     */
    public static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
