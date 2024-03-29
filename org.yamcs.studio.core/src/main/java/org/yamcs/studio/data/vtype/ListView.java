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
 * Provides a view of a wrapped list that only exposes the elements with the given indexes.
 */
class ListView {

    /**
     * A ListView implementation for doubles.
     */
    static class Double extends ListDouble {
        private final ListDouble list;
        private final ListInt indexes;

        public Double(ListDouble list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public double getDouble(int index) {
            return list.getDouble(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }

    /**
     * A ListView implementation for floats.
     */
    static class Float extends ListFloat {
        private final ListFloat list;
        private final ListInt indexes;

        public Float(ListFloat list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public float getFloat(int index) {
            return list.getFloat(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }

    /**
     * A ListView implementation for longs.
     */
    static class Long extends ListLong {
        private final ListLong list;
        private final ListInt indexes;

        public Long(ListLong list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public long getLong(int index) {
            return list.getLong(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }

    /**
     * A ListView implementation for ints.
     */
    static class Int extends ListInt {
        private final ListInt list;
        private final ListInt indexes;

        public Int(ListInt list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public int getInt(int index) {
            return list.getInt(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }

    /**
     * A ListView implementation for shorts.
     */
    static class Short extends ListShort {
        private final ListShort list;
        private final ListInt indexes;

        public Short(ListShort list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public short getShort(int index) {
            return list.getShort(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }

    /**
     * A ListView implementation for bytes.
     */
    static class Byte extends ListByte {
        private final ListByte list;
        private final ListInt indexes;

        public Byte(ListByte list, ListInt indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public byte getByte(int index) {
            return list.getByte(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }
    }
}
