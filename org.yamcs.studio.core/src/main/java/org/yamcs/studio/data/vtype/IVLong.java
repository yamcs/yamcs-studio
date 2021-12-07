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
class IVLong extends IVNumeric implements VLong {

    private final Long value;

    IVLong(Long value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }

}
