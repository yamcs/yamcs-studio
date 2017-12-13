package org.yamcs.studio.eventlog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class EventLogView extends ViewPart {

    private EventLog eventlog;

    @Override
    public void createPartControl(Composite parent) {

        eventlog = new EventLog(parent, SWT.NONE);
        eventlog.setLayoutData(new GridData(GridData.FILL_BOTH));

        eventlog.attachToSite(getViewSite());
        eventlog.connect();

        eventlog.setStatsListener(this::updateSummaryLine);
    }

    @Override
    public void setFocus() {
        eventlog.setFocus();
    }

    @Override
    public void dispose() {
        eventlog.dispose();
        super.dispose();
    }

    private void updateSummaryLine(int errorCount, int warningCount, int infoCount) {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        String summaryLine = "";
        if (yamcsInstance != null) {
            summaryLine = "Showing events for Yamcs instance " + yamcsInstance + ". ";
        }

        setContentDescription(summaryLine + String.format("%d errors, %d warnings, %d others (no filter)",
                errorCount, warningCount, infoCount));
    }

    public EventLog getEventLog() {
        return eventlog;
    }
}
