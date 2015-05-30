package org.yamcs.studio.core.vtype.pv;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.vtype.pv.PV;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.PVConnectionInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPVReader;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.vtype.YamcsVType;
import org.yamcs.studio.core.web.RestClient;

/**
 * TODO not sure how to disconnect/unsubscribe. Looks like the PVPool has some logic to release a
 * PV, but how does it trigger?
 */
public class Para_PV extends PV implements YamcsPVReader, StudioConnectionListener {

    private static final Logger log = Logger.getLogger(Para_PV.class.getName());
    private WebSocketRegistrar webSocketClient;
    private NamedObjectId id;

    protected Para_PV(String name, String baseName) {
        super(name);
        id = NamedObjectId.newBuilder().setName(baseName).build();

        notifyListenersOfPermissions(true /* read-only */);

        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient)
    {
        this.webSocketClient = webSocketClient;
        if (webSocketClient != null)
            this.webSocketClient.register(this);
    }

    @Override
    public void onStudioDisconnect()
    {
        if (webSocketClient != null)
            webSocketClient.unregister(this);
    }

    @Override
    public void write(Object newValue) throws Exception {
        throw new UnsupportedOperationException("No write supported on yamcs PVs");
    }

    @Override
    public void processConnectionInfo(PVConnectionInfo info) {
        if (!info.webSocketOpen) {
            notifyListenersOfDisconnect();
        }
    }

    @Override
    public void reportException(Exception e) {
        // TODO Can't be reported to listeners?
        log.log(Level.SEVERE, "Exception reported on PV " + getName(), e);
    }

    @Override
    public NamedObjectId getId() {
        return id;
    }

    @Override
    public void processParameterValue(ParameterValue pval) {
        log.fine(String.format("Incoming PV update of %s at %s", pval.getId().getName(), pval.getAcquisitionTimeUTC()));
        notifyListenersOfValue(YamcsVType.fromYamcs(pval));
    }

    /**
     * Called by PVPool. Closes the PV releasing underlying resources.
     */
    @Override
    protected void close() {
        super.close();
        webSocketClient.unregister(this);
    }
}
