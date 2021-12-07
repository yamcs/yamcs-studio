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

import java.util.List;

public class IVEnum extends IVMetadata implements VEnum {

    private final int index;
    private final List<String> labels;

    public IVEnum(int index, List<String> labels, Alarm alarm, Time time) {
        super(alarm, time);
        if (index < 0 || index >= labels.size()) {
            throw new IndexOutOfBoundsException("VEnum index must be within the label range");
        }
        this.index = index;
        this.labels = labels;
    }

    @Override
    public String getValue() {
        return labels.get(index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
