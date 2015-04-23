package org.yamcs.studio.commanding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.yamcs.protobuf.Rest.RestArgumentType;
import org.yamcs.protobuf.Rest.RestCommandType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.client.YamcsPlugin;

/**
 * Hand-written ugly command parser. Follows some very simple logic: - removes all whitespace - puts
 * in one MetaCommand xtce
 */
public class CommandParser {

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    public static RestCommandType toCommand(String commandString) {
        if (commandString == null)
            return null;
        commandString = commandString.replaceAll("\\s", "");

        int lparen = commandString.indexOf('(');
        RestCommandType.Builder cmd = RestCommandType.newBuilder();

        String commandName = commandString.substring(0, lparen);
        NamedObjectId.Builder commandId = NamedObjectId.newBuilder();
        commandId.setNamespace(YamcsPlugin.getDefault().getMdbNamespace());
        commandId.setName(commandName);
        cmd.setId(commandId);

        String argString = commandString.substring(lparen + 1, commandString.length() - 1);
        String[] args = argString.split(",");
        for (String arg : args) {
            String[] kvp = arg.split(":");
            cmd.addArguments(RestArgumentType.newBuilder().setName(kvp[0]).setValue(kvp[1]));
        }

        String origin;
        try {
            origin = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            origin = "Unknown";
        }
        cmd.setSequenceNumber(cmdClientId.getAndIncrement());
        cmd.setOrigin(origin);
        return cmd.build();
    }

    public static void main(String... args) {
        String cmdString = "SWITCH_VOLTAGE_ON(vlotage_num:5)";
        RestCommandType mc = toCommand(cmdString);

        System.out.println("======");
        System.out.println("" + mc.getId());
        for (RestArgumentType arg : mc.getArgumentsList()) {
            System.out.println(" - " + arg.getName() + ":" + arg.getValue());
        }
    }
}
