package org.yamcs.studio.commanding;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.IssueCommandRequest.Assignment;

/**
 * Hand-written ugly command parser. Follows some very simple logic:
 *
 * <ul>
 * <li>removes all whitespace
 * <li>puts everything in one MetaCommand xtce
 * </ul>
 */
public class CommandParser {

    public static ParseResult parseCommand(String commandString) {
        if (commandString == null) {
            return null;
        }

        commandString = commandString.trim();

        int lparen = commandString.indexOf('(');
        ParseResult result = new ParseResult();

        String commandName = commandString.substring(0, lparen).trim();
        result.qualifiedName = commandName.trim();

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
                result.assignments.add(Assignment.newBuilder().setName(name).setValue(value).build());
            }
        }

        return result;
    }

    public static class ParseResult {
        private String qualifiedName;
        private List<Assignment> assignments = new ArrayList<>();

        public String getQualifiedName() {
            return qualifiedName;
        }

        public List<Assignment> getAssignments() {
            return assignments;
        }
    }
}
