package org.yamcs.studio.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.util.DisplayUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.CommandParser;

import com.google.protobuf.MessageLite;

/**
 * Sample use:
 *
 * importPackage(Packages.org.yamcs.studio.script);
 * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
 */
public class Yamcs {

    private static final Logger log = Logger.getLogger(Yamcs.class.getName());

    public static void issueCommand(String text) {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient == null) {
            showErrorDialog("Could not issue command, client is disconnected from Yamcs server");
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
                    showErrorDialog(e.getMessage());
                });
            }
        });
    }

    private static void showErrorDialog(String message) {
        MessageDialog.openError(DisplayUtils.getDefaultShell(), "Could not issue command", message);
    }
}
