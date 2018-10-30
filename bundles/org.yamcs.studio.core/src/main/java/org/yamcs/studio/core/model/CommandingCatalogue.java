package org.yamcs.studio.core.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandId;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueEvent;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.ListCommandsResponse;
import org.yamcs.protobuf.Rest.EditCommandQueueEntryRequest;
import org.yamcs.protobuf.Rest.EditCommandQueueRequest;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.protobuf.Rest.UpdateCommandHistoryRequest;
import org.yamcs.protobuf.Rest.UpdateCommandHistoryRequest.KeyValue;
import org.yamcs.protobuf.Web.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;

import com.google.protobuf.InvalidProtocolBufferException;

public class CommandingCatalogue implements Catalogue, WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(CommandingCatalogue.class.getName());

    private AtomicInteger cmdClientId = new AtomicInteger(1);
    private List<CommandInfo> metaCommands = Collections.emptyList();
    private Map<String, CommandQueueInfo> queuesByName = new ConcurrentHashMap<>();
    // Indexes
    private Map<String, CommandInfo> commandsByQualifiedName = new LinkedHashMap<>();

    private Set<CommandHistoryListener> cmdhistListeners = new CopyOnWriteArraySet<>();
    private Set<CommandQueueListener> queueListeners = new CopyOnWriteArraySet<>();

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

    public void removeCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.remove(listener);
    }

    public void removeCommandQueueListener(CommandQueueListener listener) {
        queueListeners.remove(listener);
    }

    @Override
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("cmdhistory", "subscribe"), this);
        yamcsClient.subscribe(new WebSocketRequest("cqueues", "subscribe"), this);
        initialiseState();
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasCommand()) {
            CommandHistoryEntry cmdhistEntry = msg.getCommand();
            cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
        }

        if (msg.hasCommandQueueEvent()) {
            CommandQueueEvent queueEvent = msg.getCommandQueueEvent();
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

        if (msg.hasCommandQueueInfo()) {
            CommandQueueInfo queueInfo = msg.getCommandQueueInfo();
            queuesByName.put(queueInfo.getName(), queueInfo);
            queueListeners.forEach(l -> l.updateQueue(queueInfo));
        }
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearState();
        initialiseState();
    }

    @Override
    public void onYamcsDisconnected() {
        clearState();
    }

    private void initialiseState() {
        Job job = Job.create("Loading commands", monitor -> {
            log.fine("Fetching available commands");
            YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            int pageSize = 200;
            List<CommandInfo> commands = new ArrayList<>();

            String next = null;
            while (true) {
                try {
                    String url = "/mdb/" + instance + "/commands?details&limit=" + pageSize;
                    if (next != null) {
                        url += "&next=" + next;
                    }
                    byte[] data = yamcsClient.get(url, null).get();
                    try {
                        ListCommandsResponse response = ListCommandsResponse.parseFrom(data);
                        commands.addAll(response.getCommandList());
                        if (response.hasContinuationToken()) {
                            next = response.getContinuationToken();
                        } else {
                            break;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        log.log(Level.SEVERE, "Failed to decode server response", e);
                        break;
                    }
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    log.log(Level.SEVERE, "Exception while loading commands: " + cause.getMessage(), cause);
                    return Status.OK_STATUS;
                }
            }

            processMetaCommands(commands);
            return Status.OK_STATUS;
        });
        job.setPriority(Job.LONG);
        job.schedule(1000L);
    }

    private void clearState() {
        metaCommands = Collections.emptyList();
        queuesByName.clear();
        commandsByQualifiedName.clear();
    }

    public List<CommandInfo> getMetaCommands() {
        return metaCommands;
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    public CompletableFuture<byte[]> sendCommand(String processor, String commandName, IssueCommandRequest request) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.post("/processors/" + instance + "/" + processor + "/commands" + commandName, request);
    }

    public CompletableFuture<byte[]> editQueue(CommandQueueInfo queue, EditCommandQueueRequest request) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.patch(
                "/processors/" + instance + "/" + queue.getProcessorName() + "/cqueues/" + queue.getName(), request);
    }

    public CompletableFuture<byte[]> editQueuedCommand(CommandQueueEntry entry, EditCommandQueueEntryRequest request) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.patch(
                "/processors/" + instance + "/" + entry.getProcessorName() + "/cqueues/" + entry.getQueueName()
                        + "/entries/" + entry.getUuid(),
                request);
    }

    public synchronized void processMetaCommands(List<CommandInfo> metaCommands) {
        log.info(String.format("Loaded %d commands", metaCommands.size()));
        this.metaCommands = new ArrayList<>(metaCommands);
        this.metaCommands.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        for (CommandInfo cmd : this.metaCommands) {
            commandsByQualifiedName.put(cmd.getQualifiedName(), cmd);
        }
    }

    public String getCommandOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    public CompletableFuture<byte[]> updateCommandComment(String processor, CommandId cmdId, String newComment) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();

        KeyValue keyValue = KeyValue.newBuilder().setKey("Comment").setValue(newComment).build();
        UpdateCommandHistoryRequest request = UpdateCommandHistoryRequest.newBuilder().setCmdId(cmdId)
                .addHistoryEntry(keyValue).build();

        return yamcsClient.post(
                "/processors/" + instance + "/" + processor + "/commandhistory" + cmdId.getCommandName(),
                request);
    }
}
