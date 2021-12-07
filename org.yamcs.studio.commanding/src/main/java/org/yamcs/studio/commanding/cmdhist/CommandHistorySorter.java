/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import static org.yamcs.studio.core.utils.Comparators.INSTANT_COMPARATOR;
import static org.yamcs.studio.core.utils.Comparators.INTEGER_COMPARATOR;
import static org.yamcs.studio.core.utils.Comparators.STRING_COMPARATOR;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class CommandHistorySorter extends ViewerComparator {

    private String currentColumn;
    private boolean ascending;

    public CommandHistorySorter() {
        currentColumn = CommandHistoryView.COL_T;
        ascending = true;
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
        var r1 = ((CommandHistoryRecord) o1).getCommand();
        var r2 = ((CommandHistoryRecord) o2).getCommand();
        int rc;
        switch (currentColumn) {
        case CommandHistoryView.COL_COMMAND:
            rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            break;
        case CommandHistoryView.COL_ORIGIN_ID:
            rc = INTEGER_COMPARATOR.compare(r1.getSequenceNumber(), r2.getSequenceNumber());
            break;
        case CommandHistoryView.COL_USER:
            rc = STRING_COMPARATOR.compare(r1.getUsername(), r2.getUsername());
            if (rc == 0) {
                rc = INSTANT_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            }
            break;
        case CommandHistoryView.COL_ORIGIN:
            rc = STRING_COMPARATOR.compare(r1.getOrigin(), r2.getOrigin());
            if (rc == 0) {
                rc = STRING_COMPARATOR.compare(r1.getUsername(), r2.getUsername());
            }
            break;
        case CommandHistoryView.COL_T:
            rc = INSTANT_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            break;
        default: // dynamic column (TODO be more clever about non-timestamp dynamic columns)
            rc = INSTANT_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
        }

        return ascending ? rc : -rc;
    }

    /*public void saveState(IDialogSettings settings) {
        if (settings == null) {
            return;
        }
    
        for (int i = 0; i < priorities.length; i++) {
            settings.put("priority" + i, priorities[i]);
            settings.put("direction" + i, directions[i]);
        }
    }
    
    public void restoreState(IDialogSettings settings) {
        if (settings == null) {
            return;
        }
    
        try {
            for (int i = 0; i < priorities.length; i++) {
                priorities[i] = settings.getInt("priority" + i);
                directions[i] = settings.getInt("direction" + i);
            }
        } catch (NumberFormatException e) {
            resetState();
        }
    }
    
    public void resetState() {
        priorities = new int[DEFAULT_PRIORITIES.length];
        System.arraycopy(DEFAULT_PRIORITIES, 0, priorities, 0, priorities.length);
        directions = new int[DEFAULT_DIRECTIONS.length];
        System.arraycopy(DEFAULT_DIRECTIONS, 0, directions, 0, directions.length);
    }*/
}
