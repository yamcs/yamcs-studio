package org.yamcs.studio.data;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVString;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.UserInfo;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.VType;

public class StateDatasource implements Datasource {

    private static final String SCHEME = "state://";

    /**
     * ExecutorService on which all data is polled.
     */
    private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private Map<IPV, StateData> pv2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith(SCHEME);
    }

    @Override
    public boolean isConnected(IPV pv) {
        StateData stateData = pv2data.get(pv);
        return stateData != null && stateData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        StateData stateData = pv2data.get(pv);
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
        StateData stateData;
        String basename = pv.getName().substring(SCHEME.length());
        switch (basename) {
        case "yamcs.host":
            stateData = new YamcsHostState(exec);
            break;
        case "yamcs.instance":
            stateData = new YamcsInstanceState(exec);
            break;
        case "yamcs.processor":
            stateData = new YamcsProcessorState(exec);
            break;
        case "yamcs.serverId":
            stateData = new YamcsServerIdState(exec);
            break;
        case "yamcs.username":
            stateData = new YamcsUsernameState(exec);
            break;
        case "yamcs.version":
            stateData = new YamcsVersionState(exec);
            break;
        default:
            throw new IllegalArgumentException("Channel " + basename + " does not exist");
        }

        pv2data.put(pv, stateData);
        stateData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        StateData stateData = pv2data.remove(pv);
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
            YamcsClient client = YamcsPlugin.getYamcsClient();
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
            String value = YamcsPlugin.getInstance();

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
            String value = YamcsPlugin.getProcessor();

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
            GetServerInfoResponse serverInfo = YamcsPlugin.getServerInfo();
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
            UserInfo userInfo = YamcsPlugin.getUser();
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
            GetServerInfoResponse serverInfo = YamcsPlugin.getServerInfo();
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
