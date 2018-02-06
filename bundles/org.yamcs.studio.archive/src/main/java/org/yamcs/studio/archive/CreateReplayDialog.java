package org.yamcs.studio.archive;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Rest.CreateProcessorRequest;
import org.yamcs.protobuf.SchemaYamcs;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

import io.protostuff.JsonIOUtil;

public class CreateReplayDialog extends TitleAreaDialog {

    private static final Logger log = Logger.getLogger(CreateReplayDialog.class.getName());

    // TODO look instead at current list of processors, and find something new
    private static AtomicInteger replayCounter = new AtomicInteger();

    private Text name;
    private String nameValue = "replay" + replayCounter.incrementAndGet();

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;

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

        Label lbl = new Label(container, SWT.NONE);
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
            startDate.setDate(startTimeValue.get(Calendar.YEAR), startTimeValue.get(Calendar.MONTH),
                    startTimeValue.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(startTimeValue.get(Calendar.HOUR_OF_DAY), startTimeValue.get(Calendar.MINUTE),
                    startTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Name:");
        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.setText(nameValue);

        return container;
    }

    @Override
    protected void okPressed() {
        getButton(IDialogConstants.OK_ID).setEnabled(false);

        ClientInfo ci = ManagementCatalogue.getInstance().getCurrentClientInfo();
        CreateProcessorRequest req = toCreateProcessorRequest(ci);

        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        catalogue.createProcessorRequest(ci.getInstance(), req).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    CreateReplayDialog.super.okPressed();
                });
            } else {
                log.log(Level.SEVERE, "Could not start replay", exc);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay",
                            exc.getMessage());
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                });
            }
        });
    }

    public String getName() {
        return nameValue;
    }

    public void initialize(TimeInterval interval) {
        startTimeValue = TimeEncoding.toCalendar(interval.calculateStart());
        startTimeValue.setTimeZone(TimeCatalogue.getInstance().getTimeZone());
    }

    public CreateProcessorRequest toCreateProcessorRequest(ClientInfo ci) {
        ReplayRequest spec = ReplayRequest.newBuilder()
                .setStart(TimeEncoding.fromCalendar(RCPUtils.toCalendar(startDate, startTime)))
                .build();

        StringWriter writer = new StringWriter();
        try {
            JsonIOUtil.writeTo(writer, spec, SchemaYamcs.ReplayRequest.WRITE, false);
        } catch (IOException e) {
            throw new RuntimeException("Could not encode replay request", e);
        }
        String specJson = writer.toString();

        CreateProcessorRequest.Builder resultb = CreateProcessorRequest.newBuilder()
                .setName(name.getText())
                .setType("Archive")
                .setConfig(specJson)
                .addClientId(ci.getId());

        return resultb.build();
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
