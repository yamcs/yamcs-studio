package org.yamcs.studio.css.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.css.core.pvmanager.PVConnectionInfo;
import org.yamcs.studio.css.core.pvmanager.ParameterChannelHandler;

public class PVCatalogue implements YamcsAware {

    private static final Logger log = Logger.getLogger(PVCatalogue.class.getName());

    // Store PV channel handlers while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, ParameterChannelHandler> channelHandlersById = new LinkedHashMap<>();

    private ParameterSubscription subscription;

    public static PVCatalogue getInstance() {
        return Activator.getDefault().getPVCatalogue();
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        if (subscription != null) {
            subscription.cancel(true);
        }
        Display.getDefault().asyncExec(() -> {
            OPIUtils.resetDisplays();
        });

        if (processor == null) {
            channelHandlersById.forEach((id, channelHandler) -> {
                channelHandler.processConnectionInfo(new PVConnectionInfo(false, null));
            });
        } else {
            boolean connected = true;
            channelHandlersById.forEach((id, channelHandler) -> {
                // Only the processor has changed. Previously fetched MDB is still applicable
                // And if not, then the changeMissionDatabase will re-update all channels.
                ParameterInfo parameter = YamcsPlugin.getMissionDatabase().getParameterInfo(id);
                channelHandler.processConnectionInfo(new PVConnectionInfo(connected, parameter));
            });
        }
    }

    public synchronized void register(ParameterChannelHandler channelHandler) {
        channelHandlersById.put(channelHandler.getId(), channelHandler);
        // Report current connection state
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        ParameterInfo parameter = YamcsPlugin.getMissionDatabase().getParameterInfo(channelHandler.getId());
        channelHandler.processConnectionInfo(new PVConnectionInfo(connected, parameter));
        // Register (pending) websocket request
        NamedObjectList idList = toNamedObjectList(channelHandler.getId());
    }

    public synchronized void unregister(ParameterChannelHandler channelHandler) {
        channelHandlersById.remove(channelHandler.getId());
        if (channelHandler.isConnected()) {
            NamedObjectList idList = toNamedObjectList(channelHandler.getId());
        }
    }

    private NamedObjectList toNamedObjectList(NamedObjectId id) {
        return NamedObjectList.newBuilder().addList(id).build();
    }

    /*@Override
    public void changeMissionDatabase(MissionDatabase missionDatabase) {
        channelHandlersById.forEach((id, channelHandler) -> {
            ParameterInfo parameter = missionDatabase.getParameterInfo(id);
            if (log.isLoggable(Level.FINER)) {
                log.finer(String.format("Signaling %s --> %s", id, parameter));
            }
            channelHandler.processConnectionInfo(new PVConnectionInfo(true, parameter));
        });
    }*/

    /*@Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            ParameterChannelHandler channelHandler = channelHandlersById.get(pval.getId());
            if (channelHandler != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update channel %s to %s", channelHandler.getId().getName(),
                            StringConverter.toString(pval.getEngValue())));
                }
                channelHandler.processParameterValue(pval);
            }
        }
    }*/

    private void reportConnectionState() {
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        channelHandlersById.forEach((id, channelHandler) -> {
            ParameterInfo parameter = YamcsPlugin.getMissionDatabase().getParameterInfo(id);
            channelHandler.processConnectionInfo(new PVConnectionInfo(connected, parameter));
        });
    }
}
