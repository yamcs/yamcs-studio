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
import static org.yamcs.studio.data.vtype.ValueFactory.newVString;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.VType;

public class StateDatasource implements Datasource {

    private static final String SCHEME = "state://";

    /**
     * ExecutorService on which all data is polled.
     */
    private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private Map<String, StateData> name2data = new HashMap<>();
    private Map<IPV, StateData> pv2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith(SCHEME);
    }

    @Override
    public boolean isConnected(IPV pv) {
        var stateData = pv2data.get(pv);
        return stateData != null && stateData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        var stateData = pv2data.get(pv);
        if (stateData != null) {
            return stateData.getValue();
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

        var stateData = name2data.computeIfAbsent(basename, x -> {
            switch (basename) {
            case "yamcs.host":
                return new YamcsHostState(exec);
            case "yamcs.instance":
                return new YamcsInstanceState(exec);
            case "yamcs.processor":
                return new YamcsProcessorState(exec);
            case "yamcs.serverId":
                return new YamcsServerIdState(exec);
            case "yamcs.username":
                return new YamcsUsernameState(exec);
            case "yamcs.version":
                return new YamcsVersionState(exec);
            default:
                throw new IllegalArgumentException("Channel " + basename + " does not exist");
            }
        });

        pv2data.put(pv, stateData);
        stateData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        var stateData = pv2data.remove(pv);
        if (stateData != null) {
            stateData.unregister(pv);
        }
    }

    private static final class YamcsHostState extends StateData {
        String previousValue;

        YamcsHostState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String value = null;
            var client = YamcsPlugin.getYamcsClient();
            if (client != null) {
                value = client.getHost();
            }

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

    private static final class YamcsInstanceState extends StateData {
        String previousValue;

        YamcsInstanceState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            var value = YamcsPlugin.getInstance();

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

    private static final class YamcsProcessorState extends StateData {
        String previousValue;

        YamcsProcessorState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            var value = YamcsPlugin.getProcessor();

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

    private static final class YamcsServerIdState extends StateData {
        String previousValue;

        YamcsServerIdState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String value = null;
            var serverInfo = YamcsPlugin.getServerInfo();
            if (serverInfo != null) {
                value = serverInfo.getServerId();
            }

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

    private static final class YamcsUsernameState extends StateData {
        String previousValue;

        YamcsUsernameState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String value = null;
            var userInfo = YamcsPlugin.getUser();
            if (userInfo != null) {
                value = userInfo.getName();
            }

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

    private static final class YamcsVersionState extends StateData {
        String previousValue;

        YamcsVersionState(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        VType createValue() {
            String value = null;
            var serverInfo = YamcsPlugin.getServerInfo();
            if (serverInfo != null) {
                value = serverInfo.getYamcsVersion();
            }

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
