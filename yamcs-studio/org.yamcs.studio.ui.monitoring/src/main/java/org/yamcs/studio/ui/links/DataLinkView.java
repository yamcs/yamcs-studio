package org.yamcs.studio.ui.links;

import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.LinkListener;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class DataLinkView extends ViewPart implements StudioConnectionListener, InstanceListener, LinkListener {

    private static final Logger log = Logger.getLogger(DataLinkView.class.getName());

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

        tableViewer.getTable().addListener(SWT.FocusOut, evt -> {
            tableViewer.getTable().deselectAll();
        });

        if (getViewSite() != null)
            getViewSite().setSelectionProvider(tableViewer);

        // Set initial state
        tableViewer.refresh();

        LinkCatalogue.getInstance().addLinkListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public void onStudioConnect() {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> updateYamcsInstance());
    }

    @Override
    public void onStudioDisconnect() {
        // remove-all handled through LinkListener
    }

    private void updateYamcsInstance() {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        contentProvider.processYamcsInstance(yamcsInstance);
        if (yamcsInstance != null) {
            setContentDescription("Showing links for Yamcs instance " + yamcsInstance);
        } else {
            setContentDescription(null);
        }
    }

    @Override
    public void dispose() {
        LinkCatalogue.getInstance().removeLinkListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        ConnectionManager.getInstance().removeStudioConnectionListener(this);
        super.dispose();
    }

    @Override
    public void linkRegistered(LinkInfo linkInfo) {
        linkUpdated(linkInfo);
    }

    @Override
    public void linkUpdated(LinkInfo linkInfo) {
        if (tableViewer.getTable().isDisposed())
            return;

        Display display = tableViewer.getTable().getDisplay();
        display.asyncExec(() -> {
            if (display.isDisposed())
                return;

            log.info("processing updateLink " + linkInfo);
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
