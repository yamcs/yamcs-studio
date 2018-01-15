/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype;

import org.diirt.util.array.ListByte;

/**
 * Byte array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public interface VByteArray extends VNumberArray, VType {

    /**
     * {@inheritDoc }
     * @return the data
     */
    @Override
    ListByte getData();
}
