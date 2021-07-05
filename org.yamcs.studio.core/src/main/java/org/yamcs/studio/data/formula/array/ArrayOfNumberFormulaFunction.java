/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newDisplay;

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.formula.Statistics;
import org.yamcs.studio.data.formula.StatisticsUtil;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.NumberFormats;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.ValueFactory;
import org.yamcs.studio.data.vtype.ValueUtil;

class ArrayOfNumberFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "arrayOf";
    }

    @Override
    public String getDescription() {
        return "Constructs array from a series of numbers";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("args");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        ListDouble data = new ListDouble() {
            @Override
            public double getDouble(int index) {
                VNumber number = (VNumber) args.get(index);
                if (number == null || number.getValue() == null) {
                    return Double.NaN;
                } else {
                    return number.getValue().doubleValue();
                }
            }

            @Override
            public int size() {
                return args.size();
            }
        };

        VNumber firstNonNull = null;
        for (Object object : args) {
            if (object != null) {
                firstNonNull = (VNumber) object;
            }
        }

        Display display = displayNone();
        if (firstNonNull != null) {
            if (ValueUtil.displayHasValidDisplayLimits(firstNonNull)) {
                display = firstNonNull;
            } else {
                Statistics stats = StatisticsUtil.statisticsOf(data);
                display = newDisplay(stats.getRange().getMinimum(), stats.getRange().getMinimum(),
                        stats.getRange().getMinimum(),
                        "", NumberFormats.toStringFormat(), stats.getRange().getMaximum(),
                        stats.getRange().getMaximum(), stats.getRange().getMaximum(),
                        stats.getRange().getMinimum(), stats.getRange().getMaximum());
            }

        }

        return ValueFactory.newVNumberArray(data,
                highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args),
                display);
    }
}
