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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wraps a {@code double[]} into a {@link ListDouble}.
 */
public final class ArrayDouble extends ListDouble implements Serializable {

    private static final long serialVersionUID = 7493025761455302917L;

    private final double[] array;
    private final boolean readOnly;

    /**
     * A new read-only {@code ArrayDouble} that wraps around the given array.
     *
     * @param array
     *            an array
     */
    public ArrayDouble(double... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayDouble} that wraps around the given array.
     *
     * @param array
     *            an array
     * @param readOnly
     *            if false the wrapper allows writes to the array
     */
    public ArrayDouble(double[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public final IteratorDouble iterator() {
        return new IteratorDouble() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public double nextDouble() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return array.length;
    }

    @Override
    public double getDouble(int index) {
        return array[index];
    }

    @Override
    public void setDouble(int index, double value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ArrayDouble) {
            return Arrays.equals(array, ((ArrayDouble) obj).array);
        }

        return super.equals(obj);
    }

    double[] wrappedArray() {
        return array;
    }
}
