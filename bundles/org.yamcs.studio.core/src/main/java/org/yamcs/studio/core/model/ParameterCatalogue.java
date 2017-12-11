package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Rest.ListParameterInfoResponse;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.MergeableWebSocketRequest;
import org.yamcs.studio.core.web.WebSocketRegistrar;
import org.yamcs.studio.core.web.YamcsClient;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Keeps track of the parameter model. This does not currently include parameter values.
 */
public class ParameterCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(ParameterCatalogue.class.getName());

    private Set<ParameterListener> parameterListeners = new CopyOnWriteArraySet<>();

    private List<ParameterInfo> metaParameters = Collections.emptyList();
    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();

    // Index for faster repeat access
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    public static ParameterCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class);
    }

    public void addParameterListener(ParameterListener listener) {
        parameterListeners.add(listener);
    }

    public void removeParameterListener(ParameterListener listener) {
        parameterListeners.remove(listener);
    }

    @Override
    public void onStudioConnect() {
        loadMetaParameters();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearState();
        loadMetaParameters();
    }

    @Override
    public void onStudioDisconnect() {
        clearState();
    }

    private void clearState() {
        metaParameters = Collections.emptyList();
        unitsById.clear();
    }

    public synchronized void processMetaParameters(List<ParameterInfo> metaParameters) {
        this.metaParameters = new ArrayList<>(metaParameters);
        this.metaParameters.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

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

        parameterListeners.forEach(ParameterListener::mdbUpdated);
    }

    public void processParameterData(ParameterData pdata) {
        log.finest(String.format("Sending %s parameters to %s listeners",
                pdata.getParameterCount(), parameterListeners.size()));
        parameterListeners.forEach(l -> l.onParameterData(pdata));
    }

    public void processInvalidIdentification(NamedObjectId id) {
        log.fine("No parameter for id " + id);
        parameterListeners.forEach(l -> l.onInvalidIdentification(id));
    }

    private void loadMetaParameters() {
        log.fine("Fetching available parameters");
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        restClient.get("/mdb/" + instance + "/parameters?details", null).whenComplete((data, exc) -> {
            if (exc == null) {
                try {
                    ListParameterInfoResponse response = ListParameterInfoResponse.parseFrom(data);
                    processMetaParameters(response.getParameterList());
                } catch (InvalidProtocolBufferException e) {
                    log.log(Level.SEVERE, "Failed to decode server response", e);
                }
            }
        });
    }

    public CompletableFuture<byte[]> requestParameterDetail(String qualifiedName) {
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return restClient.get("/mdb/" + instance + "/parameters" + qualifiedName, null);
    }

    public CompletableFuture<byte[]> fetchParameterValue(String instance, String qualifiedName) {
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.get("/archive/" + instance + "/parameters2" + qualifiedName + "?limit=1", null);
    }

    public CompletableFuture<byte[]> setParameter(String processor, NamedObjectId id, Value value) {
        String pResource = toURISegments(id);
        YamcsClient yamcsClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.put("/processors/" + instance + "/" + processor + "/parameters" + pResource, value);
    }

    public void subscribeParameters(NamedObjectList idList) {
        if (ConnectionManager.getInstance().isConnected()) {
            WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
            webSocketClient.sendMessage(new MergeableWebSocketRequest("parameter", "subscribe", idList));
        }
    }

    public void unsubscribeParameters(NamedObjectList idList) {
        if (ConnectionManager.getInstance().isConnected()) {
            WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
            webSocketClient.sendMessage(new MergeableWebSocketRequest("parameter", "unsubscribe", idList));
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

    private String toURISegments(NamedObjectId id) {
        if (!id.hasNamespace()) {
            return id.getName();
        } else {
            return "/" + id.getNamespace() + "/" + id.getName();
        }
    }
}
