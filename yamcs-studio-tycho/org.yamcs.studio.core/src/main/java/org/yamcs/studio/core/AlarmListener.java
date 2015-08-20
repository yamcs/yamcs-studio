package org.yamcs.studio.core;

import org.yamcs.protobuf.Alarms.Alarm;

/**
 * Reports on alarms on the studio-wide processor
 */
public interface AlarmListener {

    public void processAlarm(Alarm alarm);

}
