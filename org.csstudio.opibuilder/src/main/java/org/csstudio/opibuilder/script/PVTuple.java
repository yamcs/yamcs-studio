/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

/**
 * The data structure which include the pvName and trigger flag
 */
public class PVTuple {
    public String pvName;
    public boolean trigger;

    public PVTuple(String pvName, boolean trigger) {
        this.pvName = pvName;
        this.trigger = trigger;
    }

    public PVTuple getCopy() {
        return new PVTuple(pvName, trigger);
    }
}
