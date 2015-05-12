package org.yamcs.studio.core.archive;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.PacketReplayRequest;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class CreateReplayDialog extends TitleAreaDialog {

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

    public CreateReplayDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Start a new replay");
        setMessage("Replays can be joined by other users", IMessageProvider.INFORMATION);
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
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Override
    protected void okPressed() {
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        ProcessorManagementRequest req = toProcessorManagementRequest();
        RestClient restClient = YamcsPlugin.getDefault().getRestClient();
        restClient.createProcessorManagementRequest(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                Display.getDefault().asyncExec(() -> {
                    if (responseMsg instanceof RestExceptionMessage) {
                        log.log(Level.WARNING, "Exception returned by server: " + responseMsg);
                        String type = ((RestExceptionMessage) responseMsg).getType();
                        String msg = ((RestExceptionMessage) responseMsg).getMsg();
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay", type + "\n" + msg);
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                    } else {
                        CreateReplayDialog.super.okPressed();
                        // Would prefer to get updates to this from the web socket client
                        YamcsPlugin.getDefault().refreshClientInfo();
                    }
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

    public String getName() {
        return nameValue;
    }

    public void initialize(TimeInterval interval, List<String> packets) {
        startTimeValue = TimeEncoding.toCalendar(interval.calculateStart());
        startTimeValue.setTimeZone(TimeZone.getTimeZone("UTC"));
        stopTimeValue = TimeEncoding.toCalendar(interval.calculateStop());
        stopTimeValue.setTimeZone(TimeZone.getTimeZone("UTC"));

        packetsValue = packets;
    }

    public ProcessorManagementRequest toProcessorManagementRequest() {
        PacketReplayRequest.Builder prr = PacketReplayRequest.newBuilder();
        for (TableItem item : packetsTable.getTable().getItems())
            if (item.getChecked())
                prr.addNameFilter(NamedObjectId.newBuilder().setNamespace("MDB:OPS Name").setName(item.getText()));

        ReplayRequest.Builder rr = ReplayRequest.newBuilder()
                .setStart(TimeEncoding.fromCalendar(toCalendar(startDate, startTime)))
                .setStop(TimeEncoding.fromCalendar(toCalendar(stopDate, stopTime)))
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