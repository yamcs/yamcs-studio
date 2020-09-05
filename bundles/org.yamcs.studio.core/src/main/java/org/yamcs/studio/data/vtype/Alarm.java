package org.yamcs.studio.data.vtype;

/**
 * Alarm information. Represents the severity and name of the highest alarm associated with the channel.
 */
public interface Alarm {

    /**
     * Describes the quality of the value returned. Never null.
     */
    AlarmSeverity getAlarmSeverity();

    /**
     * A brief text representation of the highest currently active alarm. Never null.
     */
    String getAlarmName();
}
