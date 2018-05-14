package org.yamcs.studio.eventlog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class EventLogView extends ViewPart implements YamcsConnectionListener, InstanceListener {

    private EventLog eventlog;

    @Override
    public void createPartControl(Composite parent) {
        eventlog = new EventLog(parent, SWT.NONE);
        eventlog.setLayoutData(new GridData(GridData.FILL_BOTH));

        eventlog.attachToSite(getViewSite());
        eventlog.connect();

        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public void setFocus() {
        eventlog.setFocus();
    }

    @Override
    public void onYamcsConnected() {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    private void updateYamcsInstance() {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        if (yamcsInstance != null) {
            setContentDescription("Showing events for Yamcs instance " + yamcsInstance + " (no filter)");
        } else {
            setContentDescription(null);
        }
    }

    @Override
    public void onYamcsDisconnected() {
    }

    public EventLog getEventLog() {
        return eventlog;
    }

    @Override
    public void dispose() {
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
        eventlog.dispose();
        super.dispose();
    }
}
