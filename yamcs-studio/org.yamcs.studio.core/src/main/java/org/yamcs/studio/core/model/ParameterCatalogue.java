package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Rest.ListParameterInfoResponse;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
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

    private List<ParameterInfo> metaParameters = Collections.emptyList();

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, YamcsPVReader> pvReadersById = new LinkedHashMap<>();
    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();

    // Index for faster repeat access
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    private LosTracker losTracker = new LosTracker();

    public static ParameterCatalogue getInstance() {
        YamcsPlugin plugin = YamcsPlugin.getDefault();
        if (plugin != null) {
            return plugin.getCatalogue(ParameterCatalogue.class);
        } else {
            return null;
        }
    }

    @Override
    public void onStudioConnect() {
        loadMetaParameters();
        reportConnectionState();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearState();
        loadMetaParameters();
        reportConnectionState();
    }

    @Override
    public void onStudioDisconnect() {
        clearState();
    }

    private void clearState() {
        metaParameters = Collections.emptyList();
        unitsById.clear();
        reportConnectionState();
    }

    public synchronized void register(YamcsPVReader pvReader) {
        pvReadersById.put(pvReader.getId(), pvReader);
        // Report current connection state
        ParameterInfo p = parametersById.get(pvReader.getId());
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

    public synchronized void processMetaParameters(List<ParameterInfo> metaParameters) {
        this.metaParameters = new ArrayList<>(metaParameters);
        this.metaParameters.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        log.fine("Refreshing all pv readers");
        for (ParameterInfo p : this.metaParameters) {
            NamedObjectId id = NamedObjectId.newBuilder().setName(p.getQualifiedName()).build();
            parametersById.put(id, p);

            // Update unit index
            if (p != null && p.hasType() && p.getType().getUnitSetCount() > 0) {
                String combinedUnit = p.getType().getUnitSet(0).getUnit();
                for (int i = 1; i < p.getType().getUnitSetCount(); i++) {
                    combinedUnit += " " + p.getType().getUnitSet(i).getUnit();
                }
                unitsById.put(id, combinedUnit);
            }
        }

        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo parameter = parametersById.get(id);
            if (log.isLoggable(Level.FINER)) {
                log.finer(String.format("Signaling %s --> %s", id, parameter));
            }
            pvReader.processConnectionInfo(new PVConnectionInfo(parameter));
        });
    }

    public void processParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YamcsPVReader pvReader = pvReadersById.get(pval.getId());
            if (pvReader != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update pvreader %s to %s", pvReader.getId().getName(), pval.getEngValue()));
                }
                losTracker.updatePv(pvReader, pval);
                pvReader.processParameterValue(pval);
            } else {
                log.warning("No pvreader for incoming update of " + pval.getId().getName());
            }
        }
    }

    public void processInvalidIdentification(NamedObjectId id) {
        log.fine("No pv for id " + id);
        //pvReadersById.get(id).reportException(new InvalidIdentification(id));
    }

    private void reportConnectionState() {
        pvReadersById.forEach((id, pvReader) -> {
            ParameterInfo p = parametersById.get(id);
            pvReader.processConnectionInfo(new PVConnectionInfo(p));
        });
    }

    private void loadMetaParameters() {
        log.fine("Fetching available parameters");
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        restClient.get("/mdb/" + instance + "/parameters", null, ListParameterInfoResponse.newBuilder(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                ListParameterInfoResponse response = (ListParameterInfoResponse) responseMsg;
                processMetaParameters(response.getParameterList());
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available Yamcs parameters", e);
            }
        });
    }

    public void requestParameterDetail(String qualifiedName, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            restClient.get("/mdb/" + instance + "/parameters" + qualifiedName, null, ParameterInfo.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void setParameter(String processor, NamedObjectId id, Value value, ResponseHandler responseHandler) {
        String pResource = toURISegments(id);
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            restClient.put("/processors/" + instance + "/" + processor + "/parameters" + pResource, value, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    // TODO find usages. This will only provide condensed info
    public ParameterInfo getParameterInfo(NamedObjectId id) {
        return parametersById.get(id);
    }

    public String getCombinedUnit(NamedObjectId id) {
        return unitsById.get(id);
    }

    public List<ParameterInfo> getMetaParameters() {
        return new ArrayList<>(metaParameters);
    }

    @Override
    public void shutdown() {
        losTracker.shutdown();
    }

    private String toURISegments(NamedObjectId id) {
        if (!id.hasNamespace()) {
            return id.getName();
        } else {
            return "/" + id.getNamespace() + "/" + id.getName();
        }
    }
}
