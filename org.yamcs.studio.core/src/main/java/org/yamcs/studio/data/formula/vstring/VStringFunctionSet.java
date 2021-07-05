/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vstring;

import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.yamcs.studio.data.vtype.VString}s.
 */
public class VStringFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public VStringFunctionSet() {
        super(new FormulaFunctionSetDescription("vstring",
                "Function to aggregate and manipulate strings")
                        .addFormulaFunction(new ConcatStringArrayFunction())
                        .addFormulaFunction(new ConcatStringsFunction())
                        .addFormulaFunction(new ToStringFunction()));
    }
}
