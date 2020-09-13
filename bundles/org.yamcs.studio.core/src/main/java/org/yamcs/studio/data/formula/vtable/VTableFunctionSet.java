/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vtable;

import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.yamcs.studio.data.vtype.VTable}s.
 */
public class VTableFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public VTableFunctionSet() {
        super(new FormulaFunctionSetDescription("vtable", "Function to aggregate and manipulate tables")
                .addFormulaFunction(new ColumnOfVTableFunction())
                .addFormulaFunction(new ColumnFromVNumberArrayFunction())
                .addFormulaFunction(new ColumnFromVStringArrayFunction())
                .addFormulaFunction(new TableOfFormulaFunction())
                .addFormulaFunction(new RangeFormulaFunction())
                .addFormulaFunction(new StepFormulaFunction())
                .addFormulaFunction(new ColumnFromListNumberGeneratorFunction())
                .addFormulaFunction(new NaturalJoinFunction())
                .addFormulaFunction(new TableUnionFunction())
                .addFormulaFunction(new TableRangeFilterFunction())
                .addFormulaFunction(new TableRangeArrayFilterFunction())
                .addFormulaFunction(new TableStringMatchFilterFunction())
                .addFormulaFunction(new TableValueFilterFunction()));
    }
}
