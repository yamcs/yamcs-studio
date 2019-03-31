package org.yamcs.studio.css.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.ParameterListener;
import org.yamcs.studio.css.core.pvmanager.PVConnectionInfo;
import org.yamcs.studio.css.core.pvmanager.ParameterChannelHandler;
import org.yamcs.utils.StringConverter;

public class PVCatalogue implements YamcsConnectionListener, InstanceListener, ParameterListener {

    private static final Logger log = Logger.getLogger(PVCatalogue.class.getName());

    // Store PV channel handlers while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, ParameterChannelHandler> channelHandlersById = new LinkedHashMap<>();

    public static PVCatalogue getInstance() {
        return Activator.getDefault().getPVCatalogue();
    }

    public PVCatalogue() {
        ManagementCatalogue.getInstance().addInstanceListener(this);
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ParameterCatalogue.getInstance().addParameterListener(this);
    }

    @Override
    public void onYamcsConnected() {
        reportConnectionState();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // TODO verify behaviour. Maybe we should have a beforeInstanceChange
        // and an after to get the correct pv connection state
        Display.getDefault().asyncExec(() -> {
            OPIUtils.resetDisplays();
        });
        reportConnectionState();
    }

    @Override
    public void onYamcsDisconnected() {
        reportConnectionState();
    }

    public synchronized void register(ParameterChannelHandler channelHandler) {
        channelHandlersById.put(channelHandler.getId(), channelHandler);
        // Report current connection state
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        ParameterInfo parameter = ParameterCatalogue.getInstance().getParameterInfo(channelHandler.getId());
        channelHandler.processConnectionInfo(new PVConnectionInfo(connected, parameter));
        // Register (pending) websocket request
        NamedObjectList idList = toNamedObjectList(channelHandler.getId());
        ParameterCatalogue.getInstance().subscribeParameters(idList);
    }

    public synchronized void unregister(ParameterChannelHandler channelHandler) {
        channelHandlersById.remove(channelHandler.getId());
        if (channelHandler.isConnected()) {
            NamedObjectList idList = toNamedObjectList(channelHandler.getId());
            ParameterCatalogue.getInstance().unsubscribeParameters(idList);
        }
    }

    private NamedObjectList toNamedObjectList(NamedObjectId id) {
        return NamedObjectList.newBuilder().addList(id).build();
    }

    @Override
    public void mdbUpdated() {
        channelHandlersById.forEach((id, channelHandler) -> {
            ParameterInfo parameter = ParameterCatalogue.getInstance().getParameterInfo(id);
            if (log.isLoggable(Level.FINER)) {
                log.finer(String.format("Signaling %s --> %s", id, parameter));
            }
            channelHandler.processConnectionInfo(new PVConnectionInfo(true, parameter));
        });
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            ParameterChannelHandler channelHandler = channelHandlersById.get(pval.getId());
            if (channelHandler != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update channel %s to %s", channelHandler.getId().getName(),
                            StringConverter.toString(pval.getEngValue(), false)));
                }
                channelHandler.processParameterValue(pval);
            }
        }
    }

    private void reportConnectionState() {
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        channelHandlersById.forEach((id, channelHandler) -> {
            ParameterInfo parameter = ParameterCatalogue.getInstance().getParameterInfo(id);
            channelHandler.processConnectionInfo(new PVConnectionInfo(connected, parameter));
        });
    }
}
