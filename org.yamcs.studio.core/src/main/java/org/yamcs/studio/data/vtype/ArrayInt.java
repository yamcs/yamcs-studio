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
 * Wraps a {@code int[]} into a {@link ListInt}.
 */
public final class ArrayInt extends ListInt implements Serializable {

    private static final long serialVersionUID = 7493025761455302919L;

    private final int[] array;
    private final boolean readOnly;

    /**
     * A new {@code ArrayInt} that wraps around the given array.
     *
     * @param array
     *            an array
     */
    public ArrayInt(int... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayInt} that wraps around the given array.
     *
     * @param array
     *            an array
     * @param readOnly
     *            if false the wrapper allows writes to the array
     */
    public ArrayInt(int[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public final IteratorInt iterator() {
        return new IteratorInt() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public int nextInt() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return array.length;
    }

    @Override
    public int getInt(int index) {
        return array[index];
    }

    @Override
    public void setInt(int index, int value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ArrayInt) {
            return Arrays.equals(array, ((ArrayInt) obj).array);
        }

        return super.equals(obj);
    }

    int[] wrappedArray() {
        return array;
    }
}
