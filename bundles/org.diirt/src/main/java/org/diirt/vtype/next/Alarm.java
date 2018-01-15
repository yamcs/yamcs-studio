/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.util.Objects;

/**
 * Alarm information. Represents the severity and name of the highest alarm
 * associated with the channel.
 *
 * @author carcassi
 */
public abstract class Alarm {

    /**
     * Returns the alarm severity, which describes the quality of the
     * value returned. Never null.
     *
     * @return the alarm severity
     */
    public abstract AlarmSeverity getSeverity();

    /**
     * Returns a brief text representation of the highest currently active alarm.
     * Never null.
     *
     * @return the alarm status
     */
    public abstract String getName();

    /**
     * Tests whether the give object is and Alarm with the same name and severity.
     *
     * @param obj another alarm
     * @return true if equal
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Alarm) {
            Alarm other = (Alarm) obj;

            return getSeverity().equals(other.getSeverity()) &&
                    getName().equals(other.getName());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getName());
        hash = 23 * hash + Objects.hashCode(getSeverity());
        return hash;
    }

    @Override
    public final String toString() {
        return getSeverity() + "(" + getName() + ")";
    }

    /**
     * A null-safe method to retrieve a string version of an alarm of a
     * value;
     *
     * @param alarmProvider a value with an alarm; can be null
     * @return string representation of the alarm; not null
     */
    public static String toString(AlarmProvider alarmProvider) {
        if (alarmProvider == null) {
            return "NONE";
        }

        return alarmProvider.getAlarm().toString();
    }

    /**
     * New alarm with the given severity and status.
     *
     * @param alarmSeverity the alarm severity
     * @param alarmName the alarm name
     * @return the new alarm
     */
    public static Alarm create(final AlarmSeverity alarmSeverity, final String alarmName) {
        return new IAlarm(alarmSeverity, alarmName);
    }

    private static final Alarm NONE = create(AlarmSeverity.NONE, "None");
    private static final Alarm NO_VALUE = create(AlarmSeverity.INVALID, "No value");
    private static final Alarm DISCONNECTED = create(AlarmSeverity.INVALID, "Disconnected");

    /**
     * No alarm. To be used whenever there is no alarm associated with the value.
     * <p>
     * To test for no alarm, one should always check the severity, and not
     * equality to this specific alarm: depending on the data source the status
     * may be different and contain extra information.
     *
     * @return severity NONE and status "None"
     */
    public static Alarm none() {
        return NONE;
    }

    /**
     * Alarm condition for when a value is not present. To be used as
     * the alarm associated to a null value.
     *
     * @return severity INVALID and status "No Value"
     */
    public static Alarm noValue() {
        return NO_VALUE;
    }

    /**
     * Alarm condition for when a channel is disconnected. To be used as
     * the alarm associated with a broken connection.
     *
     * @return severity UNDEFINED and status "Disconnected"
     */
    public static Alarm disconnected() {
        return DISCONNECTED;
    }

}
