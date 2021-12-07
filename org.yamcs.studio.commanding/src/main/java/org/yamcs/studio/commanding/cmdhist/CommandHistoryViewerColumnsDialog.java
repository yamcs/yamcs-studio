package org.yamcs.studio.commanding.cmdhist;

import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.utils.ColumnData;
import org.yamcs.studio.core.utils.ViewerColumnsDialog;

public class CommandHistoryViewerColumnsDialog extends ViewerColumnsDialog {

    private CommandHistoryView view;

    public CommandHistoryViewerColumnsDialog(Shell parentShell, CommandHistoryView view, ColumnData columnData) {
        super(parentShell, columnData);
        this.view = view;
    }

    @Override
    protected void performDefaults() {
        var defaultData = view.createDefaultColumnData();

        getVisible().clear();
        getVisible().addAll(defaultData.getVisibleColumns());

        getNonVisible().clear();
        getNonVisible().addAll(defaultData.getHiddenColumns());

        super.performDefaults();
    }
}
