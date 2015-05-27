package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.RestClient;

public abstract class AbstractRestHandler extends AbstractHandler implements StudioConnectionListener {
    protected RestClient restClient;
    private static final Logger log = Logger.getLogger(AbstractRestHandler.class.getName());

    public AbstractRestHandler()
    {
        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        this.restClient = restclient;
    }

    @Override
    public void onStudioDisconnect() {
        if (restClient == null)
            return;
        restClient.shutdown();
        restClient = null;
    }

    protected boolean checkRestClient(ExecutionEvent event, String action)
    {
        if (restClient == null)
        {
            String error = "Could not " + action + ", client disconnected from Yamcs server";
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    error);
            log.log(Level.SEVERE, error);
            return false;
        }
        return true;
    }
}
