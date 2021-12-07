/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.datadefinition;

/**
 * A wrapper for float[].
 */
public class FloatArrayWrapper implements IPrimaryArrayWrapper {

    private float[] data;

    public FloatArrayWrapper(float[] data) {
        this.data = data;
    }

    public void setData(float[] data) {
        this.data = data;
    }

    @Override
    public double get(int i) {
        return data[i];
    }

    @Override
    public int getSize() {
        return data.length;
    }

}
