package org.yamcs.studio.script;

import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.ui.commanding.CommandParser;
import org.yamcs.studio.ui.commanding.CommandParser.ParseResult;

/**
 * Sample use:
 *
 * importPackage(Packages.org.yamcs.studio.script);
 * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
 */
public class Yamcs {

    public static void issueCommand(String text) {
        ParseResult parsed = CommandParser.parseCommand(text);
        IssueCommandRequest.Builder req = IssueCommandRequest.newBuilder();
        req.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        req.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());
        req.addAllAssignment(parsed.getAssignments());

        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.sendCommand("realtime", parsed.getQualifiedName(), req.build());
    }
}
