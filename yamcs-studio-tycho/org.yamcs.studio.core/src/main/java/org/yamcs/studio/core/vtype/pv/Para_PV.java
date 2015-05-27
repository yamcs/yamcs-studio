package org.yamcs.studio.core.vtype.pv;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.vtype.pv.PV;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Pvalue.ParameterValue;
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
    private String baseName;

    protected Para_PV(String name, String baseName) {
        super(name);
        this.baseName = baseName;

        // Notify that this PV is read-only
        notifyListenersOfPermissions(true);

        YamcsPlugin.getDefault().addStudioConnectionListener(this);

    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient)
    {
        this.webSocketClient = webSocketClient;
        this.webSocketClient.register(this);
    }

    @Override
    public void onStudioDisconnect()
    {
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
    public String getPVName() {
        return baseName;
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
