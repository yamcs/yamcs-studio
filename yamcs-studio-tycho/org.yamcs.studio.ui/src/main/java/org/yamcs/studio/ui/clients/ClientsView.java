package org.yamcs.studio.ui.clients;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ManagementCatalogue;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.web.RestClient;

public class ClientsView extends ViewPart implements StudioConnectionListener, ProcessorListener {

    ClientsTableViewer clientsTableViewer;
    ClientsContentProvider clientsContentProvider;
    ClientsTableModel currentClientsModel;

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() ->
        {
            this.clientsTableViewer.getTable().removeAll();
            this.currentClientsModel = null;
        });
    }

    @Override
    public void createPartControl(Composite parent) {
        // Build the tables
        FillLayout fl = new FillLayout();
        fl.marginHeight = 0;
        fl.marginWidth = 0;
        parent.setLayout(fl);

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
        
        ManagementCatalogue.getInstance().addProcessorListener(this);

        // Connection to Yamcs server
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void setFocus() {

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
        if (currentClientsModel == null)
        {
            currentClientsModel = new ClientsTableModel(clientsTableViewer);
        }
        Display.getDefault().asyncExec(() ->
        {
            currentClientsModel.updateClient(clientInfo);
        });

    }

    @Override
    public void clientDisconnected(ClientInfo clientInfo) {
        Display.getDefault().asyncExec(() ->
        {
            if (currentClientsModel != null && clientInfo != null) {
                currentClientsModel.removeClient(clientInfo);
            }
        });
    }

}
