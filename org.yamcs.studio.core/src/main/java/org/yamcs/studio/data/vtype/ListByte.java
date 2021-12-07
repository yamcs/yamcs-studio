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
 * An ordered collection of {@code byte}s.
 */
public abstract class ListByte implements ListNumber, CollectionByte {

    @Override
    public IteratorByte iterator() {
        return new IteratorByte() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public byte nextByte() {
                return getByte(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return (float) getByte(index);
    }

    @Override
    public float getFloat(int index) {
        return (float) getByte(index);
    }

    @Override
    public long getLong(int index) {
        return (long) getByte(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getByte(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getByte(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setLong(int index, long value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setInt(int index, int value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setShort(int index, short value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setByte(int index, byte value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        // Should compare to the higher precision if needed
        if (obj instanceof ListDouble || obj instanceof ListFloat || obj instanceof ListLong || obj instanceof ListInt
                || obj instanceof ListShort) {
            return obj.equals(this);
        }

        if (obj instanceof ListNumber) {
            var other = (ListNumber) obj;

            if (size() != other.size()) {
                return false;
            }

            for (var i = 0; i < size(); i++) {
                if (getByte(i) != other.getByte(i)) {
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
            result = 31 * result + getShort(i);
        }
        return result;
    }

}
