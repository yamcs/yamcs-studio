/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString;
import org.yamcs.client.Acknowledgment;
import org.yamcs.client.Command;
import org.yamcs.client.Helpers;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryRecord;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Keep track of the lifecycle of a stacked command.
 */
public class StackedCommand {

    private static final char[] HEXCHARS = "0123456789abcdef".toCharArray();

    public enum StackedState {
        DISARMED("Disarmed"),
        ARMED("Armed"),
        ISSUED("Issued"),
        SKIPPED("Skipped"),
        REJECTED("Rejected");

        private String text;

        StackedState(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private int delayMs = 0;
    private CommandInfo meta;
    private Map<ArgumentInfo, String> assignments = new HashMap<>();
    private Map<String, Value> extra = new HashMap<>();

    private StackedState state = StackedState.DISARMED;

    private String comment;

    private Command execution;

    public StackedCommand() {
    }

    public StackedCommand(StackedCommand original) {
        meta = original.getMetaCommand();
        comment = original.getComment();
        assignments.putAll(original.getAssignments());
        extra.putAll(original.getExtra());
        delayMs = original.getDelayMs();
    }

    public StackedCommand(CommandHistoryRecord rec) {
        meta = YamcsPlugin.getMissionDatabase().getCommandInfo(rec.getCommand().getName());
        comment = rec.getTextForColumn("Comment", false);
        for (var assignment : rec.getCommand().getAssignments()) {
            if (assignment.getUserInput()) {
                var argument = assignment.getName();
                var value = Helpers.parseValue(assignment.getValue());
                for (var argInfo : meta.getArgumentList()) {
                    if (argInfo.getName().equals(argument)) {
                        if (value instanceof byte[]) {
                            assignments.put(argInfo, ": 0x" + toHex((byte[]) value));
                        } else {
                            assignments.put(argInfo, value.toString());
                        }
                        break;
                    }
                }
            }
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXCHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEXCHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void resetExecutionState() {
        state = StackedState.DISARMED;
        execution = null;
    }

    public void updateExecutionState(Command command) {
        execution = command;
    }

    public StyledString toStyledString(CommandStackView styleProvider) {
        var identifierStyler = styleProvider != null ? styleProvider.getIdentifierStyler(this) : null;
        var bracketStyler = styleProvider != null ? styleProvider.getBracketStyler(this) : null;
        var argNameStyler = styleProvider != null ? styleProvider.getArgNameStyler(this) : null;
        var errorStyler = styleProvider != null ? styleProvider.getErrorStyler(this) : null;
        var numberStyler = styleProvider != null ? styleProvider.getNumberStyler(this) : null;

        var str = new StyledString();

        var preferredNamespace = CommandingPlugin.getDefault().getPreferredNamespace();
        var displayedName = meta.getQualifiedName();
        if (preferredNamespace != null) {
            for (var alias : meta.getAliasList()) {
                if (alias.getNamespace().equals(preferredNamespace)) {
                    displayedName = alias.getName();
                    break;
                }
            }
        }
        str.append(displayedName, identifierStyler);

        str.append("(", bracketStyler);
        var first = true;
        for (var arg : getEffectiveAssignments()) {
            var value = getAssignedStringValue(arg.getArgumentInfo());

            if (value == null && arg.getArgumentInfo().hasInitialValue()) {
                continue;
            }

            if (!arg.isEditable()) {
                continue;
            }

            if (arg.getArgumentInfo().hasInitialValue() && !isDefaultChanged(arg.getArgumentInfo())) {
                continue;
            }

            if (!first) {
                str.append("\n, ", bracketStyler);
            }
            first = false;
            str.append(arg.getName() + ": ", argNameStyler);

            if (value == null) {
                str.append("  ", errorStyler);
            } else {
                var needQuotationMark = "string".equals(arg.getType()) || "enumeration".equals(arg.getType());
                if (needQuotationMark) {
                    str.append("\"", isValid(arg.getArgumentInfo()) ? numberStyler : errorStyler);
                }
                str.append(value, isValid(arg.getArgumentInfo()) ? numberStyler : errorStyler);
                if (needQuotationMark) {
                    str.append("\"", isValid(arg.getArgumentInfo()) ? numberStyler : errorStyler);
                }
            }
        }
        str.append(")", bracketStyler);
        return str;
    }

    public boolean isArmed() {
        return state == StackedState.ARMED;
    }

    public String getCommandId() {
        return execution != null ? execution.getId() : null;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setMetaCommand(CommandInfo meta) {
        this.meta = meta;
    }

    public String getName() {
        return meta.getQualifiedName();
    }

    public String getName(String namespace) {
        for (var alias : meta.getAliasList()) {
            if (alias.getNamespace().equals(namespace)) {
                return alias.getName();
            }
        }
        return null;
    }

    public CommandInfo getMetaCommand() {
        return meta;
    }

    public void setStackedState(StackedState state) {
        if (state == StackedState.ARMED && state != this.state) {
            execution = null;
        }
        this.state = state;
    }

    public StackedState getStackedState() {
        return state;
    }

    public Acknowledgment getQueuedState() {
        return execution != null ? execution.getQueuedAcknowledgment() : null;
    }

    public Acknowledgment getReleasedState() {
        return execution != null ? execution.getReleasedAcknowledgment() : null;
    }

    public Acknowledgment getSentState() {
        return execution != null ? execution.getSentAcknowledgment() : null;
    }

    public void addAssignment(ArgumentInfo arg, String value) {
        assignments.put(arg, value);
    }

    public Map<ArgumentInfo, String> getAssignments() {
        return assignments;
    }

    public Collection<TelecommandArgument> getEffectiveAssignments() {
        // We want this to be top-down, as-defined in mdb
        var argumentsByName = new LinkedHashMap<String, TelecommandArgument>();
        var hierarchy = new ArrayList<CommandInfo>();
        hierarchy.add(meta);
        var base = meta;
        while (base.hasBaseCommand()) {
            base = base.getBaseCommand();
            hierarchy.add(0, base);
        }

        // From parent to child. Children can override initial values (= defaults)
        for (var cmd : hierarchy) {
            // Set all values, even if null initial value. This gives us consistent ordering
            for (var argument : cmd.getArgumentList()) {
                var editable = true;
                argumentsByName.put(argument.getName(), new TelecommandArgument(argument, editable));
            }

            // Override values with actual assignments
            if (cmd.getArgumentAssignmentList() != null) {
                for (var argumentAssignment : cmd.getArgumentAssignmentList()) {
                    var argument = argumentsByName.get(argumentAssignment.getName());
                    argument.setValue(argumentAssignment.getValue());
                    argument.setEditable(false);
                }
            }
        }

        // Order required arguments before optional for better visual results
        return Stream
                .concat(argumentsByName.values().stream().filter(arg -> arg.getValue() == null),
                        argumentsByName.values().stream().filter(arg -> arg.getValue() != null))
                .collect(Collectors.toList());
    }

    public List<String> getMessages() {
        var messages = new ArrayList<String>();
        for (var arg : meta.getArgumentList()) {
            if (!isValid(arg)) {
                messages.add(String.format("Missing argument '%s'", arg.getName()));
            }
        }

        return messages;
    }

    public String getAssignedStringValue(ArgumentInfo argument) {
        return assignments.get(argument);
    }

    public boolean isAssigned(ArgumentInfo arg) {
        return assignments.get(arg) != null;
    }

    public boolean isValid(ArgumentInfo arg) {
        if (!isAssigned(arg) && !arg.hasInitialValue()) {
            return false;
        }
        return true;
    }

    public boolean isDefaultChanged(ArgumentInfo arg) {
        var assignment = assignments.get(arg);
        if (assignment != null && arg.hasInitialValue()) {
            return !assignment.equals(arg.getInitialValue());
        }
        return false;
    }

    public boolean isValid() {
        for (var arg : meta.getArgumentList()) {
            if (!isValid(arg)) {
                return false;
            }
        }
        return true;
    }

    public List<ArgumentInfo> getMissingArguments() {
        var res = new ArrayList<ArgumentInfo>();
        for (var arg : meta.getArgumentList()) {
            if (assignments.get(arg) == null) {
                res.add(arg);
            }
        }
        return res;
    }

    public void markSkipped() {
        state = StackedState.SKIPPED;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setExtra(String option, Value value) {
        if (value == null) {
            extra.remove(option);
        } else {
            extra.put(option, value);
        }
    }

    public Map<String, Value> getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return meta.getQualifiedName();
    }

    public String getSource() {
        return toStyledString(null).getString();
    }
}
