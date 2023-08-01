package org.yamcs.studio.commanding.stack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CommandStackParser {

    public static CommandStack parse(Path file) throws CommandStackParseException {
        var filename = file.getFileName().toString();
        if (filename.toLowerCase().endsWith(".xml")) {
            return parseXmlCommandStack(file);
        } else if (filename.toLowerCase().endsWith(".ycs")) {
            return parseYcsCommandStack(file);
        } else {
            throw new CommandStackParseException("Unsupported file format");
        }
    }

    private static CommandStack parseYcsCommandStack(Path file) {
        var stack = new CommandStack();

        var gson = new Gson();
        JsonObject stackObject;
        try (var reader = Files.newBufferedReader(file)) {
            stackObject = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new CommandStackParseException(e);
        }

        if (stackObject.has("advancement")) {
            var advancementObject = stackObject.get("advancement").getAsJsonObject();
            if (advancementObject.has("wait")) {
                var wait = advancementObject.get("wait").getAsInt();
                if (wait >= 0) {
                    stack.setWaitTime(wait);
                }
            }
        }

        if (stackObject.has("commands")) {
            for (var commandEl : stackObject.getAsJsonArray("commands")) {
                var commandObject = commandEl.getAsJsonObject();

                var name = commandObject.get("name").getAsString();

                CommandInfo mdbInfo;
                if (commandObject.has("namespace")) {
                    var namespace = commandObject.get("namespace").getAsString();
                    mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(namespace, name);
                    if (mdbInfo == null) {
                        throw new CommandStackParseException(
                                "Command " + name + " (" + namespace + ") does not exist in MDB");
                    }
                } else {
                    mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(name);
                    if (mdbInfo == null) {
                        throw new CommandStackParseException(
                                "Command " + name + " does not exist in MDB");
                    }
                }

                var command = new StackedCommand();
                command.setMetaCommand(mdbInfo);
                if (commandObject.has("comment")) {
                    var comment = commandObject.get("comment").getAsString();
                    command.setComment(comment);
                }
                if (commandObject.has("advancement")) {
                    var advancementObject = commandObject.get("advancement").getAsJsonObject();
                    if (advancementObject.has("wait")) {
                        var wait = advancementObject.get("wait").getAsInt();
                        if (wait >= 0) {
                            command.setWaitTime(wait);
                        }
                    }
                }
                if (commandObject.has("arguments")) {
                    var argumentsArray = commandObject.get("arguments").getAsJsonArray();
                    for (var argumentEl : argumentsArray) {
                        var argumentObject = argumentEl.getAsJsonObject();

                        var argName = argumentObject.get("name").getAsString();
                        var argValue = argumentObject.get("value");
                        var argInfo = getArgumentFromYamcs(mdbInfo, argName);
                        if (argInfo == null) {
                            throw new CommandStackParseException(
                                    "Argument " + argName + " does not exist in MDB for command " + name);
                        }
                        if (argValue.isJsonNull()) {
                            command.addAssignment(argInfo, null);
                        } else if (argValue.isJsonPrimitive()) {
                            command.addAssignment(argInfo, argValue.getAsString());
                        } else if (argValue.isJsonArray()) {
                            command.addAssignment(argInfo, argValue.getAsJsonArray().toString());
                        } else if (argValue.isJsonObject()) {
                            command.addAssignment(argInfo, argValue.getAsJsonObject().toString());
                        } else {
                            throw new CommandStackParseException("Unexpected value: " + argValue);
                        }
                    }
                }

                stack.addCommand(command);
            }
        }

        return stack;
    }

    private static CommandStack parseXmlCommandStack(Path file) {
        var stack = new CommandStack();
        try {
            var dbFactory = DocumentBuilderFactory.newInstance();
            var dBuilder = dbFactory.newDocumentBuilder();
            var doc = dBuilder.parse(file.toFile());
            doc.getDocumentElement().normalize();

            var nodes = doc.getElementsByTagName("command");

            for (var i = 0; i < nodes.getLength(); i++) {
                var node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var commandElement = (Element) node;
                    var qname = commandElement.getAttribute("qualifiedName");

                    var mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(qname);
                    if (mdbInfo == null) {
                        throw new CommandStackParseException(
                                "Command " + qname + " does not exist in MDB.");
                    }

                    var command = new StackedCommand();
                    command.setMetaCommand(mdbInfo);
                    if (commandElement.hasAttribute("comment")) {
                        var comment = commandElement.getAttribute("comment");
                        command.setComment(comment);
                    }
                    if (commandElement.hasAttribute("delayMs")) {
                        var delay = Integer.parseInt(commandElement.getAttribute("delayMs"));
                        if (delay >= 0) {
                            command.setWaitTime(delay);
                        }
                    }

                    var argNodes = commandElement.getElementsByTagName("commandArgument");
                    for (var j = 0; j < argNodes.getLength(); j++) {
                        var argNode = argNodes.item(j);
                        if (argNode.getNodeType() == Node.ELEMENT_NODE) {
                            var argElement = (Element) argNode;
                            var argName = argElement.getAttribute("argumentName");
                            var argValue = argElement.getAttribute("argumentValue");
                            var argInfo = getArgumentFromYamcs(mdbInfo, argName);
                            if (argInfo == null) {
                                throw new CommandStackParseException(
                                        "In command " + qname + ", argument " + argName + " does not exist in MDB.");
                            }
                            command.addAssignment(argInfo, argValue);
                        }
                    }
                    stack.addCommand(command);
                }
            }

            return stack;
        } catch (Exception e) {
            throw new CommandStackParseException(e);
        }
    }

    private static ArgumentInfo getArgumentFromYamcs(CommandInfo mc, String argumentName) {
        // look for argument in the command
        for (var a : mc.getArgumentList()) {
            if (a.getName().equals(argumentName)) {
                return a;
            }
        }

        // else look in the parent command
        if (mc.getBaseCommand() != mc) {
            return getArgumentFromYamcs(mc.getBaseCommand(), argumentName);
        }

        // else, argument is not found...
        return null;
    }
}
