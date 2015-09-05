package org.yamcs.studio.ui.commanding;

import org.yamcs.protobuf.Rest.RestArgumentType;
import org.yamcs.protobuf.Rest.RestCommandType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.CommandingCatalogue;

/**
 * Hand-written ugly command parser. Follows some very simple logic: - removes all whitespace - puts
 * in one MetaCommand xtce
 */
public class CommandParser {

    public static RestCommandType toCommand(String commandString) {
        if (commandString == null)
            return null;
        commandString = commandString.replaceAll("\\s", "");

        int lparen = commandString.indexOf('(');
        RestCommandType.Builder cmd = RestCommandType.newBuilder();

        String commandName = commandString.substring(0, lparen);
        NamedObjectId.Builder commandId = NamedObjectId.newBuilder();
        commandId.setName(commandName.trim());
        cmd.setId(commandId);
        cmd.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        cmd.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());

        String argString = commandString.substring(lparen + 1, commandString.length() - 1);
        String[] args = argString.split(",");
        for (String arg : args) {
            String[] kvp = arg.split(":");
            cmd.addArguments(RestArgumentType.newBuilder().setName(kvp[0]).setValue(kvp[1]));
        }

        return cmd.build();
    }
}
