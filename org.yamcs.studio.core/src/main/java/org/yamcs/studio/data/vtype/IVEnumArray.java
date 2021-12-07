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

import java.util.ArrayList;
import java.util.List;

public class IVEnumArray extends IVMetadata implements VEnumArray {

    private final ListInt indexes;
    private final List<String> labels;
    private final ListInt sizes;
    private final List<String> array;

    public IVEnumArray(ListInt indexes, List<String> labels, ListInt sizes, Alarm alarm, Time time) {
        super(alarm, time);
        List<String> tempArray = new ArrayList<>(indexes.size());
        for (var i = 0; i < indexes.size(); i++) {
            var index = indexes.getInt(i);
            if (index < 0 || index >= labels.size()) {
                throw new IndexOutOfBoundsException("VEnumArray indexes must be within the label range");
            }
            tempArray.add(labels.get(index));
        }
        this.array = tempArray;
        this.indexes = indexes;
        this.labels = labels;
        this.sizes = sizes;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public List<String> getData() {
        return array;
    }

    @Override
    public ListInt getIndexes() {
        return indexes;
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
