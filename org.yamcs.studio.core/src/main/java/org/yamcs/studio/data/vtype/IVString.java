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

public class IVString extends IVMetadata implements VString {

    private final String value;

    public IVString(String value) {
        this(value, ValueFactory.alarmNone(), ValueFactory.timeNow());
    }

    public IVString(String value, Alarm alarm, Time time) {
        super(alarm, time);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
