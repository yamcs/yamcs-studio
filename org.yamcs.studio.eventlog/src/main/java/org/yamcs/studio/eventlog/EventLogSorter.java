/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import static org.yamcs.studio.core.utils.Comparators.LONG_COMPARATOR;
import static org.yamcs.studio.core.utils.Comparators.STRING_COMPARATOR;
import static org.yamcs.studio.core.utils.Comparators.TIMESTAMP_COMPARATOR;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Event.EventSeverity;

public class EventLogSorter extends ViewerComparator {

    private String currentColumn;
    private boolean ascending;

    private static final Comparator<EventSeverity> SEVERITY_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == o2) {
            return 0;
        }
        switch (o2) {
        case INFO:
            if (o1 == EventSeverity.WATCH) {
                return 1;
            }
            // fall
        case WATCH:
            if (o1 == EventSeverity.WARNING) {
                return 1;
            }
            // fall
        case WARNING:
            if (o1 == EventSeverity.DISTRESS) {
                return 1;
            }
            // fall
        case DISTRESS:
            if (o1 == EventSeverity.CRITICAL) {
                return 1;
            }
            // fall
        case CRITICAL:
            if (o1 == EventSeverity.SEVERE || o1 == EventSeverity.ERROR) {
                return 1;
            }
            // fall
        default:
            return -1;
        }
    };

    public EventLogSorter() {
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
        var r1 = ((EventLogItem) o1).event;
        var r2 = ((EventLogItem) o2).event;
        int rc;
        switch (currentColumn) {
        case EventLogTableViewer.COL_SEVERITY:
            rc = SEVERITY_COMPARATOR.compare(r1.getSeverity(), r2.getSeverity());
            if (rc == 0) {
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_SEQNUM:
            rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            if (rc == 0) {
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_MESSAGE:
            rc = STRING_COMPARATOR.compare(r1.getMessage(), r2.getMessage());
            if (rc == 0) {
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_RECEPTION:
            rc = TIMESTAMP_COMPARATOR.compare(r1.getReceptionTime(), r2.getReceptionTime());
            if (rc == 0) {
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            }
            if (rc == 0) {
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_GENERATION:
            rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
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
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case EventLogTableViewer.COL_TYPE:
            rc = STRING_COMPARATOR.compare(r1.getType(), r2.getType());
            if (rc == 0) {
                rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            }
            if (rc == 0) {
                rc = TIMESTAMP_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        default:
            throw new IllegalStateException("Cannot order unsupported column " + currentColumn);
        }
        return ascending ? rc : -rc;
    }
}
