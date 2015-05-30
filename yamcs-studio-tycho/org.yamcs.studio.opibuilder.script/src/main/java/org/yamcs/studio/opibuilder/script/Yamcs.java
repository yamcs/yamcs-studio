package org.yamcs.studio.opibuilder.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.scriptUtil.ConsoleUtil;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.CommandParser;

import com.google.protobuf.MessageLite;

/**
 * Sample use:
 *
 * importPackage(Packages.org.yamcs.studio.opibuilder.script);
 * Yamcs.issueCommand('SIMULATOR_SWITCH_VOLTAGE_ON(voltage_num: 1)');
 */
public class Yamcs implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(Yamcs.class.getName());
    private RestClient restClient;

    private static Yamcs instance = new Yamcs();

    private Yamcs()
    {
        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    public Yamcs getInstance()
    {
        return instance;
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        this.restClient = restclient;
    }

    @Override
    public void onStudioDisconnect() {
        restClient = null;
    }

    public void issueCommand(String text) {

        if (restClient == null)
        {
            ConsoleUtil.writeError("Could not send command, client is disonnected from Yamcs server");
            return;
        }

        RestSendCommandRequest req = RestSendCommandRequest.newBuilder()
                .addCommands(CommandParser.toCommand(text)).build();
        restClient.sendCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                log.fine(String.format("Sent command %s", req));
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not send command", e);
                Display.getDefault().asyncExec(() -> {
                    ConsoleUtil.writeError("Could not send command " + e.getMessage());
                });
            }
        });
    }
}
