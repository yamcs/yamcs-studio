/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype;

import org.diirt.util.array.ListFloat;

/**
 * Float array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public interface VFloatArray extends VNumberArray, VType {

    @Override
    ListFloat getData();
}
