package org.yamcs.studio.commanding.stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SkipHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        CommandStack stack = CommandStack.getInstance();
        IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        for (Object o : sel.toArray()) {
            ((StackedCommand) o).markSkipped();
            if (stack.hasRemaining())
                commandStackView.selectActiveCommand();
            else
                commandStackView.refreshState();
        }

        return null;
    }
}
