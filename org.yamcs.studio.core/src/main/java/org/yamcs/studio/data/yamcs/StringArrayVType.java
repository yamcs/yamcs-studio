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

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VTypeToString;

public class StringArrayVType extends YamcsVType implements VStringArray {

    private ListInt sizes;

    private List<String> data;

    public StringArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        var size = value.getArrayValueCount();
        sizes = new ArrayInt(size);

        data = new ArrayList<>();
        for (var i = 0; i < size; i++) {
            data.add(value.getArrayValue(i).getStringValue());
        }
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public List<String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
