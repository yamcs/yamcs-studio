package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.StyledString;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandId;
import org.yamcs.protobuf.Mdb.ArgumentAssignmentInfo;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.protobuf.Rest.IssueCommandRequest.Assignment;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.ui.commanding.PTVInfo;

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

    private CommandInfo meta;
    private Map<ArgumentInfo, String> assignments = new HashMap<>();

    // Increases every attempt
    private int clientId = -1;

    private StackedState state = StackedState.DISARMED;

    private PTVInfo ptvInfo = new PTVInfo();

    public boolean matches(CommandId commandId) {
        // FIXME add user too
        String ourOrigin = CommandingCatalogue.getInstance().getCommandOrigin();
        return clientId == commandId.getSequenceNumber() && commandId.getOrigin().equals(ourOrigin);
    }

    public void resetExecutionState() {
        state = StackedState.DISARMED;
        ptvInfo = new PTVInfo();
        clientId = -1;
    }

    public void updateExecutionState(CommandHistoryEntry entry) {
        for (CommandHistoryAttribute attr : entry.getAttrList()) {
            if (attr.getName().equals("TransmissionConstraints")) {
                ptvInfo.setState(PTVInfo.State.fromYamcsValue(attr.getValue()));
            } else if (attr.getName().equals("CommandFailed")) {
                ptvInfo.setFailureMessage(attr.getValue().getStringValue());
            }
        }
    }

    public StyledString toStyledString(CommandStackView styleProvider) {
        StyledString str = new StyledString();
        str.append(meta.getQualifiedName(), styleProvider.getIdentifierStyler(this));
        str.append("(", styleProvider.getBracketStyler(this));
        boolean first = true;
        for (ArgumentInfo arg : meta.getArgumentList()) {
            if (!first)
                str.append(", ", styleProvider.getBracketStyler(this));
            first = false;
            str.append(arg.getName() + ": ", styleProvider.getArgNameStyler(this));
            String value = getAssignedStringValue(arg);
            if (value == null) {
                str.append("  ", styleProvider.getErrorStyler(this));
            } else {
                str.append(value, isValid(arg) ? styleProvider.getNumberStyler(this) : styleProvider.getErrorStyler(this));
            }
        }
        str.append(")", styleProvider.getBracketStyler(this));
        return str;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean isArmed() {
        return state == StackedState.ARMED;
    }

    /**
     * Generates a REST-representation. The sequence number is increased everytime this method is
     * called, and therefore represents an 'issue attempt'.
     */
    public IssueCommandRequest.Builder toIssueCommandRequest() {
        IssueCommandRequest.Builder req = IssueCommandRequest.newBuilder();
        req.setSequenceNumber(CommandingCatalogue.getInstance().getNextCommandClientId());
        req.setOrigin(CommandingCatalogue.getInstance().getCommandOrigin());
        assignments.forEach((k, v) -> {
            req.addAssignment(Assignment.newBuilder().setName(k.getName()).setValue(v));
        });

        return req;
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

    public PTVInfo getPTVInfo() {
        return ptvInfo;
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
        while (base.getBaseCommand() != null) {
            base = base.getBaseCommand();
            hierarchy.add(0, base);
        }

        // From parent to child. Children can override initial values (= defaults)
        for (CommandInfo cmd : hierarchy) {
            // Set all values, even if null initial value. This gives us consistent ordering
            for (ArgumentInfo argument : cmd.getArgumentList()) {
                String name = argument.getName();
                String value = argument.getInitialValue();
                boolean editable = true;
                argumentsByName.put(name, new TelecommandArgument(name, value, editable));
            }

            // Override with actual assignments
            // TODO this should return an empty list in yamcs. Not null
            if (cmd.getArgumentAssignmentList() != null)
                for (ArgumentAssignmentInfo argumentAssignment : cmd.getArgumentAssignmentList()) {
                    String name = argumentAssignment.getName();
                    String value = argumentAssignment.getValue();
                    boolean editable = (cmd == meta);
                    argumentsByName.put(name, new TelecommandArgument(name, value, editable));
                }
        }

        return argumentsByName.values();
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<String>();
        for (ArgumentInfo arg : meta.getArgumentList())
            if (!isValid(arg))
                messages.add(String.format("Missing argument '%s'", arg.getName()));

        return messages;
    }

    public String getAssignedStringValue(ArgumentInfo argument) {
        return assignments.get(argument);
    }

    public boolean isAssigned(ArgumentInfo arg) {
        return assignments.get(arg) != null;
    }

    public boolean isValid(ArgumentInfo arg) {
        if (!isAssigned(arg))
            return false;
        return true; // TODO more local checks
    }

    public boolean isValid() {
        for (ArgumentInfo arg : meta.getArgumentList())
            if (!isValid(arg))
                return false;
        return true;
    }

    public List<ArgumentInfo> getMissingArguments() {
        List<ArgumentInfo> res = new ArrayList<>();
        for (ArgumentInfo arg : meta.getArgumentList()) {
            if (assignments.get(arg) == null)
                res.add(arg);
        }
        return res;
    }

    public void markSkipped() {
        state = StackedState.SKIPPED;
    }

    @Override
    public String toString() {
        return meta.getQualifiedName();
    }
}
