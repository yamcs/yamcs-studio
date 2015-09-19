package org.yamcs.studio.ui.archive;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
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
import org.yamcs.protobuf.Yamcs.EndAction;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.PacketReplayRequest;
import org.yamcs.protobuf.Yamcs.PpReplayRequest;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.ui.utils.TimeInterval;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.css.OPIUtils;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class CreateReplayDialog extends TitleAreaDialog {

    private static final Logger log = Logger.getLogger(CreateReplayDialog.class.getName());

    // TODO look instead at current list of processors, and find something new
    private static AtomicInteger replayCounter = new AtomicInteger();

    private Text name;
    private String nameValue = "replay" + replayCounter.incrementAndGet();

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;

    private TableViewer packetsTable;
    private List<String> packetsValue;

    private TableViewer ppTable;
    private List<String> ppValue;

    public CreateReplayDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Start a new replay");
        setMessage("Replays can be joined by other users", IMessageProvider.INFORMATION);
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

        /*
         * PACKET TABLE
         */
        Label lbl = new Label(container, SWT.NONE);
        lbl.setText("Packets:");
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);

        Composite tableWrapper = new Composite(container, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableWrapper.setLayout(gl);

        packetsTable = CheckboxTableViewer.newCheckList(tableWrapper, SWT.V_SCROLL | SWT.BORDER);
        packetsTable.setContentProvider(ArrayContentProvider.getInstance());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5 * packetsTable.getTable().getItemHeight();
        packetsTable.getTable().setLayoutData(gd);
        packetsTable.setInput(packetsValue);
        for (TableItem item : packetsTable.getTable().getItems())
            item.setChecked(true);

        /*
         * PP Table
         */
        lbl = new Label(container, SWT.NONE);
        lbl.setText("PPs:");
        lbl.setToolTipText("Processed Parameters");
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);

        tableWrapper = new Composite(container, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableWrapper.setLayout(gl);

        ppTable = CheckboxTableViewer.newCheckList(tableWrapper, SWT.V_SCROLL | SWT.BORDER);
        ppTable.setContentProvider(ArrayContentProvider.getInstance());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5 * ppTable.getTable().getItemHeight();
        ppTable.getTable().setLayoutData(gd);
        ppTable.setInput(ppValue);
        for (TableItem item : ppTable.getTable().getItems())
            item.setChecked(true);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Start At:");
        Composite startComposite = new Composite(container, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        startComposite.setLayout(rl);
        startDate = new DateTime(startComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        startTime = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        if (startTimeValue != null) {
            startDate.setDate(startTimeValue.get(Calendar.YEAR), startTimeValue.get(Calendar.MONTH), startTimeValue.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(startTimeValue.get(Calendar.HOUR_OF_DAY), startTimeValue.get(Calendar.MINUTE), startTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Name:");
        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.setText(nameValue);

        return container;
    }

    private static Calendar toCalendar(DateTime dateWidget, DateTime timeWidget) {
        Calendar cal = Calendar.getInstance(TimeCatalogue.getInstance().getTimeZone());
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Override
    protected void okPressed() {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient == null) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay\n", ""
                    + "Disconnected from Yamcs server");
            return;
        }

        getButton(IDialogConstants.OK_ID).setEnabled(false);
        ProcessorManagementRequest req = toProcessorManagementRequest();

        restClient.createProcessorManagementRequest(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                Display.getDefault().asyncExec(() -> {
                    OPIUtils.resetDisplays();
                    CreateReplayDialog.super.okPressed();
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

    public void initialize(TimeInterval interval, List<String> packets, List<String> ppGroups) {
        startTimeValue = TimeEncoding.toCalendar(interval.calculateStart());
        startTimeValue.setTimeZone(TimeCatalogue.getInstance().getTimeZone());

        packetsValue = packets;
        ppValue = ppGroups;
    }

    public ProcessorManagementRequest toProcessorManagementRequest() {
        PacketReplayRequest.Builder prr = PacketReplayRequest.newBuilder();
        for (TableItem item : packetsTable.getTable().getItems())
            if (item.getChecked())
                prr.addNameFilter(NamedObjectId.newBuilder().setName(item.getText()));

        PpReplayRequest.Builder pprr = PpReplayRequest.newBuilder();
        for (TableItem item : ppTable.getTable().getItems())
            if (item.getChecked())
                pprr.addGroupNameFilter(item.getText());

        ClientInfo ci = ManagementCatalogue.getInstance().getCurrentClientInfo();
        ReplayRequest.Builder rr = ReplayRequest.newBuilder()
                .setStart(TimeEncoding.fromCalendar(toCalendar(startDate, startTime)))
                .setStop(TimeEncoding.MAX_INSTANT)
                .setEndAction(EndAction.STOP);

        if (prr.getNameFilterCount() > 0)
            rr.setPacketRequest(prr);
        if (pprr.getGroupNameFilterCount() > 0)
            rr.setPpRequest(pprr);

        return ProcessorManagementRequest.newBuilder()
                .setOperation(ProcessorManagementRequest.Operation.CREATE_PROCESSOR)
                .setInstance(ConnectionManager.getInstance().getWebProperties().getInstance())
                .setName(name.getText())
                .setType("Archive")
                .setReplaySpec(rr)
                .addClientId(ci.getId())
                .build();
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
