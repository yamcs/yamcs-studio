/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype;

import java.util.List;
import org.diirt.util.array.ListInt;

/**
 *
 * @author carcassi
 */
public interface VEnumArray extends Array, Enum, Alarm, Time, VType {
    @Override
    List<String> getData();

    /**
     * Returns the indexes instead of the labels.
     *
     * @return an array of indexes
     */
    ListInt getIndexes();
}
