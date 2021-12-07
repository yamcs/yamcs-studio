/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012, 2021 diirt developers and others
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.yamcs.studio.data.formula;

import java.util.List;

import org.yamcs.studio.data.vtype.Alarm;
import org.yamcs.studio.data.vtype.AlarmSeverity;
import org.yamcs.studio.data.vtype.Time;
import org.yamcs.studio.data.vtype.ValueFactory;
import org.yamcs.studio.data.vtype.ValueUtil;

/**
 * Definition for a function that can be integrated in the formula language.
 */
public interface FormulaFunction {

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
    public default Time latestValidTimeOrNowOf(List<Object> args) {
        Time finalTime = null;

        // Give priority to parameter time to prevent issues with sysdate associated to constants
        // being later than parameter time in case of replays.
        var useOnlyParameterTime = false;
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
    public default Alarm highestSeverityOf(List<Object> args, boolean considerNull) {
        var finalAlarm = ValueFactory.alarmNone();
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
