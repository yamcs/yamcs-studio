package org.yamcs.studio.ui.commanding;

import org.yamcs.protobuf.Rest.RestArgumentType;
import org.yamcs.protobuf.Rest.RestCommandType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;

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
        cmd.setSequenceNumber(YamcsPlugin.getNextCommandClientId());
        cmd.setOrigin(YamcsPlugin.getDefault().getOrigin());

        String argString = commandString.substring(lparen + 1, commandString.length() - 1);
        String[] args = argString.split(",");
        for (String arg : args) {
            String[] kvp = arg.split(":");
            cmd.addArguments(RestArgumentType.newBuilder().setName(kvp[0]).setValue(kvp[1]));
        }

        return cmd.build();
    }
}
