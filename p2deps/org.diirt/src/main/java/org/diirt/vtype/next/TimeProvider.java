/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * A value that provides time information.
 *
 * @author carcassi
 */
interface TimeProvider {

    /**
     * The time associated with this value.
     *
     * @return the time; not null
     */
    Time getTime();
}
