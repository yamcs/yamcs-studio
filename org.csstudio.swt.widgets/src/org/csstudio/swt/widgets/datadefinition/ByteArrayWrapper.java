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
 * A wrapper for byte[].
 */
public class ByteArrayWrapper implements IPrimaryArrayWrapper {

    private byte[] data;

    public ByteArrayWrapper(byte[] data) {
        this.data = data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public double get(int i) {
        return data[i];
    }

    public int getSize() {
        return data.length;
    }

}
