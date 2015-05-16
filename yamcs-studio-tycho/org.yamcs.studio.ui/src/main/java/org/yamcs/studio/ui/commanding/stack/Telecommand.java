package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yamcs.xtce.Argument;
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
