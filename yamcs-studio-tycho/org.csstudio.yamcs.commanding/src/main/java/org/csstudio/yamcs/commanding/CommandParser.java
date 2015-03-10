package org.csstudio.yamcs.commanding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.RestArgumentType;
import org.yamcs.protostuff.RestCommandType;

/**
 * Hand-written ugly command parser. Follows some very simple logic:
 * - removes all whitespace
 * - puts in one MetaCommand xtce
 */
public class CommandParser {
    
    public static RestCommandType toCommand(String commandString) {
        if (commandString == null) return null;
        commandString = commandString.replaceAll("\\s", "");
        
        int lparen = commandString.indexOf('(');
        RestCommandType cmd = new RestCommandType();
        
        String commandName = commandString.substring(0, lparen);
        NamedObjectId commandId = new NamedObjectId();
        if (!commandName.startsWith("/")) {
            commandId.setNamespace("MDB:OPS Name");
        }
        commandId.setName(commandName);
        cmd.setId(commandId);
        
        try {
            cmd.setOrigin(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            cmd.setOrigin("Unknown");
        }
        
        String argString = commandString.substring(lparen + 1, commandString.length() - 1);
        
        cmd.setArgumentsList(new ArrayList<RestArgumentType>());
        String[] args = argString.split(",");
        for (String arg : args) {
            RestArgumentType argumentType = new RestArgumentType();
            String[] kvp = arg.split(":");
            argumentType.setName(kvp[0]);
            argumentType.setValue(kvp[1]);
            cmd.getArgumentsList().add(argumentType);
        }
        
        return cmd;
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
