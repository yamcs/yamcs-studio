package org.yamcs.studio.opibuilder.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.scriptUtil.ConsoleUtil;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
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
public class Yamcs {

    private static final Logger log = Logger.getLogger(Yamcs.class.getName());

    public static void issueCommand(String text) {
        RestClient client = YamcsPlugin.getDefault().getRestClient();
        if (client == null)
        {
            ConsoleUtil.writeError("Could not send command, client is disonnected from Yamcs server");
            return;
        }

        RestSendCommandRequest req = RestSendCommandRequest.newBuilder()
                .addCommands(CommandParser.toCommand(text)).build();
        client.sendCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                if (response instanceof RestExceptionMessage) {
                    Display.getDefault().asyncExec(() -> {
                        RestExceptionMessage exc = (RestExceptionMessage) response;
                        ConsoleUtil.writeError("[" + exc.getType() + "] " + exc.getMsg());
                    });
                }
                log.fine(String.format("Sent command %s", req));
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not send command", e);
                ConsoleUtil.writeError("Could not send command");
            }
        });
    }
}
