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
 * Wraps a {@code short[]} into a {@link ListShort}.
 */
public final class ArrayShort extends ListShort implements Serializable {

    private static final long serialVersionUID = 7493025761455302921L;

    private final short[] array;
    private final boolean readOnly;

    /**
     * A new {@code ArrayShort} that wraps around the given array.
     */
    public ArrayShort(short... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayShort} that wraps around the given array.
     *
     * @param array
     *            an array
     * @param readOnly
     *            if false the wrapper allows writes to the array
     */
    public ArrayShort(short[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public IteratorShort iterator() {
        return new IteratorShort() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public short nextShort() {
                return array[index++];
            }
        };
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public short getShort(int index) {
        return array[index];
    }

    @Override
    public void setShort(int index, short value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ArrayShort) {
            return Arrays.equals(array, ((ArrayShort) obj).array);
        }

        return super.equals(obj);
    }

    short[] wrappedArray() {
        return array;
    }
}
