package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo.ClientState;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.ServiceState;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.WebSocketRegistrar;

/**
 * Provides access to aggregated state on yamcs management-type information.
 * <p>
 * There should be only one long-lived instance of this class, which goes down together with the
 * application (same lifecycle as {@link YamcsPlugin}). This catalogue deals with maintaining
 * correct state accross connection-reconnects, so listeners only need to register once.
 */
public class ManagementCatalogue implements StudioConnectionListener {

    private Set<ManagementListener> managementListeners = new CopyOnWriteArraySet<>();
    private Map<String, ProcessorInfo> processorInfoByName = new ConcurrentHashMap<>();
    private Map<Integer, ClientInfo> clientInfoById = new ConcurrentHashMap<>();

    // Redundant, but quickly accessible
    private int currentClientId = -1;

    public static ManagementCatalogue getInstance() {
        return YamcsPlugin.getDefault().getManagementCatalogue();
    }

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, WebSocketRegistrar webSocketClient) {
        webSocketClient.subscribeToManagementInfo();
    }

    @Override
    public void onStudioDisconnect() {
        // Clear everything, we'll get a fresh set upon connect
        clientInfoById.clear();
        processorInfoByName.clear();
        currentClientId = -1;
    }

    public void addManagementListener(ManagementListener listener) {
        managementListeners.add(listener);

        // Inform listeners of the current model
        processorInfoByName.forEach((k, v) -> listener.processorUpdated(v));
        clientInfoById.forEach((k, v) -> listener.clientUpdated(v));
    }

    public void processClientInfo(ClientInfo clientInfo) {
        if (clientInfo.getState() == ClientState.DISCONNECTED) {
            clientInfoById.remove(clientInfo.getId());
            if (clientInfo.getCurrentClient())
                currentClientId = -1;

            managementListeners.forEach(l -> l.clientDisconnected(clientInfo));
        } else {
            clientInfoById.put(clientInfo.getId(), clientInfo);
            if (clientInfo.getCurrentClient())
                currentClientId = clientInfo.getId();

            managementListeners.forEach(l -> l.clientUpdated(clientInfo));
        }
    }

    public void processProcessorInfo(ProcessorInfo processorInfo) {
        if (processorInfo.getState() == ServiceState.TERMINATED)
            processorInfoByName.remove(processorInfo.getName());
        else
            processorInfoByName.put(processorInfo.getName(), processorInfo);

        managementListeners.forEach(l -> l.processorUpdated(processorInfo));
    }

    public void processStatistics(Statistics stats) {
        managementListeners.forEach(l -> l.statisticsUpdated(stats));
    }

    public ProcessorInfo getProcessorInfo(String processorName) {
        return processorInfoByName.get(processorName);
    }

    public ClientInfo getClientInfo(int clientId) {
        return clientInfoById.get(clientId);
    }

    public ClientInfo getCurrentClientInfo() {
        return clientInfoById.get(currentClientId);
    }

    public ProcessorInfo getCurrentProcessorInfo() {
        ClientInfo ci = clientInfoById.get(currentClientId);
        return (ci != null) ? processorInfoByName.get(ci.getProcessorName()) : null;
    }

    public List<ProcessorInfo> getProcessors() {
        return new ArrayList<>(processorInfoByName.values());
    }

    public List<ClientInfo> getClients() {
        return new ArrayList<>(clientInfoById.values());
    }
}