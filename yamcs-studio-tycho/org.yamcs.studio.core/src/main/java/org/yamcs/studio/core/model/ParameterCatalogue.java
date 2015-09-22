package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.pvmanager.LosTracker;
import org.yamcs.studio.core.pvmanager.PVConnectionInfo;
import org.yamcs.studio.core.pvmanager.YamcsPVReader;
import org.yamcs.studio.core.web.InvalidIdentification;
import org.yamcs.studio.core.web.MergeableWebSocketRequest;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.WebSocketRegistrar;

import com.google.protobuf.MessageLite;

public class ParameterCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(ParameterCatalogue.class.getName());

    private List<RestParameter> metaParameters = Collections.emptyList();

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, YamcsPVReader> pvReadersById = new LinkedHashMap<>();
    private Map<NamedObjectId, RestParameter> availableParametersById = new LinkedHashMap<>();

    private LosTracker losTracker = new LosTracker();

    public static ParameterCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class);
    }

    @Override
    public void onStudioConnect() {
        loadMetaParameters();
        reportConnectionState();
    }

    @Override
    public void onStudioDisconnect() {
        metaParameters = Collections.emptyList();
        reportConnectionState();
    }

    public synchronized void register(YamcsPVReader pvReader) {
        pvReadersById.put(pvReader.getId(), pvReader);
        // Report current connection state
        RestParameter p = availableParametersById.get(pvReader.getId());
        pvReader.processConnectionInfo(new PVConnectionInfo(p));
        // Register (pending) websocket request
        NamedObjectList idList = pvReader.toNamedObjectList();
        if (ConnectionManager.getInstance().isConnected()) {
            WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
            webSocketClient.sendMessage(new MergeableWebSocketRequest("parameter", "subscribe", idList));
        }
    }

    public synchronized void unregister(YamcsPVReader pvReader) {
        pvReadersById.remove(pvReader);
        NamedObjectList idList = pvReader.toNamedObjectList();
        if (ConnectionManager.getInstance().isConnected()) {
            WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
            webSocketClient.sendMessage(new MergeableWebSocketRequest("parameter", "unsubscribe", idList));
        }
    }

    public synchronized void processMetaParameters(List<RestParameter> metaParameters) {
        this.metaParameters = new ArrayList<>(metaParameters);
        this.metaParameters.sort((p1, p2) -> {
            return p1.getId().getName().compareTo(p2.getId().getName());
        });

        log.fine("Refreshing all pv readers");
        for (RestParameter p : this.metaParameters)
            availableParametersById.put(p.getId(), p);

        pvReadersById.forEach((id, pvReader) -> {
            RestParameter parameter = availableParametersById.get(id);
            log.finer(String.format("Signaling %s --> %s", id, parameter));
            pvReader.processConnectionInfo(new PVConnectionInfo(parameter));
        });
    }

    public void processParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YamcsPVReader pvReader = pvReadersById.get(pval.getId());
            if (pvReader != null) {
                log.fine(String.format("Request to update pvreader %s to %s", pvReader.getId().getName(), pval.getEngValue()));
                losTracker.updatePv(pvReader, pval);
                pvReader.processParameterValue(pval);
            } else {
                log.warning("No pvreader for incoming update of " + pval.getId().getName());
            }
        }
    }

    public void processInvalidIdentification(NamedObjectId id) {
        pvReadersById.get(id).reportException(new InvalidIdentification(id));
    }

    private void reportConnectionState() {
        pvReadersById.forEach((id, pvReader) -> {
            RestParameter p = availableParametersById.get(id);
            pvReader.processConnectionInfo(new PVConnectionInfo(p));
        });
    }

    private void loadMetaParameters() {
        log.fine("Fetching available parameters");
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        RestListAvailableParametersRequest.Builder req = RestListAvailableParametersRequest.newBuilder();
        restClient.listAvailableParameters(req.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                RestListAvailableParametersResponse response = (RestListAvailableParametersResponse) responseMsg;
                processMetaParameters(response.getParametersList());
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs parameters", e);
            }
        });
    }

    public List<RestParameter> getMetaParameters() {
        return new ArrayList<>(metaParameters);
    }

    @Override
    public void shutdown() {
        losTracker.shutdown();
    }
}
