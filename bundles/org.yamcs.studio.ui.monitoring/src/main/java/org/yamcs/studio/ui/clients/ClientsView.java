package org.yamcs.studio.ui.clients;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;

public class ClientsView extends ViewPart implements ManagementListener {

    private ClientsTableViewer viewer;
    private ClientsTableViewerContentProvider contentProvider;

    @Override
    public void createPartControl(Composite parent) {
        viewer = new ClientsTableViewer(parent);

        contentProvider = new ClientsTableViewerContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setInput(contentProvider);

        if (getViewSite() != null) {
            getViewSite().setSelectionProvider(viewer);
        }

        // Set initial state
        viewer.refresh();

        ManagementCatalogue.getInstance().addManagementListener(this);
    }

    @Override
    public void dispose() {
        ManagementCatalogue.getInstance().removeManagementListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Override
    public void processorUpdated(ProcessorInfo processorInfo) {
    }

    @Override
    public void processorClosed(ProcessorInfo processorInfo) {
    }

    @Override
    public void statisticsUpdated(Statistics stats) {
    }

    @Override
    public void clientUpdated(ClientInfo clientInfo) {
        Display.getDefault().asyncExec(() -> {
            contentProvider.processClientUpdate(clientInfo);
            viewer.refresh();
        });
    }

    @Override
    public void clientDisconnected(ClientInfo clientInfo) {
        Display.getDefault().asyncExec(() -> {
            contentProvider.processClientDisconnect(clientInfo);
            viewer.refresh();
        });
    }

    @Override
    public void instanceUpdated(ConnectionInfo connectionInfo) {
    }

    @Override
    public void clearAllManagementData() {
        Display.getDefault().asyncExec(() -> {
            contentProvider.clearAll();
            viewer.refresh();
        });
    }
}
