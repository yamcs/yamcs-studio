package org.yamcs.studio.ui.commanding;

import org.yamcs.protobuf.Commanding.ArgumentType;
import org.yamcs.protobuf.Commanding.CommandType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.CommandingCatalogue;

/**
 * Hand-written ugly command parser. Follows some very simple logic:
 *
 * <ul>
 * <li>removes all whitespace
 * <li>puts everything in one MetaCommand xtce
 * </ul>
 */
public class CommandParser {

    public static CommandType toCommand(String commandString) {
        return toCommand(commandString, CommandingCatalogue.getInstance());
    }

    // Extracted out for unit tests
    static CommandType toCommand(String commandString, CommandingCatalogue commandingCatalogue) {
        if (commandString == null)
            return null;

        commandString = commandString.trim();

        int lparen = commandString.indexOf('(');
        CommandType.Builder cmd = CommandType.newBuilder();

        String commandName = commandString.substring(0, lparen).trim();
        NamedObjectId.Builder commandId = NamedObjectId.newBuilder();
        commandId.setName(commandName.trim());
        cmd.setId(commandId);
        cmd.setSequenceNumber(commandingCatalogue.getNextCommandClientId());
        cmd.setOrigin(commandingCatalogue.getCommandOrigin());

        String argString = commandString.substring(lparen + 1, commandString.length() - 1);
        String[] args = argString.split(",");
        for (String arg : args) {
            arg = arg.trim();
            if (!arg.isEmpty()) {
                String[] kvp = arg.split(":");
                String name = kvp[0].trim();
                String value = kvp[1].trim();
                if (value.length() >= 2) {
                    if ((value.startsWith("'") && value.endsWith("'")) ||
                            value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                        value = value.replace("\\\"", "\"").replace("\\'", "'");
                    }
                }
                cmd.addArguments(ArgumentType.newBuilder().setName(name).setValue(value));
            }
        }

        return cmd.build();
    }
}
