package org.yamcs.studio.ui.links;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class LinkContentProvider implements IStructuredContentProvider {
    //   private CommandQueues queues = CommandQueues.getInstance();
    private TableViewer tableViewer;

    public LinkContentProvider(TableViewer tableViewer) {
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
