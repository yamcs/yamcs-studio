/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

/**
 * A collector can be written from one thread and read from another and provides
 * the point where two subsystems and their rate can be decoupled.
 *
 * @param <I> the type written in the collector
 * @param <O> the type read from the collector
 * @author carcassi
 */
public interface Collector<I, O> extends WriteFunction<I>, ReadFunction<O> {

    /**
     * Task to run to notify of the presence of a new value in the collector.
     *
     * @param notification callback for the notification
     */
    public void setChangeNotification(Runnable notification);

}
