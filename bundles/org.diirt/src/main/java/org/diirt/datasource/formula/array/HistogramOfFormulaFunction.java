/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.array;

import static org.diirt.vtype.ValueFactory.*;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.StatefulFormulaFunction;
import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;
import org.diirt.util.array.ListNumbers;
import org.diirt.util.stats.Range;
import org.diirt.util.stats.Ranges;
import org.diirt.util.stats.Statistics;
import org.diirt.util.stats.StatisticsUtil;
import org.diirt.util.text.NumberFormats;

import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;

/**
 * @author shroffk
 *
 */
public class HistogramOfFormulaFunction extends StatefulFormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "histogramOf";
    }

    @Override
    public String getDescription() {
        return "Returns a histograms of the elements in the array.";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("Array", "index");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumber.class;
    }

    private VNumberArray previousValue;
    private VNumberArray previousResult;
    private double previousMaxCount;
    private Range previousXRange;

    @Override
    public Object calculate(List<Object> args) {
        VNumberArray numberArray = (VNumberArray) args.get(0);
        if (numberArray == null) {
            return null;
        }

        // If no change, return previous
        if (previousValue == numberArray) {
            return previousResult;
        }

        Statistics stats = StatisticsUtil.statisticsOf(numberArray.getData());
        int nBins = 100;
        Range aggregatedRange = Ranges.aggregateRange(stats.getRange(), previousXRange);
        Range xRange;
        if (Ranges.overlap(aggregatedRange, stats.getRange()) >= 0.75) {
            xRange = aggregatedRange;
        } else {
            xRange = stats.getRange();
        }

        IteratorNumber newValues = numberArray.getData().iterator();
        double minValueRange = xRange.getMinimum();
        double maxValueRange = xRange.getMaximum();

        ListNumber xBoundaries = ListNumbers.linearListFromRange(minValueRange, maxValueRange, nBins + 1);
        String unit = numberArray.getUnits();
        int[] binData = new int[nBins];
        double maxCount = 0;
        while (newValues.hasNext()) {
            double value = newValues.nextDouble();
            // Check value in range
            if (xRange.contains(value)) {

                int bin = (int) Math.floor(xRange.normalize( value) * nBins);
                if (bin == nBins) {
                    bin--;
                }

                binData[bin]++;
                if (binData[bin] > maxCount) {
                    maxCount = binData[bin];
                }
            }
        }

        if (previousMaxCount > maxCount && previousMaxCount < maxCount * 2.0) {
            maxCount = previousMaxCount;
        }

        previousMaxCount = maxCount;
        previousXRange = xRange;
        previousValue = numberArray;
        previousResult = newVNumberArray(new ArrayInt(binData), new ArrayInt(nBins), Arrays.asList(newDisplay(xBoundaries, unit)),
                numberArray, numberArray, newDisplay(0.0, 0.0, 0.0, "count", NumberFormats.format(0), maxCount, maxCount, maxCount, Double.NaN, Double.NaN));

        return previousResult;
    }

}
