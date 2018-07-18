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

public class PVCatalogue implements YamcsConnectionListener, InstanceListener, ParameterListener {

    private static final Logger log = Logger.getLogger(PVCatalogue.class.getName());

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, ParameterChannelHandler> pvReadersById = new LinkedHashMap<>();

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

    public synchronized void register(ParameterChannelHandler pvReader) {
        pvReadersById.put(pvReader.getId(), pvReader);
        // Report current connection state
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(pvReader.getId());
        pvReader.processConnectionInfo(new PVConnectionInfo(connected, p));
        // Register (pending) websocket request
        NamedObjectList idList = toNamedObjectList(pvReader.getId());
        ParameterCatalogue.getInstance().subscribeParameters(idList);
    }

    public synchronized void unregister(ParameterChannelHandler pvReader) {
        pvReadersById.remove(pvReader.getId());
        if (pvReader.isConnected()) {
            NamedObjectList idList = toNamedObjectList(pvReader.getId());
            ParameterCatalogue.getInstance().unsubscribeParameters(idList);
        }
    }

    private NamedObjectList toNamedObjectList(NamedObjectId id) {
        return NamedObjectList.newBuilder().addList(id).build();
    }

    @Override
    public void mdbUpdated() {
        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo parameter = ParameterCatalogue.getInstance().getParameterInfo(id);
            if (log.isLoggable(Level.FINER)) {
                log.finer(String.format("Signaling %s --> %s", id, parameter));
            }
            pvReader.processConnectionInfo(new PVConnectionInfo(true, parameter));
        });
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            ParameterChannelHandler pvReader = pvReadersById.get(pval.getId());
            if (pvReader != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update pvreader %s to %s", pvReader.getId().getName(),
                            pval.getEngValue()));
                }
                pvReader.processParameterValue(pval);
            }
        }
    }

    private void reportConnectionState() {
        boolean connected = YamcsPlugin.getYamcsClient().isConnected();
        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(id);
            pvReader.processConnectionInfo(new PVConnectionInfo(connected, p));
        });
    }
}
