package org.yamcs.studio.eventlog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class EventLogView extends ViewPart {

    private EventLog eventlog;

    @Override
    public void createPartControl(Composite parent) {
        eventlog = new EventLog(parent, SWT.NONE);
        eventlog.setLayoutData(new GridData(GridData.FILL_BOTH));

        eventlog.attachToSite(getViewSite());
        createActions(getSite().getShell());
    }

    private void createActions(Shell shell) {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();

        Action configureColumnsAction = new Action("Configure Columns...", IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                eventlog.openConfigureColumnsDialog(shell);
            }
        };
        mgr.add(configureColumnsAction);
    }

    @Override
    public void setFocus() {
        eventlog.setFocus();
    }

    public EventLog getEventLog() {
        return eventlog;
    }

    @Override
    public void dispose() {
        eventlog.dispose();
        super.dispose();
    }
}
