package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yamcs.xtce.Argument;
import org.yamcs.xtce.ArgumentAssignment;
import org.yamcs.xtce.MetaCommand;

/**
 * Keep track of a non-issued command. For now, used only as part of a command stack. Could also
 * eventually be renamed to StackedCommand
 *
 * @see {@link CommandStack}
 */
public class Telecommand {

    private MetaCommand meta;
    private Map<Argument, String> assignments = new HashMap<>();

    public void setMetaCommand(MetaCommand meta) {
        this.meta = meta;
    }

    public MetaCommand getMetaCommand() {
        return meta;
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

    public String getAssignedStringValue(Argument argument) {
        return assignments.get(argument);
    }

    public boolean isAssigned(Argument arg) {
        return assignments.get(arg) != null;
    }

    public boolean isValid(Argument arg) {
        if (!isAssigned(arg))
            return false;
        return true; // TODO
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
