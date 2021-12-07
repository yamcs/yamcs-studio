/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newAlarm;
import static org.yamcs.studio.data.vtype.ValueFactory.newTime;
import static org.yamcs.studio.data.vtype.ValueFactory.newVDouble;
import static org.yamcs.studio.data.vtype.ValueFactory.newVString;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.yamcs.studio.data.vtype.Alarm;
import org.yamcs.studio.data.vtype.AlarmSeverity;
import org.yamcs.studio.data.vtype.VType;

public class SysDatasource implements Datasource {

    private static final String SCHEME = "sys://";

    /**
     * ExecutorService on which all data is polled.
     */
    private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private Map<String, SysData> name2data = new HashMap<>();
    private Map<IPV, SysData> pv2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith(SCHEME);
    }

    @Override
    public boolean isConnected(IPV pv) {
        var sysData = pv2data.get(pv);
        return sysData != null && sysData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        var sysData = pv2data.get(pv);
        if (sysData != null) {
            return sysData.getValue();
        }
        return null;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onStarted(IPV pv) {
        var basename = pv.getName().substring(SCHEME.length());

        var sysData = name2data.computeIfAbsent(basename, x -> {
            switch (basename) {
            case "free_mb":
                return new FreeMbSys(exec);
            case "max_mb":
                return new MaxMbSys(exec);
            case "used_mb":
                return new UsedMbSys(exec);
            case "time":
                return new TimeSys(exec);
            case "user":
                return new UserSys(exec);
            case "host_name":
                return new HostNameSys(exec);
            case "qualified_host_name":
                return new QualifiedHostNameSys(exec);
            default:
                if (basename.startsWith("system.")) {
                    var propertyName = basename.substring("system.".length());
                    return new SystemPropertySys(propertyName, exec);
                }
                throw new IllegalArgumentException("Channel " + basename + " does not exist");
            }
        });

        pv2data.put(pv, sysData);
        sysData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        var sysData = pv2data.remove(pv);
        if (sysData != null) {
            sysData.unregister(pv);
        }
    }

    private static final class FreeMbSys extends SysData {
        FreeMbSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            return newVDouble(bytesToMebiByte(Runtime.getRuntime().freeMemory()), alarmNone(), timeNow(),
                    memoryDisplay);
        }
    }

    private static final class MaxMbSys extends SysData {
        MaxMbSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            return newVDouble(bytesToMebiByte(Runtime.getRuntime().maxMemory()), alarmNone(), timeNow(), memoryDisplay);
        }
    }

    private static final class UsedMbSys extends SysData {
        UsedMbSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            return newVDouble(bytesToMebiByte(Runtime.getRuntime().totalMemory()), alarmNone(), timeNow(),
                    memoryDisplay);
        }
    }

    private static final class TimeSys extends SysData {

        private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

        TimeSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            var time = Instant.now();
            var formatted = timeFormat.format(ZonedDateTime.ofInstant(time, ZoneId.systemDefault()));
            return newVString(formatted, alarmNone(), newTime(time));
        }
    }

    private static final class UserSys extends SysData {
        String propertyName;
        String previousValue;

        UserSys(ScheduledExecutorService executor) {
            super(executor);
            propertyName = "user.name";
        }

        @Override
        VType createValue() {
            var value = System.getProperty(propertyName);
            if (value == null) {
                value = "";
            }
            if (!Objects.equals(value, previousValue)) {
                previousValue = value;
                return newVString(value, alarmNone(), timeNow());
            } else {
                return null;
            }
        }
    }

    private static final class HostNameSys extends SysData {
        String previousValue;

        HostNameSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String hostname;
            Alarm alarm;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
                alarm = alarmNone();
            } catch (UnknownHostException ex) {
                hostname = "Unknown host";
                alarm = newAlarm(AlarmSeverity.INVALID, "Undefined");
            }
            if (!Objects.equals(hostname, previousValue)) {
                previousValue = hostname;
                return newVString(hostname, alarm, timeNow());
            } else {
                return null;
            }
        }
    }

    private static final class QualifiedHostNameSys extends SysData {
        String previousValue;

        QualifiedHostNameSys(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String hostname;
            Alarm alarm;
            try {
                hostname = InetAddress.getLocalHost().getCanonicalHostName();
                alarm = alarmNone();
            } catch (UnknownHostException ex) {
                hostname = "Unknown host";
                alarm = newAlarm(AlarmSeverity.INVALID, "Undefined");
            }
            if (!Objects.equals(hostname, previousValue)) {
                previousValue = hostname;
                return newVString(hostname, alarm, timeNow());
            } else {
                return null;
            }
        }
    }

    private static final class SystemPropertySys extends SysData {
        String propertyName;
        String previousValue;

        SystemPropertySys(String propertyName, ScheduledExecutorService executor) {
            super(executor);
            this.propertyName = propertyName;
        }

        @Override
        VType createValue() {
            var value = System.getProperty(propertyName);
            if (value == null) {
                value = "";
            }
            if (!Objects.equals(value, previousValue)) {
                previousValue = value;
                return newVString(value, alarmNone(), timeNow());
            } else {
                return null;
            }
        }
    }
}
