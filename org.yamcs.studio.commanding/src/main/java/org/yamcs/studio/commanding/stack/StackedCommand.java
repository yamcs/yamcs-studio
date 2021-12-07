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
import org.yamcs.protobuf.Mdb.ArgumentAssignmentInfo;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Keep track of the lifecycle of a stacked command.
 *
 * @see {@link CommandStack}
 */
public class StackedCommand {

    public enum StackedState {
        DISARMED("Disarmed"), ARMED("Armed"), ISSUED("Issued"), SKIPPED("Skipped"), REJECTED("Rejected");

        private String text;

        private StackedState(String text) {
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

    public void resetExecutionState() {
        state = StackedState.DISARMED;
        execution = null;
    }

    public void updateExecutionState(Command command) {
        this.execution = command;
    }

    public StyledString toStyledString(CommandStackView styleProvider) {
        var identifierStyler = styleProvider != null ? styleProvider.getIdentifierStyler(this) : null;
        var bracketStyler = styleProvider != null ? styleProvider.getBracketStyler(this) : null;
        var argNameStyler = styleProvider != null ? styleProvider.getArgNameStyler(this) : null;
        var errorStyler = styleProvider != null ? styleProvider.getErrorStyler(this) : null;
        var numberStyler = styleProvider != null ? styleProvider.getNumberStyler(this) : null;

        var str = new StyledString();
        str.append(meta.getQualifiedName(), identifierStyler);
        str.append("(", bracketStyler);
        var first = true;
        for (TelecommandArgument arg : getEffectiveAssignments()) {
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
        return this.delayMs;
    }

    public void setMetaCommand(CommandInfo meta) {
        this.meta = meta;
    }

    public String getName() {
        return meta.getQualifiedName();
    }

    public CommandInfo getMetaCommand() {
        return meta;
    }

    public void setStackedState(StackedState state) {
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
        Map<String, TelecommandArgument> argumentsByName = new LinkedHashMap<>();
        List<CommandInfo> hierarchy = new ArrayList<>();
        hierarchy.add(meta);
        var base = meta;
        while (base.hasBaseCommand()) {
            base = base.getBaseCommand();
            hierarchy.add(0, base);
        }

        // From parent to child. Children can override initial values (= defaults)
        for (CommandInfo cmd : hierarchy) {
            // Set all values, even if null initial value. This gives us consistent ordering
            for (ArgumentInfo argument : cmd.getArgumentList()) {
                var editable = true;
                argumentsByName.put(argument.getName(), new TelecommandArgument(argument, editable));
            }

            // Override values with actual assignments
            if (cmd.getArgumentAssignmentList() != null) {
                for (ArgumentAssignmentInfo argumentAssignment : cmd.getArgumentAssignmentList()) {
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
        List<String> messages = new ArrayList<>();
        for (ArgumentInfo arg : meta.getArgumentList()) {
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
        return true; // TODO more local checks
    }

    public boolean isDefaultChanged(ArgumentInfo arg) {
        var assignment = assignments.get(arg);
        if (assignment != null && arg.hasInitialValue()) {
            return !assignment.equals(arg.getInitialValue());
        }
        return false;
    }

    public boolean isValid() {
        for (ArgumentInfo arg : meta.getArgumentList()) {
            if (!isValid(arg)) {
                return false;
            }
        }
        return true;
    }

    public List<ArgumentInfo> getMissingArguments() {
        List<ArgumentInfo> res = new ArrayList<>();
        for (ArgumentInfo arg : meta.getArgumentList()) {
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

    public static StackedCommand buildCommandFromSource(String commandSource) throws Exception {
        var result = new StackedCommand();

        // Source must follow the format:
        // <CommandAlias>(<arg1>:<val1>, [...] , <argN>:<valN>)
        // or
        // <CommandAlias>()
        if (commandSource == null) {
            throw new Exception("No Source attached to this command");
        }
        commandSource = commandSource.trim();
        if (commandSource.isEmpty()) {
            throw new Exception("No Source attached to this command");
        }
        var indexStartOfArguments = commandSource.indexOf("(");
        var indexStopOfArguments = commandSource.lastIndexOf(")");
        var commandArguments = commandSource.substring(indexStartOfArguments + 1, indexStopOfArguments);
        commandArguments = commandArguments.replaceAll("[\n]", "");
        var qualifiedName = commandSource.substring(0, indexStartOfArguments);

        // Retrieve meta command
        var commandInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(qualifiedName);
        if (commandInfo == null) {
            throw new Exception("Unable to retrieved this command in the MDB");
        }
        result.setMetaCommand(commandInfo);

        // Retrieve arguments assignment
        // TODO: write formal source grammar
        var commandArgumentsTab = commandArguments.split(",");
        for (String commandArgument : commandArgumentsTab) {
            if (commandArgument == null || commandArgument.isEmpty()) {
                continue;
            }
            var components = commandArgument.split(":");
            var argument = components[0].trim();
            var value = components[1].trim();
            var foundArgument = false;
            var metaCommandArgumentsList = getAllArgumentList(commandInfo);
            for (ArgumentInfo ai : metaCommandArgumentsList) {
                foundArgument = ai.getName().toUpperCase().equals(argument.toUpperCase());
                if (foundArgument) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    result.addAssignment(ai, value);
                    break;
                }
            }
            if (!foundArgument) {
                throw new Exception("Argument " + argument + " is not part of the command definition");
            }
        }

        return result;
    }

    private static List<ArgumentInfo> getAllArgumentList(CommandInfo commandInfo) {
        var result = commandInfo.getArgumentList();
        if (commandInfo.getBaseCommand() != commandInfo) {
            return Stream.concat(result.stream(), getAllArgumentList(commandInfo.getBaseCommand()).stream())
                    .collect(Collectors.toList());
        } else {
            return result;
        }
    }

    public String getSource() {
        return toStyledString(null).getString();
    }
}
