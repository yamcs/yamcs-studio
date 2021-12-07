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

public class IVBooleanArray extends IVMetadata implements VBooleanArray {

    private final ListInt sizes;
    private final ListBoolean data;

    public IVBooleanArray(ListBoolean data, ListInt sizes, Alarm alarm, Time time) {
        super(alarm, time);
        this.data = data;
        this.sizes = sizes;
    }

    @Override
    public ListBoolean getData() {
        return data;
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
