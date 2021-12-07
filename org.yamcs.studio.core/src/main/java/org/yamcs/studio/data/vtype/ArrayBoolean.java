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
 * Wraps a {@code boolean[]} into a {@link ListBoolean}.
 */
public final class ArrayBoolean extends ListBoolean implements Serializable {

    private static final long serialVersionUID = 7493025761455302915L;

    private final boolean[] array;
    private final boolean readOnly;

    /**
     * A new read-only {@code ArrayBoolean} that wraps around the given array.
     *
     * @param array
     *            an array
     */
    public ArrayBoolean(boolean... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayBoolean} that wraps around the given array.
     *
     * @param array
     *            an array
     * @param readOnly
     *            if false the wrapper allows writes to the array
     */
    public ArrayBoolean(boolean[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public final int size() {
        return array.length;
    }

    @Override
    public boolean getBoolean(int index) {
        return array[index];
    }

    @Override
    public void setBoolean(int index, boolean value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayBoolean) {
            return Arrays.equals(array, ((ArrayBoolean) obj).array);
        }

        return super.equals(obj);
    }

    boolean[] wrappedArray() {
        return array;
    }

}
