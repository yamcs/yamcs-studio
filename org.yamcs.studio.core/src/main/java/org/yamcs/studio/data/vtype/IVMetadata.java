/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.time.Instant;

/**
 * Partial implementation for numeric types.
 */
public class IVMetadata implements Alarm, Time {

    private final Alarm alarm;
    private final Time time;

    public IVMetadata(Alarm alarm, Time time) {
        this.alarm = alarm;
        this.time = time;
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        return alarm.getAlarmSeverity();
    }

    @Override
    public String getAlarmName() {
        return alarm.getAlarmName();
    }

    @Override
    public Instant getTimestamp() {
        return time.getTimestamp();
    }

    @Override
    public Integer getTimeUserTag() {
        return time.getTimeUserTag();
    }

    @Override
    public boolean isTimeValid() {
        return time.isTimeValid();
    }
}
