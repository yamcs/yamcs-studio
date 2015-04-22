package org.csstudio.vtype.pv.yamcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.PVConnectionInfo;
import org.csstudio.platform.libs.yamcs.WebSocketRegistrar;
import org.csstudio.platform.libs.yamcs.YamcsPVReader;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.vtype.YamcsVType;
import org.csstudio.vtype.pv.PV;
import org.yamcs.protobuf.Pvalue.ParameterValue;

/**
 * TODO not sure how to disconnect/unsubscribe. Looks like the PVPool has some logic to release a
 * PV, but how does it trigger?
 */
public class Para_PV extends PV implements YamcsPVReader {

    private static final Logger log = Logger.getLogger(Para_PV.class.getName());
    private WebSocketRegistrar webSocketClient;
    private String baseName;

    protected Para_PV(String name, String baseName) {
        super(name);
        this.baseName = baseName;

        // Notify that this PV is read-only
        notifyListenersOfPermissions(true);

        webSocketClient = YamcsPlugin.getDefault().getWebSocketClient();
        webSocketClient.register(this);
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
