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

import java.util.List;

/**
 * A table. Tables are collections of columns, each of which is composed of a String representing the name of the column
 * and a list of a particular type (all elements of the same column must be of the same type).
 */
public interface VTable extends VType {

    /**
     * The number of columns in the table.
     *
     * @return the number of columns
     */
    int getColumnCount();

    /**
     * The number of rows in the table.
     * <p>
     * Currently, it is not clear whether all columns actually have the same number of rows, that is if all arrays have
     * the same length. In the case of variable row, this will return the maximum row count, that is the length of the
     * longest array/column.
     *
     * @return the number of rows
     */
    int getRowCount();

    /**
     * The type of the elements in the column. The column array will be an array of the given type. For primitive types,
     * this function will return the TYPE class (such as {@link Double#TYPE}, while {@link #getColumnData(int) } will
     * return a {@link ListNumber}.
     *
     * @param column
     *            the column index
     * @return the type of this column
     */
    Class<?> getColumnType(int column);

    /**
     * The name of the given column.
     *
     * @param column
     *            the column index
     * @return the name of the column
     */
    String getColumnName(int column);

    /**
     * The data for the given column.
     * <p>
     * The data is going to be a {@link List} in case of objects or a {@link ListNumber} in case of a numeric primitive.
     *
     * @param column
     *            the column index
     * @return the data of the column
     */
    Object getColumnData(int column);
}
