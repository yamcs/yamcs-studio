package org.yamcs.studio.commanding.queue;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class CommandQueuesTableContentProvider implements IStructuredContentProvider {

    public CommandQueuesTableContentProvider(TableViewer tableViewer) {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        // The inputElement comes from view.setInput()
        if (inputElement instanceof List) {
            List<?> models = (List<?>) inputElement;
            return models.toArray();
        }
        return new Object[0];
    }

    public int indexOf(Object element) {
        // return queues.getQueues().indexOf(element);
        return 0;
    }

}
