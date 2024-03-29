/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.studio.data.vtype.VType;

public abstract class StateData {

    private static Logger log = Logger.getLogger(StateData.class.getName());

    private final Runnable task = () -> {
        try {
            createAndSaveValue();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Data state problem", ex);
        }
    };

    private Set<IPV> pvs = new HashSet<>();
    private VType value;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> taskFuture;

    public StateData(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public boolean isConnected() {
        return !pvs.isEmpty();
    }

    public VType getValue() {
        return value;
    }

    private void createAndSaveValue() {
        var newValue = createValue();
        if (newValue != null) {
            value = newValue;
        }
        pvs.forEach(IPV::notifyValueChange);
    }

    abstract VType createValue();

    void register(IPV pv) {
        pvs.add(pv);
        if (taskFuture == null) {
            taskFuture = executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
        }
        pv.notifyConnectionChange();
        if (value != null) {
            pv.notifyValueChange();
        }
    }

    void unregister(IPV pv) { // Note that we don't reset the value, it can stay around for a new connect
        pvs.remove(pv);
        if (pvs.isEmpty()) {
            taskFuture.cancel(true);
            taskFuture = null;
            pv.notifyConnectionChange();
        }
    }
}
