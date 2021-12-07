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
 * Immutable VInt implementation.
 */
public class IVInt extends IVNumeric implements VInt {

    private final Integer value;

    public IVInt(Integer value) {
        this(value, ValueFactory.alarmNone(), ValueFactory.timeNow(), ValueFactory.displayNone());
    }

    public IVInt(Integer value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
