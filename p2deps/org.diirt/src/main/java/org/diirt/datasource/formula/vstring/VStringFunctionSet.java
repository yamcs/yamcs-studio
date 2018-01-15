/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vstring;

import org.diirt.datasource.formula.FormulaFunctionSet;
import org.diirt.datasource.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.diirt.vtype.VString}s.
 *
 * @author shroffk
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
                .addFormulaFunction(new ToStringFunction())
        );
    }

}
