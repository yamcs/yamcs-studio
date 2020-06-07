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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.client.Page;
import org.yamcs.client.WebSocketClientCallback;
import org.yamcs.client.WebSocketRequest;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.archive.ArchiveClient.ListOptions;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandId;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueEvent;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.ConnectionInfo;
import org.yamcs.protobuf.EditQueueEntryRequest;
import org.yamcs.protobuf.EditQueueRequest;
import org.yamcs.protobuf.IssueCommandRequest;
import org.yamcs.protobuf.IssueCommandRequest.Assignment;
import org.yamcs.protobuf.IssueCommandResponse;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.ListCommandsResponse;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.UpdateCommandHistoryRequest;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.ClearanceListener;
import org.yamcs.studio.core.client.YamcsStudioClient;

import com.google.protobuf.InvalidProtocolBufferException;

public class CommandingCatalogue implements Catalogue, WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(CommandingCatalogue.class.getName());

    private AtomicInteger cmdClientId = new AtomicInteger(1);
    private List<CommandInfo> metaCommands = Collections.emptyList();
    private Map<String, CommandInfo> commandsByQualifiedName = new LinkedHashMap<>();
    private SignificanceLevelType clearance;

    private Set<ClearanceListener> clearanceListeners = new CopyOnWriteArraySet<>();
    private Set<CommandHistoryListener> cmdhistListeners = new CopyOnWriteArraySet<>();

    public static CommandingCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(CommandingCatalogue.class);
    }

    public int getNextCommandClientId() {
        return cmdClientId.incrementAndGet();
    }

    public void addCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.add(listener);
    }

    public void removeCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.remove(listener);
    }

    public SignificanceLevelType getClearance() {
        return clearance;
    }

    public void addClearanceListener(ClearanceListener clearanceListener) {
        clearanceListeners.add(clearanceListener);
    }

    public void removeClearanceListener(ClearanceListener clearanceListener) {
        clearanceListeners.remove(clearanceListener);
    }

    @Override
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("cmdhistory", "subscribe"), this);
        ConnectionInfo connectionInfo = yamcsClient.getConnectionInfo();
        boolean clearanceEnabled = connectionInfo.getProcessor().getCheckCommandClearance();
        clearance = connectionInfo.hasClearance() ? connectionInfo.getClearance() : null;
        clearanceListeners.forEach(l -> l.clearanceChanged(clearanceEnabled, clearance));
        initialiseState();
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasConnectionInfo()) {
            ConnectionInfo connectionInfo = msg.getConnectionInfo();
            boolean clearanceEnabled = connectionInfo.getProcessor().getCheckCommandClearance();
            if (connectionInfo.hasClearance()) {
                clearanceListeners.forEach(l -> l.clearanceChanged(clearanceEnabled, connectionInfo.getClearance()));
            } else {
                clearanceListeners.forEach(l -> l.clearanceChanged(clearanceEnabled, null));
            }
        }

        if (msg.hasCommand()) {
            CommandHistoryEntry cmdhistEntry = msg.getCommand();
            cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
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
        clearanceListeners.forEach(l -> l.clearanceChanged(false, null));
    }

    private void initialiseState() {
        Job job = Job.create("Loading commands", monitor -> {
            log.fine("Fetching available commands");
            MissionDatabaseClient mdb = YamcsPlugin.getMissionDatabaseClient();
            List<CommandInfo> commands = new ArrayList<>();

            if (mdb != null) {
                try {
                    Page<CommandInfo> page = mdb.listCommands(MissionDatabaseClient.ListOptions.limit(200)).get();
                    page.iterator().forEachRemaining(commands::add);
                    while (page.hasNextPage()) {
                        page = page.getNextPage().get();
                        page.iterator().forEachRemaining(commands::add);
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
        commandsByQualifiedName.clear();
    }

    public List<CommandInfo> getMetaCommands() {
        return metaCommands;
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    public CompletableFuture<IssueCommandResponse> sendCommand(String processor, String commandName, IssueCommandRequest request) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        ProcessorClient processorClient = YamcsPlugin.getYamcsClient().createProcessorClient(instance, processor);
        CommandBuilder builder  = processorClient.prepareCommand(commandName);
        if (request.hasComment()) {
            builder.
        }
        for (Assignment assignment : request.getAssignmentList()) {
            builder.withArgument(assignment.getName(), assignment.getValue());
        }
        return builder.issue();
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
}
