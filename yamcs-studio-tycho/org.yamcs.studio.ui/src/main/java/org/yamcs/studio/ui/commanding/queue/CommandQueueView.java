package org.yamcs.studio.ui.commanding.queue;

import java.util.HashMap;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.utils.TimeEncoding;

public class CommandQueueView extends ViewPart implements StudioConnectionListener, CommandQueueListener {
    public static long oldCommandWarningTime = 60;
    private static final Logger log = Logger.getLogger(CommandQueueView.class.getName());

    CommandQueueControlClient commandQueueControl;
    YamcsConnector yconnector;

    HashMap<String, QueuesTableModel> queuesModels = new HashMap<String, QueuesTableModel>();
    QueuesTableModel currentQueuesModel;

    CommandQueuesTableContentProvider commandQueuesContentProvider;
    CommandQueuedTableContentProvider commandQueuedContentProvider;

    CommandQueuesTableViewer commandQueuesTableViewer;
    CommandQueuedTableViewer commandQueuedTableViewer;

    private volatile String selectedInstance;

    @Override
    public void onStudioConnect() {
        YamcsConnectData hornetqProps = ConnectionManager.getInstance().getHornetqProperties();
        yconnector.connect(hornetqProps);
        setSelectedInstance(hornetqProps.instance);
    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() ->
        {
            commandQueuesTableViewer.getTable().removeAll();
            commandQueuedTableViewer.getTable().removeAll();
            currentQueuesModel = null;
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

        SashForm sash = new SashForm(parent, SWT.VERTICAL);

        Composite tableWrapper = new Composite(sash, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandQueuesTableViewer = new CommandQueuesTableViewer(this, tableWrapper, tcl);
        commandQueuesContentProvider = new CommandQueuesTableContentProvider(commandQueuesTableViewer);
        commandQueuesTableViewer.setContentProvider(commandQueuesContentProvider);
        commandQueuesTableViewer.setInput(commandQueuesContentProvider);
        commandQueuesTableViewer.addSelectionChangedListener(evt ->
        {
            currentQueuesModel.reloadCommandsTable((IStructuredSelection) evt.getSelection());
        });
        if (getViewSite() != null)
            getViewSite().setSelectionProvider(commandQueuesTableViewer);

        Composite tableWrapper2 = new Composite(sash, SWT.NONE);
        tableWrapper2.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl2 = new TableColumnLayout();
        tableWrapper2.setLayout(tcl2);
        commandQueuedTableViewer = new CommandQueuedTableViewer(this, tableWrapper2, tcl2);
        commandQueuedContentProvider = new CommandQueuedTableContentProvider(commandQueuedTableViewer);
        commandQueuedTableViewer.setContentProvider(commandQueuedContentProvider);
        commandQueuedTableViewer.setInput(commandQueuedContentProvider);

        // Set initial state
        refreshState();

        // Connection to Yamcs server
        yconnector = new YamcsConnector(false);
        commandQueueControl = new CommandQueueControlClient(yconnector);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        commandQueueControl.addCommandQueueListener(this);
    }

    public void refreshState() {
        commandQueuesTableViewer.refresh();
        commandQueuedTableViewer.refresh();
    }

    @Override
    public void setFocus() {
        commandQueuesTableViewer.getTable().setFocus();
        //  commandQueuedTableViewer.getTable().setFocus();

    }

    public void addProcessor(String instance, String channelName) {
        QueuesTableModel model = new QueuesTableModel(this, commandQueuesTableViewer, commandQueuedTableViewer, instance, channelName);
        queuesModels.put(instance + "." + channelName, model);
        if (currentQueuesModel == null)
            currentQueuesModel = model;
    }

    public void removeProcessor(String instance, String channelName) {
        queuesModels.remove(instance + "." + channelName);
    }

    public void setProcessor(String instance, String channelName) {
        log.fine("setting channel : " + instance + " cn: " + channelName);
        currentQueuesModel = channelName == null ? null : queuesModels.get(instance + "." + channelName);
        if (currentQueuesModel == null)
            return;

        commandQueuedTableViewer.setContentProvider(currentQueuesModel);
    }

    @Override
    public void updateQueue(CommandQueueInfo cqi) {

        Display.getDefault().asyncExec(() ->
        {
            log.fine("processing updateQueue " + cqi);
            String modelName = cqi.getInstance() + "." + cqi.getProcessorName();
            if (!queuesModels.containsKey(modelName))
            {
                addProcessor(cqi.getInstance(), cqi.getProcessorName());
            }
            QueuesTableModel model = queuesModels.get(modelName);
            model.updateQueue(cqi);
        });
    }

    @Override
    public void commandAdded(CommandQueueEntry cqe) {
        Display.getDefault().asyncExec(() -> {
            QueuesTableModel model = queuesModels.get(cqe.getInstance() + "." + cqe.getProcessorName());
            model.commandAdded(cqe);
        });
    }

    @Override
    public void commandRejected(CommandQueueEntry cqe) {
        Display.getDefault().asyncExec(() -> {
            QueuesTableModel model = queuesModels.get(cqe.getInstance() + "." + cqe.getProcessorName());
            model.removeCommandFromQueue(cqe);
        });
    }

    @Override
    public void commandSent(CommandQueueEntry cqe) {
        Display.getDefault().asyncExec(() -> {
            QueuesTableModel model = queuesModels.get(cqe.getInstance() + "." + cqe.getProcessorName());
            model.removeCommandFromQueue(cqe);
        });
    }

    /*
     * called when instance changed in yamcs studio. After the list of channels is received, the
     * update is also called to retrieve the list of commnand queues
     */
    public void setSelectedInstance(String newInstance) {
        queuesModels.clear();
        this.selectedInstance = newInstance;
    }

    /* called when the instance has been changed from yamcs studio menu */
    public void update() {
        commandQueueControl.receiveInitialConfig();
    }

    @Override
    public void log(String string) {
        log.info(string);
    }

    public static void main(String[] arg)
    {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("dialog test");
        shell.open();

        CommandQueueView cqv = new CommandQueueView();
        cqv.createPartControl(shell);
        shell.pack();

        YamcsConnectData hornetqProps = new YamcsConnectData();
        hornetqProps.host = "127.0.0.1";
        hornetqProps.instance = "obcp";
        hornetqProps.username = "operator";
        hornetqProps.password = "password";
        cqv.onStudioConnect();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}
