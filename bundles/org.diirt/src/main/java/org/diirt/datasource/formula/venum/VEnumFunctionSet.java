/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.venum;

import org.diirt.datasource.formula.*;

/**
 * Formula functions that operate on {@link org.diirt.vtype.VEnum}s.
 *
 * @author carcassi
 */
public class VEnumFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public VEnumFunctionSet() {
        super(new FormulaFunctionSetDescription("venum",
                "Functions for enum manipulation")
                .addFormulaFunction(new EnumIndexOfFunction())
                .addFormulaFunction(new EnumFromVNumberFunction())
        );
    }

}
