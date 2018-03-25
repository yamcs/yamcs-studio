/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.List;

import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.Time;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.ValueUtil;

/**
 * Definition for a function that can be integrated in the formula language.
 *
 * @author carcassi
 */
public interface FormulaFunction {

    /**
     * Whether the function is a pure function, given the same arguments always returns the same result.
     *
     * @return true if it's a pure function
     */
    public boolean isPure();

    /**
     * Whether the function takes a variable number of arguments.
     * <p>
     * Variable arguments can only be at the end of the argument list, and have the same type.
     *
     * @return true if the function can accept a variable number of arguments
     */
    public boolean isVarArgs();

    /**
     * Return the name of the function.
     *
     * @return the function name
     */
    public String getName();

    /**
     * Return the description of the function.
     *
     * @return the function description
     */
    public String getDescription();

    /**
     * The ordered list of the arguments type.
     *
     * @return the types of the arguments
     */
    public List<Class<?>> getArgumentTypes();

    /**
     * The ordered list of the argument names.
     *
     * @return the names of the names
     */
    public List<String> getArgumentNames();

    /**
     * The type of the function result.
     *
     * @return the result type
     */
    public Class<?> getReturnType();

    /**
     * Calculate the result of the function given the arguments.
     *
     * @param args
     *            the argument list
     * @return the result of the function
     */
    public Object calculate(List<Object> args);

    /**
     * Returns the time with latest valid timestamp or now.
     *
     * @param args
     *            a list of values
     * @return the latest time; can't be null
     */
    public default Time latestValidTimeOrNowOf(final List<Object> args) {
        Time finalTime = null;

        // Give priority to parameter time to prevent issues with sysdate associated to constants
        // being later than parameter time in case of replays.
        boolean useOnlyParameterTime = false;
        for (Object object : args) {
            if (object != null && object.getClass().getName().startsWith("org.yamcs")) {
                useOnlyParameterTime = true;
                break;
            }
        }

        for (Object object : args) {
            Time newTime;
            if (object != null) {
                if (useOnlyParameterTime && !object.getClass().getName().startsWith("org.yamcs")) {
                    continue;
                }
                newTime = ValueUtil.timeOf(object);
                if (newTime != null && newTime.isTimeValid()
                        && (finalTime == null || newTime.getTimestamp().compareTo(finalTime.getTimestamp()) > 0)) {
                    finalTime = newTime;
                }
            }
        }

        if (finalTime == null) {
            finalTime = ValueFactory.timeNow();
        }

        return finalTime;
    }

    /**
     * Returns the alarm with highest severity. null values can either be ignored or treated as UNDEFINED severity.
     *
     * @param args
     *            a list of values
     * @param considerNull
     *            whether to consider null values
     * @return the highest alarm; can't be null
     */
    public default Alarm highestSeverityOf(final List<Object> args, final boolean considerNull) {
        Alarm finalAlarm = ValueFactory.alarmNone();
        for (Object object : args) {
            Alarm newAlarm;
            if (object == null && considerNull) {
                newAlarm = ValueFactory.newAlarm(AlarmSeverity.UNDEFINED, "No Value");
            } else {
                newAlarm = ValueUtil.alarmOf(object);
                if (newAlarm == null) {
                    newAlarm = ValueFactory.alarmNone();
                }
            }
            if (newAlarm.getAlarmSeverity().compareTo(finalAlarm.getAlarmSeverity()) > 0) {
                finalAlarm = newAlarm;
            }
        }

        return finalAlarm;
    }
}
