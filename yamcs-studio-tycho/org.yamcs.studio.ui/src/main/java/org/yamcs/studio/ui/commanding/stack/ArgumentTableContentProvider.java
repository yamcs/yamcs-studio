package org.yamcs.studio.ui.commanding.stack;

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
        Telecommand cmd = (Telecommand) inputElement;
        return cmd.getEffectiveAssignments().toArray();
    }
}
