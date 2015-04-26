package org.yamcs.studio.core.commanding;

import static org.yamcs.studio.core.ui.Comparators.INTEGER_COMPARATOR;
import static org.yamcs.studio.core.ui.Comparators.LONG_COMPARATOR;
import static org.yamcs.studio.core.ui.Comparators.STRING_COMPARATOR;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class TelecommandViewerComparator extends ViewerComparator {

    private String currentColumn;
    private boolean ascending;

    public TelecommandViewerComparator() {
        currentColumn = TelecommandView.COL_T;
        ascending = false;
    }

    public int getDirection() {
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
        TelecommandRecord r1 = (TelecommandRecord) o1;
        TelecommandRecord r2 = (TelecommandRecord) o2;
        int rc;
        switch (currentColumn) {
        case TelecommandView.COL_COMMAND:
            rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            break;
        case TelecommandView.COL_SRC_ID:
            rc = INTEGER_COMPARATOR.compare(r1.getSequenceNumber(), r2.getSequenceNumber());
            break;
        case TelecommandView.COL_SRC_HOST:
            rc = STRING_COMPARATOR.compare(r1.getOrigin(), r2.getOrigin());
            break;
        case TelecommandView.COL_USER:
            rc = STRING_COMPARATOR.compare(r1.getUsername(), r2.getUsername());
            break;
        case TelecommandView.COL_SEQ_ID:
            rc = STRING_COMPARATOR.compare(r1.getFinalSequenceCount(), r2.getFinalSequenceCount());
            break;
        case TelecommandView.COL_T:
            rc = LONG_COMPARATOR.compare(r1.getRawGenerationTime(), r2.getRawGenerationTime());
            break;
        default: // dynamic column (TODO be more clever about non-timestamp dynamic columns)
            long delta1 = r1.getAckDurationForColumn(currentColumn);
            long delta2 = r2.getAckDurationForColumn(currentColumn);
            rc = -LONG_COMPARATOR.compare(delta1, delta2);
        }

        return ascending ? rc : -rc;
    }
}
