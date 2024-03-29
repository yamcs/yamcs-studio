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
 * An ordered collection of {@code double}s.
 */
public abstract class ListDouble implements ListNumber, CollectionDouble {

    @Override
    public IteratorDouble iterator() {
        return new IteratorDouble() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public double nextDouble() {
                return getDouble(index++);
            }
        };
    }

    @Override
    public float getFloat(int index) {
        return (float) getDouble(index);
    }

    @Override
    public long getLong(int index) {
        return (long) getDouble(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getDouble(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getDouble(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getDouble(index);
    }

    @Override
    public void setDouble(int index, double value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setFloat(int index, float value) {
        setDouble(index, value);
    }

    @Override
    public void setLong(int index, long value) {
        setDouble(index, value);
    }

    @Override
    public void setInt(int index, int value) {
        setDouble(index, value);
    }

    @Override
    public void setShort(int index, short value) {
        setDouble(index, value);
    }

    @Override
    public void setByte(int index, byte value) {
        setDouble(index, value);
    }

    /**
     * Concatenates a several lists of numbers into a single list
     *
     * @param lists
     *            the lists to concatenate
     * @return the given lists concatenated together
     */
    public static ListDouble concatenate(ListNumber... lists) {

        // since these lists are read-only, we precompute the size
        var size = 0;
        for (var l : lists) {
            size += l.size();
        }
        var sizeCopy = size;

        return new ListDouble() {
            @Override
            public int size() {
                return sizeCopy;
            }

            @Override
            public double getDouble(int index) {
                if (index < 0 || index >= size()) {
                    throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", size: " + size());
                }
                // treat the lists we concatenated as a whole set - that is
                // we never start back at index 0 after traversing through one
                // of the concatenated lists

                // for example, {a, b, c} {d, e, f} used to be indexed as
                // {0, 1, 2} {0, 1, 2} and they are now indexed as
                // {0, 1, 2} {3, 4, 5}
                var startIdx = 0;
                for (var l : lists) {
                    var endIdx = startIdx + l.size() - 1;
                    if (startIdx <= index && index <= endIdx) {
                        return l.getDouble(index - startIdx);
                    }
                    startIdx += l.size();
                }

                // should never happpen
                return 0;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListDouble) {
            var other = (ListDouble) obj;

            if (size() != other.size()) {
                return false;
            }

            for (var i = 0; i < size(); i++) {
                if (Double.doubleToLongBits(getDouble(i)) != Double.doubleToLongBits(other.getDouble(i))) {
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
            var bits = Double.doubleToLongBits(getDouble(i));
            result = 31 * result + (int) (bits ^ (bits >>> 32));
        }
        return result;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("[");
        var i = 0;
        for (; i < size() - 1; i++) {
            builder.append(getDouble(i)).append(", ");
        }
        builder.append(getDouble(i)).append("]");
        return builder.toString();
    }
}
