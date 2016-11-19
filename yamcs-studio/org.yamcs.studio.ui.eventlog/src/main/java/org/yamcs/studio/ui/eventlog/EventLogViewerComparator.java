package org.yamcs.studio.ui.eventlog;

import static org.yamcs.studio.core.ui.utils.Comparators.LONG_COMPARATOR;
import static org.yamcs.studio.core.ui.utils.Comparators.STRING_COMPARATOR;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogViewerComparator implements Comparator<Event> {

    private String currentColumn;
    private boolean ascending;

    public EventLogViewerComparator() {
        currentColumn = EventLogView.COL_RECEIVED;
        ascending = false;
    }

    public int getDirection() {
        return ascending ? SWT.DOWN : SWT.UP;
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
    public int compare(Event r1, Event r2) {
        int rc;
        switch (currentColumn) {
        case EventLogView.COL_SEQNUM:
            // compare seq number
            rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_DESCRIPTION:
            // compare message
            rc = STRING_COMPARATOR.compare(r1.getMessage(), r2.getMessage());
            break;
        case EventLogView.COL_RECEIVED:
            // compare reception time, seq number
            rc = LONG_COMPARATOR.compare(r1.getReceptionTime(), r2.getReceptionTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_GENERATION:
            // compare generation time, seq number
            rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_SOURCE:
            // compare source, type, generation time, seq number
            rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            if (rc == 0)
                rc = STRING_COMPARATOR.compare(r1.getType(), r2.getType());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        default:
            throw new IllegalStateException("Cannot order unsupported column " + currentColumn);
        }
        return ascending ? rc : -rc;
    }
}
