/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.math;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.diirt.datasource.formula.StatefulFormulaFunction;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.ValueFactory;

/**
 *
 * @author carcassi
 */
public class IntegrateFormulaFunction extends StatefulFormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "integrate";
    }

    @Override
    public String getDescription() {
        return "Integrates the given signal in time";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("value");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumber.class;
    }

    private Instant previousTime;
    private double integratedValue;
    private List<VNumber> values = new LinkedList<>();

    @Override
    public Object calculate(final List<Object> args) {
        VNumber value = (VNumber) args.get(0);
        if (value != null && value.getValue() != null) {
            values.add(value);
        }

        if (values.isEmpty()) {
            return null;
        }

        if (previousTime == null) {
            Instant now = Instant.now();
            if (now.compareTo(values.get(0).getTimestamp()) <= 0) {
                previousTime = now;
            } else {
                previousTime = values.get(0).getTimestamp();
            }
        }
        Instant currentTime = Instant.now();

        integratedValue += integrate(previousTime, currentTime, values);
        previousTime = currentTime;

        while (values.size() > 1 && values.get(1).getTimestamp().compareTo(currentTime) <= 0) {
            values.remove(0);
        }

        return ValueFactory.newVDouble(integratedValue);
    }

    static double integrate(Instant start, Instant end, List<VNumber> values) {
        if (values.isEmpty()) {
            return 0;
        }

        if (values.get(0).getTimestamp().compareTo(end) >= 0) {
            return 0;
        }

        double integratedValue = 0;
        for (int i = 0; i < values.size() - 1; i++) {
            integratedValue += integrate(start, end, values.get(i), values.get(i+1));
        }
        integratedValue += integrate(start, end, values.get(values.size() - 1), null);

        return integratedValue;
    }

    static double integrate(Instant start, Instant end, VNumber value, VNumber nextValue) {
        Instant actualStart = Collections.max(Arrays.asList(start, value.getTimestamp()));
        Instant actualEnd = end;
        if (nextValue != null) {
            actualEnd = Collections.min(Arrays.asList(end, nextValue.getTimestamp()));
        }
        Duration duration = Duration.between(actualStart, actualEnd);
        if (!duration.isNegative() && !duration.isZero()) {
            return TimeDuration.toSecondsDouble(duration.multipliedBy(value.getValue().longValue()));
        } else {
            return 0;
        }
    }

}
