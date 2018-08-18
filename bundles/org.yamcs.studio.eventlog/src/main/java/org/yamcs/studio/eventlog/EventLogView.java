package org.yamcs.studio.eventlog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class EventLogView extends ViewPart {

    private EventLog eventlog;

    @Override
    public void createPartControl(Composite parent) {
        eventlog = new EventLog(parent, SWT.NONE);
        eventlog.setLayoutData(new GridData(GridData.FILL_BOTH));

        eventlog.attachToSite(getViewSite());
        eventlog.connect();
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
