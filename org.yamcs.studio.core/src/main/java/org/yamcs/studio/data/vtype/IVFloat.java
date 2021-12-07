/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

/**
 * Immutable VFloat implementation.
 */
public class IVFloat extends IVNumeric implements VFloat {

    private final Float value;

    IVFloat(Float value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
