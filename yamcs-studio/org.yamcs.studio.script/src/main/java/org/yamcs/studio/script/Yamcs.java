package org.yamcs.studio.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.util.DisplayUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.commanding.CommandParser;
import org.yamcs.studio.ui.commanding.CommandParser.ParseResult;

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
        ParseResult parsed = CommandParser.parseCommand(text);
        IssueCommandRequest.Builder req = IssueCommandRequest.newBuilder();
        req.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        req.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());
        req.addAllAssignment(parsed.getAssignments());

        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.sendCommand("realtime", parsed.getQualifiedName(), req.build(), new ResponseHandler() {
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
