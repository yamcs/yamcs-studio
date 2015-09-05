package org.yamcs.studio.core.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.WebSocketRegistrar;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.XtceDb;

import com.google.protobuf.MessageLite;

public class CommandingCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(CommandingCatalogue.class.getName());

    private AtomicInteger cmdClientId = new AtomicInteger(1);

    @Deprecated
    private XtceDb mdb;
    private Collection<MetaCommand> metaCommands = Collections.emptyList();
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

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, WebSocketRegistrar webSocketClient) {
        webSocketClient.sendMessage(new WebSocketRequest("management", "cmdhistory"));
        loadMetaCommands();
    }

    @Override
    public void onStudioDisconnect() {
        metaCommands = Collections.emptyList();
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
    }

    public Collection<MetaCommand> getMetaCommands() {
        return metaCommands;
    }

    private void loadMetaCommands() {
        log.fine("Fetching available commands");
        ConnectionManager.getInstance().getRestClient().dumpRawMdb(new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                RestDumpRawMdbResponse response = (RestDumpRawMdbResponse) responseMsg;
                try (ObjectInputStream oin = new ObjectInputStream(response.getRawMdb().newInput())) {
                    XtceDb newMdb = (XtceDb) oin.readObject();
                    Display.getDefault().asyncExec(() -> {
                        mdb = newMdb;
                        metaCommands = mdb.getMetaCommands();
                    });
                } catch (IOException | ClassNotFoundException e) {
                    log.log(Level.SEVERE, "Could not deserialize mdb", e);
                    Display.getDefault().asyncExec(() -> {
                        MessageDialog.openError(Display.getDefault().getActiveShell(),
                                "Incompatible Yamcs Server", "Could not interpret Mission Database. "
                                        + "This usually happens when Yamcs Studio is not, or no longer, "
                                        + "compatible with Yamcs Server.");
                    });
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs commands", e);
            }
        });
    }

    @Deprecated
    public XtceDb getMdb() {
        return mdb;
    }

    public String getCommandOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }
}
