package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Rest.CreateProcessorRequest;
import org.yamcs.protobuf.Rest.EditClientRequest;
import org.yamcs.protobuf.Rest.EditProcessorRequest;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.Web.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo.ClientState;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.YamcsInstance;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;

/**
 * Provides access to aggregated state on yamcs management-type information.
 * <p>
 * There should be only one long-lived instance of this class, which goes down together with the application (same
 * lifecycle as {@link YamcsPlugin}). This catalogue deals with maintaining correct state accross connection-reconnects,
 * so listeners only need to register once.
 */
public class ManagementCatalogue implements Catalogue, WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(ManagementCatalogue.class.getName());

    private Set<ManagementListener> managementListeners = new CopyOnWriteArraySet<>();
    private Set<InstanceListener> instanceListeners = new CopyOnWriteArraySet<>();

    private Map<Integer, ClientInfo> clientInfoById = new ConcurrentHashMap<>();

    private ConnectionInfo connectionInfo;
    private ProcessorInfo currentProcessor;

    public static ManagementCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class);
    }

    @Override
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("management", "subscribe"), this);
        yamcsClient.subscribe(new WebSocketRequest("processor", "subscribe"), this);
        connectionInfo = yamcsClient.getConnectionInfo();
        if (connectionInfo != null) {
            currentProcessor = connectionInfo.getProcessor();
        }
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasConnectionInfo()) {

            ConnectionInfo prevConnectionInfo = connectionInfo;
            connectionInfo = msg.getConnectionInfo();
            currentProcessor = connectionInfo.getProcessor();

            if (prevConnectionInfo != null
                    && !prevConnectionInfo.getInstance().getName().equals(connectionInfo.getInstance().getName())) {
                YamcsInstance instance = connectionInfo.getInstance();
                log.fine("Instance " + instance.getName() + ": " + instance.getState());
                instanceListeners.forEach(l -> l.instanceChanged(
                        prevConnectionInfo.getInstance().getName(), instance.getName()));
            }

            managementListeners.forEach(l -> l.instanceUpdated(connectionInfo));
        }

        if (msg.hasClientInfo()) {
            ClientInfo clientInfo = msg.getClientInfo();
            if (clientInfo.getState() == ClientState.DISCONNECTED) {
                clientInfoById.remove(clientInfo.getId());
                managementListeners.forEach(l -> l.clientDisconnected(clientInfo));
            } else {
                clientInfoById.put(clientInfo.getId(), clientInfo);
                managementListeners.forEach(l -> l.clientUpdated(clientInfo));
            }
        }

        if (msg.hasProcessorInfo()) {
            currentProcessor = msg.getProcessorInfo();
            managementListeners.forEach(l -> l.processorUpdated(currentProcessor));
        }

        if (msg.hasStatistics()) {
            Statistics statistics = msg.getStatistics();
            managementListeners.forEach(l -> l.statisticsUpdated(statistics));
        }
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // Ignore. It's this catalogues responsability to inform other InstanceListeners;
        // Further, the information in this catalogue is server-wide. Not instance-specific.
    }

    @Override
    public void onYamcsDisconnected() {
        // Clear everything, we'll get a fresh set upon connect
        clientInfoById.clear();
        currentProcessor = null;
        connectionInfo = null;

        managementListeners.forEach(ManagementListener::clearAllManagementData);
    }

    public void addManagementListener(ManagementListener listener) {
        managementListeners.add(listener);

        // Inform listeners of the current model
        if (currentProcessor != null) {
            listener.processorUpdated(currentProcessor);
        }
        clientInfoById.forEach((k, v) -> listener.clientUpdated(v));
    }

    public void removeManagementListener(ManagementListener listener) {
        managementListeners.remove(listener);
    }

    public void addInstanceListener(InstanceListener listener) {
        instanceListeners.add(listener);
    }

    public void removeInstanceListener(InstanceListener listener) {
        instanceListeners.remove(listener);
    }

    public ClientInfo getClientInfo(int clientId) {
        return clientInfoById.get(clientId);
    }

    public ClientInfo getCurrentClientInfo() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        ConnectionInfo connectionInfo = yamcsClient.getConnectionInfo();
        if (connectionInfo != null) {
            int currentClientId = connectionInfo.getClientId();
            return clientInfoById.get(currentClientId);
        }
        return null;
    }

    public static String getCurrentYamcsInstance() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        ConnectionInfo connectionInfo = yamcsClient.getConnectionInfo();
        if (connectionInfo != null && connectionInfo.hasInstance()) {
            return connectionInfo.getInstance().getName();
        }
        return null;
    }

    public ProcessorInfo getCurrentProcessorInfo() {
        return currentProcessor;
    }

    public List<ClientInfo> getClients() {
        return new ArrayList<>(clientInfoById.values());
    }

    public CompletableFuture<byte[]> createProcessorRequest(String yamcsInstance, CreateProcessorRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.post("/processors/" + yamcsInstance, request);
    }

    public CompletableFuture<byte[]> editProcessorRequest(String yamcsInstance, String processor,
            EditProcessorRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.patch("/processors/" + yamcsInstance + "/" + processor, request);
    }

    public CompletableFuture<byte[]> editClientRequest(int clientId, EditClientRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.patch("/clients/" + clientId, request);
    }

    public CompletableFuture<byte[]> fetchInstanceInformationRequest(String yamcsInstance) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.get("/instances/" + yamcsInstance + "?aggregate", null);
    }

    public CompletableFuture<byte[]> restartInstance(String yamcsInstance) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.patch("/instances/" + yamcsInstance + "?state=restarted", null);
    }

    public CompletableFuture<byte[]> fetchProcessors() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.get("/processors", null);
    }
}
