/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.alarm;

import static org.yamcs.studio.data.vtype.ValueFactory.newVEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.Alarm;
import org.yamcs.studio.data.vtype.AlarmSeverity;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.ValueUtil;

/**
 * Extract the alarm of a value as a VEnum.
 */
class AlarmOfFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "alarmOf";
    }

    @Override
    public String getDescription() {
        return "The alarm severity as a VEnum";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(Object.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("arg");
    }

    @Override
    public Class<?> getReturnType() {
        return VEnum.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }
        Object arg = args.get(0);
        Alarm alarm = ValueUtil.alarmOf(arg);
        return newVEnum(alarm.getAlarmSeverity().ordinal(), AlarmSeverity.labels(),
                alarm,
                latestValidTimeOrNowOf(args));
    }

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
