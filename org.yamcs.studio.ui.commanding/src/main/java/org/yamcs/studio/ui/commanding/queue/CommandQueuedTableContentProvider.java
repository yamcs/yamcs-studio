package org.yamcs.studio.ui.commanding.queue;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/*contains all the queues and corresponding commands for one channel*/
public class CommandQueuedTableContentProvider implements IStructuredContentProvider {
    //   private CommandQueues queues = CommandQueues.getInstance();
    private TableViewer tableViewer;

    public CommandQueuedTableContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return new Object[0];
    }

}
