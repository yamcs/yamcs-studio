package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class CommandStackTableContentProvider implements IStructuredContentProvider {

    private CommandStack stack = CommandStack.getInstance();
    private TableViewer tableViewer;

    public CommandStackTableContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO ?
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return stack.getCommands().toArray();
    }

    public int indexOf(Object element) {
        return stack.getCommands().indexOf(element);
    }

    public void addTelecommand(StackedCommand entry) {
        stack.addCommand(entry);
        tableViewer.add(entry);
    }
}
