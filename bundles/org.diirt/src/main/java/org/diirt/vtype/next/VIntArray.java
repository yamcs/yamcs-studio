/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import org.diirt.util.array.ListInt;

/**
 * Scalar int array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VIntArray extends VNumberArray {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListInt getData();
}
