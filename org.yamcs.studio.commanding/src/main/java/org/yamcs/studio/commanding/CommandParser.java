/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
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

        var lparen = commandString.indexOf('(');
        var result = new ParseResult();

        var commandName = commandString.substring(0, lparen).trim();
        result.qualifiedName = commandName.trim();

        var argString = commandString.substring(lparen + 1, commandString.length() - 1);
        var args = argString.split(",");
        for (String arg : args) {
            arg = arg.trim();
            if (!arg.isEmpty()) {
                var kvp = arg.split(":");
                var name = kvp[0].trim();
                var value = kvp[1].trim();
                if (value.length() >= 2) {
                    if ((value.startsWith("'") && value.endsWith("'"))
                            || value.startsWith("\"") && value.endsWith("\"")) {
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
