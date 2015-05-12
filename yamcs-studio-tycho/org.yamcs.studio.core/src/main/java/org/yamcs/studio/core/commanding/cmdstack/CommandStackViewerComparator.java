package org.yamcs.studio.core.commanding.cmdstack;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Non-configurable ordering. So that user can have some consistency here. Sorting is always in
 * natural intended execution order.
 */
public class CommandStackViewerComparator extends ViewerComparator {

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        Telecommand t1 = (Telecommand) o1;
        Telecommand t2 = (Telecommand) o2;
        return Integer.compare(t1.getRowId(), t2.getRowId());
    }
}
