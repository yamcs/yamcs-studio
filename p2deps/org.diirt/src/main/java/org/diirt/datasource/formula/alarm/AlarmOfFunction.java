/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.alarm;

import static org.diirt.vtype.ValueFactory.*;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;

import org.diirt.datasource.util.NullUtils;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.ValueUtil;

/**
 * Extract the alarm of a value as a VEnum.
 *
 * @author carcassi
 */
class AlarmOfFunction implements FormulaFunction {

    @Override
    public boolean isPure() {
        return true;
    }

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
        return Arrays.<Class<?>>asList(Object.class);
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
        if (NullUtils.containsNull(args)) {
            return null;
        }
        Object arg = args.get(0);
        Alarm alarm = ValueUtil.alarmOf(arg);
        return newVEnum(alarm.getAlarmSeverity().ordinal(), AlarmSeverity.labels(),
                alarm,
                ValueUtil.latestValidTimeOrNowOf(args));
    }

}
