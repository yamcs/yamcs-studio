package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Alarms.Alarm;

/**
 * Reports on alarms on the studio-wide processor
 */
public interface AlarmListener {

    void processAlarm(Alarm alarm);
}
