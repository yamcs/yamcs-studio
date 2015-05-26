package org.yamcs.studio.ui.commanding.stack;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.StyledString;
import org.yamcs.protobuf.Rest.RestArgumentType;
import org.yamcs.protobuf.Rest.RestCommandType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.xtce.Argument;
import org.yamcs.xtce.ArgumentAssignment;
import org.yamcs.xtce.MetaCommand;

/**
 * Keep track of the lifecycle of a stacked command.
 *
 * @see {@link CommandStack}
 */
public class StackedCommand {

    public enum State {
        UNARMED("Unarmed"),
        ARMED("Armed"),
        ISSUED("Issued"),
        SKIPPED("Skipped"),
        REJECTED("Rejected");

        private String text;

        private State(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private MetaCommand meta;
    private Map<Argument, String> assignments = new HashMap<>();
    private State state = State.UNARMED;

    public StyledString toStyledString(CommandStackView styleProvider) {
        StyledString str = new StyledString();
        str.append(meta.getOpsName(), styleProvider.getIdentifierStyler(this));
        str.append("(", styleProvider.getBracketStyler(this));
        boolean first = true;
        for (Argument arg : meta.getArgumentList()) {
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

    public boolean isArmed() {
        return state == State.ARMED;
    }

    public RestCommandType.Builder toRestCommandType() {
        RestCommandType.Builder req = RestCommandType.newBuilder();
        req.setId(NamedObjectId.newBuilder()
                .setNamespace(YamcsPlugin.getDefault().getMdbNamespace())
                .setName(meta.getOpsName()));
        req.setSequenceNumber(YamcsPlugin.getNextCommandClientId());
        assignments.forEach((k, v) -> {
            req.addArguments(RestArgumentType.newBuilder().setName(k.getName()).setValue(v));
        });
        req.setOrigin(YamcsPlugin.getDefault().getOrigin());

        return req;
    }

    public void setMetaCommand(MetaCommand meta) {
        this.meta = meta;
    }

    public MetaCommand getMetaCommand() {
        return meta;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void addAssignment(Argument arg, String value) {
        assignments.put(arg, value);
    }

    public Map<Argument, String> getAssignments() {
        return assignments;
    }

    public Collection<TelecommandArgument> getEffectiveAssignments() {
        // We want this to be top-down, as-defined in mdb
        Map<String, TelecommandArgument> argumentsByName = new LinkedHashMap<>();
        List<MetaCommand> hierarchy = new ArrayList<>();
        hierarchy.add(meta);
        MetaCommand base = meta;
        while (base.getBaseMetaCommand() != null) {
            base = base.getBaseMetaCommand();
            hierarchy.add(0, base);
        }

        // From parent to child. Children can override initial values (= defaults)
        for (MetaCommand cmd : hierarchy) {
            // Set all values, even if null initial value. This gives us consistent ordering
            for (Argument argument : cmd.getArgumentList()) {
                String name = argument.getName();
                String value = argument.getInitialValue();
                boolean editable = true;
                argumentsByName.put(name, new TelecommandArgument(name, value, editable));
            }

            // Override with actual assignments
            // TODO this should return an empty list in yamcs. Not null
            if (cmd.getArgumentAssignmentList() != null)
                for (ArgumentAssignment argumentAssignment : cmd.getArgumentAssignmentList()) {
                    String name = argumentAssignment.getArgumentName();
                    String value = argumentAssignment.getArgumentValue();
                    boolean editable = (cmd == meta);
                    argumentsByName.put(name, new TelecommandArgument(name, value, editable));
                }
        }

        return argumentsByName.values();
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<String>();
        for (Argument arg : meta.getArgumentList())
            if (!isValid(arg))
                messages.add(String.format("Missing argument '%s'", arg.getName()));

        return messages;
    }

    public String getAssignedStringValue(Argument argument) {
        return assignments.get(argument);
    }

    public boolean isAssigned(Argument arg) {
        return assignments.get(arg) != null;
    }

    public boolean isValid(Argument arg) {
        if (!isAssigned(arg))
            return false;
        return true; // TODO more local checks
    }

    public boolean isValid() {
        for (Argument arg : meta.getArgumentList())
            if (!isValid(arg))
                return false;
        return true;
    }

    public List<Argument> getMissingArguments() {
        List<Argument> res = new ArrayList<>();
        for (Argument arg : meta.getArgumentList()) {
            if (assignments.get(arg) == null)
                res.add(arg);
        }
        return res;
    }
}
