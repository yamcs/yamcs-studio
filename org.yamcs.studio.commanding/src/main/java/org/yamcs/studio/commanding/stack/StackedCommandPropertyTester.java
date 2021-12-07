package org.yamcs.studio.commanding.stack;

import org.eclipse.core.expressions.PropertyTester;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;

public class StackedCommandPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        var cmd = (StackedCommand) receiver;
        if ("canBeSkipped".equals(property)) {
            return cmd == CommandStack.getInstance().getActiveCommand() && cmd.getStackedState() != StackedState.ISSUED
                    && cmd.getStackedState() != StackedState.SKIPPED;
        }
        return false;
    }
}
