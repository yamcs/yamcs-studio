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
import org.yamcs.studio.data.vtype.ArrayFloat;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListFloat;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VFloatArray;
import org.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.studio.data.vtype.ValueUtil;

public class FloatArrayVType extends YamcsVType implements VFloatArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListFloat data;

    public FloatArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        var size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        var floatValues = new float[size];
        for (var i = 0; i < floatValues.length; i++) {
            floatValues[i] = value.getArrayValue(i).getFloatValue();
        }
        data = new ArrayFloat(floatValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListFloat getData() {
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
