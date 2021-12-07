/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVBoolean;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import org.yamcs.studio.data.vtype.VBoolean;

/**
 * Function to simulate a boolean signal that turns on and off.
 */
public class Flipflop extends SimFunction<VBoolean> {

    private boolean value = true;

    /**
     * Creates a flipflop that changes every 500 ms.
     */
    public Flipflop() {
        this(0.5);
    }

    /**
     * Creates a signal that turns on and off every interval.
     *
     * @param interval
     *            interval between samples in seconds
     */
    public Flipflop(Double interval) {
        super(interval);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
    }

    @Override
    VBoolean nextValue() {
        value = !value;
        return newVBoolean(value, alarmNone(), timeNow());
    }
}
