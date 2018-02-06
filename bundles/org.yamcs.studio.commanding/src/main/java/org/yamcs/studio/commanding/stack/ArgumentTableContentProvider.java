package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ArgumentTableContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        StackedCommand cmd = (StackedCommand) inputElement;
        return cmd.getEffectiveAssignments().toArray();
    }
}
