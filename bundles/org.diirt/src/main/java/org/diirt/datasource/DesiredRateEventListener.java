/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

/**
 * Processes the callback for events at the desired rate.
 *
 * @author carcassi
 */
interface DesiredRateEventListener {

    /**
     * New event to be processed.
     */
    void desiredRateEvent(DesiredRateEvent event);
}
