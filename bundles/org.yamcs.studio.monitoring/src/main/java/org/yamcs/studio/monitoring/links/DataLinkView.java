package org.yamcs.studio.monitoring.links;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.LinkInfo;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.LinkListener;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class DataLinkView extends ViewPart implements YamcsConnectionListener, InstanceListener, LinkListener {

    private DataLinkTableViewer tableViewer;
    private DataLinkTableViewerContentProvider contentProvider;

    @Override
    public void createPartControl(Composite parent) {
        Composite tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        tableViewer = new DataLinkTableViewer(tableWrapper, tcl);
        contentProvider = new DataLinkTableViewerContentProvider(tableViewer);
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(contentProvider);

        tableViewer.getTable().addListener(SWT.MouseDown, evt -> {
            // Allow the user to get rid of a selection in small tables
            // Note: before this was registered to a FocusOut, but that
            // broke the popup menu for me.
            if (tableViewer.getCell(new Point(evt.x, evt.y)) == null) {
                tableViewer.getTable().deselectAll();
            }
        });

        if (getViewSite() != null) {
            getViewSite().setSelectionProvider(tableViewer);
        }

        // Set initial state
        tableViewer.refresh();

        LinkCatalogue.getInstance().addLinkListener(this);
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public void onYamcsConnected() {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    @Override
    public void onYamcsDisconnected() {
        // remove-all handled through LinkListener
    }

    private void updateYamcsInstance() {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        contentProvider.processYamcsInstance(yamcsInstance);
    }

    @Override
    public void dispose() {
        LinkCatalogue.getInstance().removeLinkListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
        super.dispose();
    }

    @Override
    public void linkRegistered(LinkInfo linkInfo) {
        linkUpdated(linkInfo);
    }

    @Override
    public void linkUpdated(LinkInfo linkInfo) {
        if (tableViewer.getTable().isDisposed()) {
            return;
        }

        Display display = tableViewer.getTable().getDisplay();
        display.asyncExec(() -> {
            if (display.isDisposed()) {
                return;
            }

            contentProvider.processLinkInfo(linkInfo);
        });
    }

    @Override
    public void linkUnregistered(LinkInfo linkInfo) {
        // TODO but not currently sent by Yamcs
    }

    @Override
    public void clearDataLinkData() {
        Display.getDefault().asyncExec(() -> {
            tableViewer.getTable().removeAll();
            contentProvider.clearAll();
        });
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }
}
