/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.yamcs;

import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VIntArray;
import org.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.studio.data.vtype.ValueUtil;

public class Sint32ArrayVType extends YamcsVType implements VIntArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ArrayInt data;

    public Sint32ArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        var size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        var intValues = new int[size];
        for (var i = 0; i < intValues.length; i++) {
            intValues[i] = value.getArrayValue(i).getSint32Value();
        }
        data = new ArrayInt(intValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListInt getData() {
        return data;
    }

    @Override
    public List<ArrayDimensionDisplay> getDimensionDisplay() {
        return dimensionDisplay;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
