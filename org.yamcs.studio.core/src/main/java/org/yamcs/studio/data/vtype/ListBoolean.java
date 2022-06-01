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

/**
 * An ordered collection of {@code boolean}s. Since in Java Boolean does not inherit from Number, ListBoolean does not
 * inherit from ListNumber.
 */
public abstract class ListBoolean {

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range ({@code index < 0 || index >= size()})
     */
    public abstract boolean getBoolean(int index);

    /**
     * Changes the element at the specified position.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range ({@code index < 0 || index >= size()})
     */
    public abstract void setBoolean(int index, boolean value);

    /**
     * Returns the number of elements in the collection.
     */
    public abstract int size();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListBoolean) {
            var other = (ListBoolean) obj;

            if (size() != other.size()) {
                return false;
            }

            for (var i = 0; i < size(); i++) {
                if (getBoolean(i) != other.getBoolean(i)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        var result = 1;
        for (var i = 0; i < size(); i++) {
            result = 31 * result + (getBoolean(i) ? 1 : 0);
        }
        return result;
    }
}
