/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype;

import java.util.List;
import org.diirt.util.array.ListInt;
import org.diirt.util.array.ListLong;

/**
 *
 * @author carcassi
 */
class IVLongArray extends IVNumberArray implements VLongArray {

    private final ListLong data;

    public IVLongArray(ListLong data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay,
            Alarm alarm, Time time, Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListLong getData() {
        return data;
    }

}
