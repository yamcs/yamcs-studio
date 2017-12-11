package org.yamcs.studio.css;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.ParameterListener;
import org.yamcs.studio.css.pvmanager.PVConnectionInfo;
import org.yamcs.studio.css.pvmanager.YamcsPVReader;

public class PVCatalogue implements Catalogue, ParameterListener {

    private static final Logger log = Logger.getLogger(PVCatalogue.class.getName());

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, YamcsPVReader> pvReadersById = new LinkedHashMap<>();

    public static PVCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(PVCatalogue.class);
    }

    @Override
    public void onStudioConnect() {
        reportConnectionState();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        reportConnectionState();
    }

    @Override
    public void onStudioDisconnect() {
        reportConnectionState();
    }

    public synchronized void register(YamcsPVReader pvReader) {
        pvReadersById.put(pvReader.getId(), pvReader);
        // Report current connection state
        ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(pvReader.getId());
        pvReader.processConnectionInfo(new PVConnectionInfo(p));
        // Register (pending) websocket request
        NamedObjectList idList = pvReader.toNamedObjectList();
        ParameterCatalogue.getInstance().subscribeParameters(idList);
    }

    public synchronized void unregister(YamcsPVReader pvReader) {
        pvReadersById.remove(pvReader.getId());
        NamedObjectList idList = pvReader.toNamedObjectList();
        ParameterCatalogue.getInstance().unsubscribeParameters(idList);
    }

    @Override
    public void mdbUpdated() {
        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo parameter = ParameterCatalogue.getInstance().getParameterInfo(id);
            if (log.isLoggable(Level.FINER)) {
                log.finer(String.format("Signaling %s --> %s", id, parameter));
            }
            pvReader.processConnectionInfo(new PVConnectionInfo(parameter));
        });
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YamcsPVReader pvReader = pvReadersById.get(pval.getId());
            if (pvReader != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update pvreader %s to %s", pvReader.getId().getName(),
                            pval.getEngValue()));
                }
                pvReader.processParameterValue(pval);
            } else {
                log.warning("No pvreader for incoming update of " + pval.getId().getName());
            }
        }
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        // pvReadersById.get(id).reportException(new InvalidIdentification(id));
    }

    private void reportConnectionState() {
        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(id);
            pvReader.processConnectionInfo(new PVConnectionInfo(p));
        });
    }
}
