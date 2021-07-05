/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.venum;

import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.yamcs.studio.data.vtype.VEnum}s.
 */
public class VEnumFunctionSet extends FormulaFunctionSet {

    public VEnumFunctionSet() {
        super(new FormulaFunctionSetDescription("venum",
                "Functions for enum manipulation")
                        .addFormulaFunction(new EnumIndexOfFunction())
                        .addFormulaFunction(new EnumFromVNumberFunction()));
    }
}
