/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.datadefinition;

/**
 * A wrapper for short[].
 */
public class ShortArrayWrapper implements IPrimaryArrayWrapper {

    private short[] data;

    public ShortArrayWrapper(short[] data) {
        this.data = data;
    }

    public void setData(short[] data) {
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
