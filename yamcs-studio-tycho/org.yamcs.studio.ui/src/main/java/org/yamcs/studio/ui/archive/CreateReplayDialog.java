package org.yamcs.studio.ui.archive;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.runmode.OPIView;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.EndAction;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.PacketReplayRequest;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.YamcsUIPlugin;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class CreateReplayDialog extends TitleAreaDialog implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(CreateReplayDialog.class.getName());

    private Text name;
    private String nameValue = "replay";

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;

    private DateTime stopDate;
    private DateTime stopTime;
    private Calendar stopTimeValue;

    private TableViewer packetsTable;
    private List<String> packetsValue;

    private RestClient restClient = null;

    public CreateReplayDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Start a new replay");
        setMessage("Replays can be joined by other users", IMessageProvider.INFORMATION);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        this.restClient = restclient;
    }

    @Override
    public void onStudioDisconnect() {
        restClient = null;
    }

    private void validate() {
        String errorMessage = null;
        Calendar start = CreateReplayDialog.toCalendar(startDate, startTime);
        Calendar stop = CreateReplayDialog.toCalendar(stopDate, stopTime);
        if (start.after(stop))
            errorMessage = "Stop has to be greater than start";

        setErrorMessage(errorMessage);
        getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 2;
        container.setLayout(layout);

        Label lbl = new Label(container, SWT.NONE);
        lbl.setText("Name");
        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.setText(nameValue);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Start");
        Composite startComposite = new Composite(container, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        startComposite.setLayout(rl);
        startDate = new DateTime(startComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        startDate.addListener(SWT.Selection, e -> validate());
        startTime = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        startTime.addListener(SWT.Selection, e -> validate());
        if (startTimeValue != null) {
            startDate.setDate(startTimeValue.get(Calendar.YEAR), startTimeValue.get(Calendar.MONTH), startTimeValue.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(startTimeValue.get(Calendar.HOUR_OF_DAY), startTimeValue.get(Calendar.MINUTE), startTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Stop");
        Composite stopComposite = new Composite(container, SWT.NONE);
        rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        rl.fill = true;
        stopComposite.setLayout(rl);
        stopDate = new DateTime(stopComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        stopDate.addListener(SWT.Selection, e -> validate());
        stopTime = new DateTime(stopComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        stopTime.addListener(SWT.Selection, e -> validate());
        if (stopTimeValue != null) {
            stopDate.setDate(stopTimeValue.get(Calendar.YEAR), stopTimeValue.get(Calendar.MONTH), stopTimeValue.get(Calendar.DAY_OF_MONTH));
            stopTime.setTime(stopTimeValue.get(Calendar.HOUR_OF_DAY), stopTimeValue.get(Calendar.MINUTE), stopTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Packets");
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);

        Composite tableWrapper = new Composite(container, SWT.NONE);
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper.setLayout(tcl);

        packetsTable = new TableViewer(tableWrapper, SWT.CHECK | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
        packetsTable.getTable().setHeaderVisible(false);
        packetsTable.getTable().setLinesVisible(true);

        TableViewerColumn nameColumn = new TableViewerColumn(packetsTable, SWT.NONE);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return (String) element;
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100));

        packetsTable.setContentProvider(ArrayContentProvider.getInstance());
        gd = new GridData();
        gd.heightHint = 5 * packetsTable.getTable().getItemHeight();
        packetsTable.getTable().setLayoutData(gd);
        packetsTable.setInput(packetsValue);
        for (TableItem item : packetsTable.getTable().getItems())
            item.setChecked(true);

        return container;
    }

    private static Calendar toCalendar(DateTime dateWidget, DateTime timeWidget) {
        Calendar cal = Calendar.getInstance(YamcsUIPlugin.getDefault().getTimeZone());
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Override
    protected void okPressed() {

        if (restClient == null) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay\n", ""
                    + "Disconnected from Yamcs server");
            return;
        }

        getButton(IDialogConstants.OK_ID).setEnabled(false);
        ProcessorManagementRequest req = toProcessorManagementRequest();
        resetDisplays();

        restClient.createProcessorManagementRequest(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                Display.getDefault().asyncExec(() -> {
                    CreateReplayDialog.super.okPressed();
                    YamcsPlugin.getDefault().refreshClientInfo();
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not start replay", e);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay", e.getMessage());
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                });
            }
        });
    }

    private void resetDisplays() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IViewReference reference : page.getViewReferences()) {
                    IViewPart viewPart = reference.getView(false);
                    if (viewPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) viewPart);
                }
                for (IEditorReference reference : page.getEditorReferences()) {
                    IEditorPart editorPart = reference.getEditor(false);
                    if (editorPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) editorPart);
                }
            }
        }
    }

    private void refreshDisplay(IOPIRuntime opiRuntime) {
        try {
            OPIView.ignoreMemento();
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            ErrorHandlerUtil.handleError("Failed to refresh OPI", e);
        }
    }

    public String getName() {
        return nameValue;
    }

    public void initialize(TimeInterval interval, List<String> packets) {
        startTimeValue = TimeEncoding.toCalendar(interval.calculateStart());
        startTimeValue.setTimeZone(YamcsUIPlugin.getDefault().getTimeZone());
        stopTimeValue = TimeEncoding.toCalendar(interval.calculateStop());
        stopTimeValue.setTimeZone(YamcsUIPlugin.getDefault().getTimeZone());

        packetsValue = packets;
    }

    public ProcessorManagementRequest toProcessorManagementRequest() {
        PacketReplayRequest.Builder prr = PacketReplayRequest.newBuilder();
        for (TableItem item : packetsTable.getTable().getItems())
            if (item.getChecked())
                prr.addNameFilter(NamedObjectId.newBuilder().setName(item.getText()));

        ReplayRequest.Builder rr = ReplayRequest.newBuilder()
                .setStart(TimeEncoding.fromCalendar(toCalendar(startDate, startTime)))
                .setStop(TimeEncoding.fromCalendar(toCalendar(stopDate, stopTime)))
                .setEndAction(EndAction.STOP)
                .setPacketRequest(prr);
        return ProcessorManagementRequest.newBuilder()
                .setOperation(ProcessorManagementRequest.Operation.CREATE_PROCESSOR)
                .setInstance(YamcsPlugin.getDefault().getInstance())
                .setName(name.getText())
                .setType("Archive")
                .setReplaySpec(rr)
                .addClientId(YamcsPlugin.getDefault().getClientInfo().getId())
                .build();
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
