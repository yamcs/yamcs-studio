package org.yamcs.studio.core.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueEvent;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.protobuf.Rest.IssueCommandResponse;
import org.yamcs.protobuf.Rest.ListCommandsResponse;
import org.yamcs.protobuf.Rest.PatchCommandQueueEntryRequest;
import org.yamcs.protobuf.Rest.PatchCommandQueueRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.WebSocketRegistrar;

import com.google.protobuf.MessageLite;

public class CommandingCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(CommandingCatalogue.class.getName());

    private AtomicInteger cmdClientId = new AtomicInteger(1);
    private List<CommandInfo> metaCommands = Collections.emptyList();
    private Map<String, CommandQueueInfo> queuesByName = new ConcurrentHashMap<>();

    private Set<CommandHistoryListener> cmdhistListeners = new CopyOnWriteArraySet<>();
    private Set<CommandQueueListener> queueListeners = new CopyOnWriteArraySet<>();

    // Indexes
    private Map<String, CommandInfo> commandsByQualifiedName = new LinkedHashMap<>();

    public static CommandingCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(CommandingCatalogue.class);
    }

    public int getNextCommandClientId() {
        return cmdClientId.incrementAndGet();
    }

    public void addCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.add(listener);
    }

    public void addCommandQueueListener(CommandQueueListener listener) {
        queueListeners.add(listener);

        // Inform listener of current model
        queuesByName.forEach((k, v) -> listener.updateQueue(v));
    }

    @Override
    public void onStudioConnect() {
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("cmdhistory", "subscribe"));
        webSocketClient.sendMessage(new WebSocketRequest("cqueues", "subscribe"));
        loadMetaCommands();
    }

    @Override
    public void onStudioDisconnect() {
        metaCommands = Collections.emptyList();
        queuesByName.clear();
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
    }

    public void processCommandQueueInfo(CommandQueueInfo queueInfo) {
        queuesByName.put(queueInfo.getName(), queueInfo);
        queueListeners.forEach(l -> l.updateQueue(queueInfo));
    }

    public void processCommandQueueEvent(CommandQueueEvent queueEvent) {
        switch (queueEvent.getType()) {
        case COMMAND_ADDED:
            queueListeners.forEach(l -> l.commandAdded(queueEvent.getData()));
            break;
        case COMMAND_REJECTED:
            queueListeners.forEach(l -> l.commandRejected(queueEvent.getData()));
            break;
        case COMMAND_SENT:
            queueListeners.forEach(l -> l.commandSent(queueEvent.getData()));
            break;
        default:
            log.log(Level.SEVERE, "Unsupported queue event type " + queueEvent.getType());
        }
    }

    public List<CommandInfo> getMetaCommands() {
        return metaCommands;
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    public void sendCommand(String processor, String commandName, IssueCommandRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String instance = connectionManager.getYamcsInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            restClient.post("/processors/" + instance + "/" + processor + "/commands" + commandName, request, IssueCommandResponse.newBuilder(),
                    responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void editQueue(CommandQueueInfo queue, PatchCommandQueueRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String instance = connectionManager.getYamcsInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            restClient.patch("/processors/" + instance + "/" + queue.getProcessorName() + "/cqueues/" + queue.getName(),
                    request, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void editQueuedCommand(CommandQueueEntry entry, PatchCommandQueueEntryRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String instance = connectionManager.getYamcsInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            restClient.patch(
                    "/processors/" + instance + "/" + entry.getProcessorName() + "/cqueues/" + entry.getQueueName() + "/entries/" + entry.getUuid(),
                    request, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public synchronized void processMetaCommands(List<CommandInfo> metaCommands) {
        this.metaCommands = new ArrayList<>(metaCommands);
        this.metaCommands.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        for (CommandInfo cmd : this.metaCommands) {
            commandsByQualifiedName.put(cmd.getQualifiedName(), cmd);
        }
    }

    private void loadMetaCommands() {
        log.fine("Fetching available commands");
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        String instance = connectionManager.getYamcsInstance();
        restClient.get("/mdb/" + instance + "/commands", null, ListCommandsResponse.newBuilder(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                ListCommandsResponse response = (ListCommandsResponse) responseMsg;
                processMetaCommands(response.getCommandList());
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs commands", e);
            }
        });
    }

    public String getCommandOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }
}
