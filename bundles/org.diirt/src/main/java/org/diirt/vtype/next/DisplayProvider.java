/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * A value that provides display information.
 *
 * @author carcassi
 */
interface DisplayProvider {

    /**
     * The display associated with this value.
     *
     * @return the display; not null
     */
    Display getDisplay();
}
