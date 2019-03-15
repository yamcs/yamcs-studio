package org.yamcs.studio.script;

import java.util.Map;

import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.protobuf.Rest.IssueCommandRequest.Assignment;
import org.yamcs.studio.commanding.CommandParser;
import org.yamcs.studio.commanding.CommandParser.ParseResult;
import org.yamcs.studio.core.model.CommandingCatalogue;

public class Yamcs {

    /**
     * Sample use:
     *
     * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
     */
    public static void issueCommand(String commandText) {
        ParseResult parsed = CommandParser.parseCommand(commandText);
        IssueCommandRequest.Builder req = IssueCommandRequest.newBuilder();
        req.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        req.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());
        req.addAllAssignment(parsed.getAssignments());

        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.sendCommand("realtime", parsed.getQualifiedName(), req.build());
    }

    /**
     * Sample use:
     *
     * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
     */
    public static void issueCommand(String command, Map<String, Object> args) {
        IssueCommandRequest.Builder req = IssueCommandRequest.newBuilder();
        req.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        req.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());
        if (args != null) {
            args.forEach((k, v) -> {
                req.addAssignment(Assignment.newBuilder()
                        .setName(k).setValue(String.valueOf(v)));
            });
        }

        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.sendCommand("realtime", command, req.build());
    }
}
