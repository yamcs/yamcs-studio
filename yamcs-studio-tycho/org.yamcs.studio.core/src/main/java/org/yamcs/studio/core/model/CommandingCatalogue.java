package org.yamcs.studio.core.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Rest.ListCommandsResponse;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.WebSocketRegistrar;

import com.google.protobuf.MessageLite;

public class CommandingCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(CommandingCatalogue.class.getName());

    private AtomicInteger cmdClientId = new AtomicInteger(1);

    private List<CommandInfo> metaCommands = Collections.emptyList();

    private Set<CommandHistoryListener> cmdhistListeners = new CopyOnWriteArraySet<>();

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

    @Override
    public void onStudioConnect() {
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("cmdhistory", "subscribe"));
        loadMetaCommands();
    }

    @Override
    public void onStudioDisconnect() {
        metaCommands = Collections.emptyList();
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
    }

    public List<CommandInfo> getMetaCommands() {
        return metaCommands;
    }

    public CommandInfo getCommandInfo(String qualifiedName) {
        return commandsByQualifiedName.get(qualifiedName);
    }

    public synchronized void processMetaCommands(List<CommandInfo> metaCommands) {
        this.metaCommands = new ArrayList<>(metaCommands);
        this.metaCommands.sort((p1, p2) -> {
            return p1.getDescription().getQualifiedName().compareTo(p2.getDescription().getQualifiedName());
        });

        for (CommandInfo cmd : this.metaCommands) {
            commandsByQualifiedName.put(cmd.getDescription().getQualifiedName(), cmd);
        }
    }

    private void loadMetaCommands() {
        log.fine("Fetching available commands");
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        restClient.listCommands(new ResponseHandler() {
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
