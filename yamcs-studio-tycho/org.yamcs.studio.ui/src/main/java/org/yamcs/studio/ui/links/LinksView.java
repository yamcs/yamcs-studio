package org.yamcs.studio.ui.links;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;

public class LinksView extends ViewPart implements StudioConnectionListener, LinkListener {

    private static final Logger log = Logger.getLogger(LinksView.class.getName());
    YamcsConnector yconnector;
    private volatile String selectedInstance; // TODO: handle switching between instance at the Studio level
    ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);

    LinksTableViewer linksTableViewer;
    LinkContentProvider linksContentProvider;
    LinkControlClient linkControlClient;

    HashMap<String, LinkTableModel> linkModels = new HashMap<String, LinkTableModel>();
    LinkTableModel currentLinkModel;

    @Override
    public void log(String message) {
        log.info(message);
    }

    @Override
    public void updateLink(LinkInfo li) {
        try {
            if (!li.getInstance().equals(selectedInstance))
                return;

            Display.getDefault().asyncExec(() ->
            {
                try {
                    log.fine("processing updateLink " + li);
                    String modelName = li.getInstance();
                    if (!linkModels.containsKey(modelName))
                    {
                        addLinkModel(li.getInstance());
                    }
                    LinkTableModel model = linkModels.get(modelName);
                    model.updateLink(li);
                }
                    catch (Exception e)
                    {
                        log.severe(e.toString());
                    }
                });
        } catch (Exception e)
        {
            log.severe(e.toString());
        }
    }

    public void addLinkModel(String instance)
    {
        LinkTableModel model = new LinkTableModel(timer, linksTableViewer);
        linkModels.put(instance, model);
        if (currentLinkModel == null)
            currentLinkModel = model;
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        yconnector.connect(hornetqProps);
        setSelectedInstance(hornetqProps.instance);

    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() ->
        {
            this.linksTableViewer.getTable().removeAll();
            this.currentLinkModel = null;
            this.linkModels.clear();
        });
        yconnector.disconnect();
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
        linksTableViewer = new LinksTableViewer(this, tableWrapper, tcl);
        linksContentProvider = new LinkContentProvider(linksTableViewer);
        linksTableViewer.setContentProvider(linksContentProvider);
        linksTableViewer.setInput(linksContentProvider);

        linksTableViewer.getTable().addListener(SWT.FocusOut, (event) -> {
            linksTableViewer.getTable().deselectAll();
        });

        if (getViewSite() != null)
            getViewSite().setSelectionProvider(linksTableViewer);

        // Set initial state
        linksTableViewer.refresh();

        // Connection to Yamcs server
        yconnector = new YamcsConnector(false);
        linkControlClient = new LinkControlClient(yconnector);
        if (YamcsPlugin.getDefault() != null)
            YamcsPlugin.getDefault().addStudioConnectionListener(this);
        linkControlClient.setLinkListener(this);

    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    private void setSelectedInstance(String newInstance) {
        // queuesModels.clear();
        this.selectedInstance = newInstance;
    }

    public static void main(String[] arg)
    {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("dialog test");
        shell.open();

        LinksView lv = new LinksView();
        lv.createPartControl(shell);
        shell.pack();

        YamcsConnectData hornetqProps = new YamcsConnectData();
        hornetqProps.host = "127.0.0.1";
        hornetqProps.instance = "obcp";
        hornetqProps.username = "operator";
        hornetqProps.password = "password";
        lv.onStudioConnect(null, null, hornetqProps, null, null);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}
