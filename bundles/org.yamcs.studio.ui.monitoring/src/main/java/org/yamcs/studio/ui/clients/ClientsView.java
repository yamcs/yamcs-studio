package org.yamcs.studio.ui.clients;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;

public class ClientsView extends ViewPart implements ManagementListener {

    private ClientsTableViewer clientsTableViewer;
    private ClientsContentProvider clientsContentProvider;
    private ClientsTableModel currentClientsModel;

    @Override
    public void createPartControl(Composite parent) {
        Composite tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        clientsTableViewer = new ClientsTableViewer(this, tableWrapper, tcl);
        clientsContentProvider = new ClientsContentProvider(clientsTableViewer);
        clientsTableViewer.setContentProvider(clientsContentProvider);
        clientsTableViewer.setInput(clientsContentProvider);

        if (getViewSite() != null)
            getViewSite().setSelectionProvider(clientsTableViewer);

        // Set initial state
        clientsTableViewer.refresh();

        ManagementCatalogue.getInstance().addManagementListener(this);
    }

    @Override
    public void dispose() {
        ManagementCatalogue.getInstance().removeManagementListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
        clientsTableViewer.getTable().setFocus();
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
        if (currentClientsModel == null) {
            currentClientsModel = new ClientsTableModel(clientsTableViewer);
        }
        Display.getDefault().asyncExec(() -> {
            currentClientsModel.updateClient(clientInfo);
        });

    }

    @Override
    public void clientDisconnected(ClientInfo clientInfo) {
        Display.getDefault().asyncExec(() -> {
            if (currentClientsModel != null && clientInfo != null) {
                currentClientsModel.removeClient(clientInfo);
            }
        });
    }

    @Override
    public void clearAllManagementData() {
        Display.getDefault().asyncExec(() -> {
            clientsTableViewer.getTable().removeAll();
            currentClientsModel = null;
        });
    }
}
