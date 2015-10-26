package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Alarms.AlarmInfo;

/**
 * Reports on alarms on the studio-wide processor
 */
public interface AlarmListener {

    void processAlarmInfo(AlarmInfo alarmInfo);
}
