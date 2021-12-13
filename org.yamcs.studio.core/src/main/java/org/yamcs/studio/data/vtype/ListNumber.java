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
 * An ordered collection of numeric (primitive) elements.
 */
public interface ListNumber extends CollectionNumber {

    /**
     * Returns the element at the specified position in this list casted to a double.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    double getDouble(int index);

    /**
     * Returns the element at the specified position in this list casted to a float.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    float getFloat(int index);

    /**
     * Returns the element at the specified position in this list casted to a long.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    long getLong(int index);

    /**
     * Returns the element at the specified position in this list casted to an int.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    int getInt(int index);

    /**
     * Returns the element at the specified position in this list casted to a short.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    short getShort(int index);

    /**
     * Returns the element at the specified position in this list casted to a byte.
     *
     * @param index
     *            position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    byte getByte(int index);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setDouble(int index, double value);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setFloat(int index, float value);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setLong(int index, long value);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setInt(int index, int value);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setShort(int index, short value);

    /**
     * Changes the element at the specified position, casting to the internal representation.
     *
     * @param index
     *            position of the element to change
     * @param value
     *            the new value
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setByte(int index, byte value);
}
