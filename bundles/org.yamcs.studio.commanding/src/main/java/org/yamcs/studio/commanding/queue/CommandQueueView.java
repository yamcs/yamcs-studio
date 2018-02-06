package org.yamcs.studio.commanding.queue;

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
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.studio.core.model.CommandQueueListener;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.utils.TimeEncoding;

public class CommandQueueView extends ViewPart implements CommandQueueListener {
    public static long oldCommandWarningTime = 60;
    private static final Logger log = Logger.getLogger(CommandQueueView.class.getName());

    HashMap<String, QueuesTableModel> queuesModels = new HashMap<>();
    QueuesTableModel currentQueuesModel;

    CommandQueuesTableContentProvider commandQueuesContentProvider;
    CommandQueuedTableContentProvider commandQueuedContentProvider;

    CommandQueuesTableViewer commandQueuesTableViewer;
    CommandQueuedTableViewer commandQueuedTableViewer;

    @Override
    public void clearCommandQueueData() {
        Display.getDefault().asyncExec(() -> {
            commandQueuesTableViewer.getTable().removeAll();
            commandQueuedTableViewer.getTable().removeAll();
            currentQueuesModel = null;
        });
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
        commandQueuesTableViewer.addSelectionChangedListener(evt -> {
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

        CommandingCatalogue.getInstance().addCommandQueueListener(this);
    }

    @Override
    public void dispose() {
        CommandingCatalogue.getInstance().removeCommandQueueListener(this);
        super.dispose();
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
        if (commandQueuesTableViewer.getTable().isDisposed())
            return;
        commandQueuesTableViewer.getTable().getDisplay().asyncExec(() -> {
            if (commandQueuesTableViewer.getTable().isDisposed())
                return;
            log.fine(String.format("processing updateQueue %s", cqi));
            String modelName = cqi.getInstance() + "." + cqi.getProcessorName();
            if (!queuesModels.containsKey(modelName)) {
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

    public static void main(String[] arg) {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("dialog test");
        shell.open();

        CommandQueueView cqv = new CommandQueueView();
        cqv.createPartControl(shell);
        shell.pack();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}
