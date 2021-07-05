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
import org.eclipse.jface.viewers.StyledString.Styler;
import org.yamcs.client.Acknowledgment;
import org.yamcs.client.Command;
import org.yamcs.protobuf.Mdb.ArgumentAssignmentInfo;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Keep track of the lifecycle of a stacked command.
 *
 * @see {@link CommandStack}
 */
public class StackedCommand {

    public enum StackedState {
        DISARMED("Disarmed"),
        ARMED("Armed"),
        ISSUED("Issued"),
        SKIPPED("Skipped"),
        REJECTED("Rejected");

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
    private String selectedAlias;

    private Command execution;

    public void resetExecutionState() {
        state = StackedState.DISARMED;
        execution = null;
    }

    public void updateExecutionState(Command command) {
        this.execution = command;
    }

    public StyledString toStyledString(CommandStackView styleProvider) {
        Styler identifierStyler = styleProvider != null ? styleProvider.getIdentifierStyler(this) : null;
        Styler bracketStyler = styleProvider != null ? styleProvider.getBracketStyler(this) : null;
        Styler argNameStyler = styleProvider != null ? styleProvider.getArgNameStyler(this) : null;
        Styler errorStyler = styleProvider != null ? styleProvider.getErrorStyler(this) : null;
        Styler numberStyler = styleProvider != null ? styleProvider.getNumberStyler(this) : null;

        StyledString str = new StyledString();
        str.append(getSelectedAlias(), identifierStyler);
        str.append("(", bracketStyler);
        boolean first = true;
        for (TelecommandArgument arg : getEffectiveAssignments()) {
            String value = getAssignedStringValue(arg.getArgumentInfo());

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
                boolean needQuotationMark = "string".equals(arg.getType())
                        || "enumeration".equals(arg.getType());
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
        CommandInfo base = meta;
        while (base.hasBaseCommand()) {
            base = base.getBaseCommand();
            hierarchy.add(0, base);
        }

        // From parent to child. Children can override initial values (= defaults)
        for (CommandInfo cmd : hierarchy) {
            // Set all values, even if null initial value. This gives us consistent ordering
            for (ArgumentInfo argument : cmd.getArgumentList()) {
                boolean editable = true;
                argumentsByName.put(argument.getName(), new TelecommandArgument(argument, editable));
            }

            // Override values with actual assignments
            if (cmd.getArgumentAssignmentList() != null) {
                for (ArgumentAssignmentInfo argumentAssignment : cmd.getArgumentAssignmentList()) {
                    TelecommandArgument argument = argumentsByName.get(argumentAssignment.getName());
                    argument.setValue(argumentAssignment.getValue());
                    argument.setEditable(false);
                }
            }
        }

        // Order required arguments before optional for better visual results
        return Stream.concat(
                argumentsByName.values().stream().filter(arg -> arg.getValue() == null),
                argumentsByName.values().stream().filter(arg -> arg.getValue() != null)).collect(Collectors.toList());
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
        String assignment = assignments.get(arg);
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

    public void setSelectedAliase(String alias) {
        this.selectedAlias = alias;

    }

    public String getSelectedAlias() {
        return selectedAlias;

    }

    public static StackedCommand buildCommandFromSource(String commandSource) throws Exception {
        StackedCommand result = new StackedCommand();

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
        int indexStartOfArguments = commandSource.indexOf("(");
        int indexStopOfArguments = commandSource.lastIndexOf(")");
        String commandArguments = commandSource.substring(indexStartOfArguments + 1, indexStopOfArguments);
        commandArguments = commandArguments.replaceAll("[\n]", "");
        String commandAlias = commandSource.substring(0, indexStartOfArguments);

        // Retrieve meta command and selected namespace
        CommandInfo commandInfo = null;
        String selectedAlias = "";
        for (CommandInfo ci : YamcsPlugin.getMissionDatabase().getCommands()) {

            for (NamedObjectId noi : ci.getAliasList()) {
                String alias = noi.getNamespace() + "/" + noi.getName();
                if (alias.equals(commandAlias)) {
                    commandInfo = ci;
                    selectedAlias = alias;
                    break;
                }
            }
        }
        if (commandInfo == null) {
            throw new Exception("Unable to retrieved this command in the MDB");
        }
        result.setMetaCommand(commandInfo);
        result.setSelectedAliase(selectedAlias);

        // Retrieve arguments assignment
        // TODO: write formal source grammar
        String[] commandArgumentsTab = commandArguments.split(",");
        for (String commandArgument : commandArgumentsTab) {
            if (commandArgument == null || commandArgument.isEmpty()) {
                continue;
            }
            String[] components = commandArgument.split(":");
            String argument = components[0].trim();
            String value = components[1].trim();
            boolean foundArgument = false;
            List<ArgumentInfo> metaCommandArgumentsList = getAllArgumentList(commandInfo);
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
        List<ArgumentInfo> result = commandInfo.getArgumentList();
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
