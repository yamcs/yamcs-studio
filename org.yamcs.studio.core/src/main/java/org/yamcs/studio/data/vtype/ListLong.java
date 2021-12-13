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
 * An ordered collection of {@code long}s.
 */
public abstract class ListLong implements ListNumber, CollectionLong {

    @Override
    public IteratorLong iterator() {
        return new IteratorLong() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public long nextLong() {
                return getLong(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return getLong(index);
    }

    @Override
    public float getFloat(int index) {
        return getLong(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getLong(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getLong(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getLong(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setLong(index, (long) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setLong(index, (long) value);
    }

    @Override
    public void setLong(int index, long value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setInt(int index, int value) {
        setLong(index, value);
    }

    @Override
    public void setShort(int index, short value) {
        setLong(index, value);
    }

    @Override
    public void setByte(int index, byte value) {
        setLong(index, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListLong) {
            var other = (ListLong) obj;

            if (size() != other.size()) {
                return false;
            }

            for (var i = 0; i < size(); i++) {
                if (getLong(i) != other.getLong(i)) {
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
            var element = getLong(i);
            var elementHash = (int) (element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }
        return result;
    }
}
