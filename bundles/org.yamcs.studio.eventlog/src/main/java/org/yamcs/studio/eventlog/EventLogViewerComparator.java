package org.yamcs.studio.eventlog;

import static org.yamcs.studio.core.ui.utils.Comparators.LONG_COMPARATOR;
import static org.yamcs.studio.core.ui.utils.Comparators.STRING_COMPARATOR;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogViewerComparator extends ViewerComparator {

    private String currentColumn;
    private boolean ascending;

    public EventLogViewerComparator() {
        currentColumn = EventLogTableViewer.COL_GENERATION;
        ascending = true;
    }

    public int getDirection() {
        // In a range of A-Z, chevron should point to where 'A' is.
        return ascending ? SWT.UP : SWT.DOWN;
    }

    public void setColumn(TableColumn column) {
        if (column.getText().equals(currentColumn)) {
            ascending = !ascending;
        } else {
            currentColumn = column.getText();
            ascending = true;
        }
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        Event r1 = (Event) o1;
        Event r2 = (Event) o2;
        int rc;
        switch (currentColumn) {
        case EventLogTableViewer.COL_SEQNUM:
            rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogTableViewer.COL_MESSAGE:
            rc = STRING_COMPARATOR.compare(r1.getMessage(), r2.getMessage());
            break;
        case EventLogTableViewer.COL_RECEPTION:
            rc = LONG_COMPARATOR.compare(r1.getReceptionTime(), r2.getReceptionTime());
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            }
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_GENERATION:
            rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            }
            break;
        case EventLogTableViewer.COL_SOURCE:
            rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            if (rc == 0) {
                rc = STRING_COMPARATOR.compare(r1.getType(), r2.getType());
            }
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            }
            break;
        case EventLogTableViewer.COL_TYPE:
            rc = STRING_COMPARATOR.compare(r1.getType(), r2.getType());
            if (rc == 0) {
                rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            }
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            }
            break;
        default:
            throw new IllegalStateException("Cannot order unsupported column " + currentColumn);
        }
        return ascending ? rc : -rc;
    }
}
