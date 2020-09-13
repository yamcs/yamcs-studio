/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.alarm;

import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.yamcs.studio.data.vtype.Alarm}s.
 */
public class AlarmFunctionSet extends FormulaFunctionSet {

    public AlarmFunctionSet() {
        super(new FormulaFunctionSetDescription("alarm",
                "Functions for alarm manipulation")
                        .addFormulaFunction(new HighestSeverityFunction())
                        .addFormulaFunction(new AlarmOfFunction()));
    }
}
