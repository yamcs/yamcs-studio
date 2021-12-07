/********************************************************************************
 * Copyright (c) 2009 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

import java.util.Arrays;

public class ExprArray extends Expr {

    private int columns;
    private int rows;
    private Expr[] array;

    public ExprArray(int rows, int columns) {
        super(ExprType.Array);
        this.array = new Expr[rows * columns];
        this.columns = columns;
        this.rows = rows;
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public int length() {
        return array.length;
    }

    public Expr get(int index) {
        return array[index];
    }

    public Expr get(int row, int column) {
        return array[row * columns + column];
    }

    public void set(int index, Expr value) {
        array[index] = value;
    }

    public void set(int row, int column, Expr value) {
        array[row * columns + column] = value;
    }

    public Expr[] getInternalArray() {
        return array;
    }

    @Override
    public int hashCode() {
        return 567 ^ rows ^ columns ^ array.length;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExprArray)) {
            return false;
        }

        var a = (ExprArray) obj;
        return a.rows == rows && a.columns == columns && Arrays.equals(a.array, array);
    }
}
