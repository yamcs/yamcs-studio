package org.yamcs.studio.eventlog;

import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.utils.ColumnData;
import org.yamcs.studio.core.utils.ViewerColumnsDialog;

public class EventLogViewColumnsDialog extends ViewerColumnsDialog {

    private EventLogTableViewer viewer;

    public EventLogViewColumnsDialog(Shell parentShell, EventLogTableViewer viewer, ColumnData columnData) {
        super(parentShell, columnData);
        this.viewer = viewer;
    }

    @Override
    protected void performDefaults() {
        ColumnData defaultData = viewer.createDefaultColumnData();

        getVisible().clear();
        getVisible().addAll(defaultData.getVisibleColumns());

        getNonVisible().clear();
        getNonVisible().addAll(defaultData.getHiddenColumns());

        super.performDefaults();
    }
}
