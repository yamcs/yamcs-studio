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
 * Wraps a {@code float[]} into a {@link ListFloat}.
 */
public final class ArrayFloat extends ListFloat implements Serializable {

    private static final long serialVersionUID = 7493025761455302918L;

    private final float[] array;
    private final boolean readOnly;

    /**
     * A new read-only {@code ArrayFloat} that wraps around the given array.
     *
     * @param array
     *            an array
     */
    public ArrayFloat(float... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayFloat} that wraps around the given array.
     *
     * @param array
     *            an array
     * @param readOnly
     *            if false the wrapper allows writes to the array
     */
    public ArrayFloat(float[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public final IteratorFloat iterator() {
        return new IteratorFloat() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public float nextFloat() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return array.length;
    }

    @Override
    public float getFloat(int index) {
        return array[index];
    }

    @Override
    public void setFloat(int index, float value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ArrayFloat) {
            return Arrays.equals(array, ((ArrayFloat) obj).array);
        }

        return super.equals(obj);
    }

    float[] wrappedArray() {
        return array;
    }
}
