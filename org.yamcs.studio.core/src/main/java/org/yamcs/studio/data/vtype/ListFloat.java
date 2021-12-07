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
 * An ordered collection of {@code float}s.
 */
public abstract class ListFloat implements ListNumber, CollectionFloat {

    @Override
    public IteratorFloat iterator() {
        return new IteratorFloat() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public float nextFloat() {
                return getFloat(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return (double) getFloat(index);
    }

    @Override
    public long getLong(int index) {
        return (long) getFloat(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getFloat(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getFloat(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getFloat(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setFloat(int index, float value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setLong(int index, long value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setInt(int index, int value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setShort(int index, short value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setByte(int index, byte value) {
        setFloat(index, (float) value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListFloat) {
            var other = (ListFloat) obj;

            if (size() != other.size()) {
                return false;
            }

            for (var i = 0; i < size(); i++) {
                if (Float.floatToIntBits(getFloat(i)) != Float.floatToIntBits(other.getFloat(i))) {
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
            result = 31 * result + Float.floatToIntBits(getFloat(i));
        }
        return result;
    }
}
