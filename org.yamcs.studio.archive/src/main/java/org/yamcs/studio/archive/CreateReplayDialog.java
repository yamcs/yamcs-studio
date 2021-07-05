package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.CreateProcessorRequest;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.RCPUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CreateReplayDialog extends TitleAreaDialog {

    private static final Logger log = Logger.getLogger(CreateReplayDialog.class.getName());

    // TODO look instead at current list of processors, and find something new
    private static AtomicInteger replayCounter = new AtomicInteger();

    private Text name;
    private String nameValue = "replay" + replayCounter.incrementAndGet();

    private DateTime startDate;
    private DateTime startTime;
    private Instant startTimeValue;

    private DateTime stopDate;
    private DateTime stopTime;
    private Instant stopTimeValue;

    private Button stepByStepButton;

    private TableViewer ppTable;
    private List<String> ppValue;

    private CreateProcessorRequest request;

    public CreateReplayDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Start a new replay");
        // setMessage("Replays can be joined by other users", IMessageProvider.INFORMATION);
    }

    private void validate() {
        String errorMessage = null;
        Instant start = RCPUtils.toInstant(startDate, startTime);
        Instant stop = RCPUtils.toInstant(stopDate, stopTime);
        if (start.isAfter(stop)) {
            errorMessage = "Stop has to be greater than start";
        }

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
        lbl.setText("Start:");
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(startTimeValue, YamcsPlugin.getZoneId());
            Calendar cal = GregorianCalendar.from(zdt);
            startDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Stop:");
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(stopTimeValue, YamcsPlugin.getZoneId());
            Calendar cal = GregorianCalendar.from(zdt);
            stopDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            stopTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Name:");
        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.setText(nameValue);

        lbl = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.verticalAlignment = SWT.CENTER;
        gd.heightHint = 20;
        lbl.setLayoutData(gd);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Packets:");
        Button includePackets = new Button(container, SWT.CHECK);
        includePackets.setText("All");
        includePackets.setLayoutData(new GridData());
        includePackets.setSelection(true);
        includePackets.setEnabled(false);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Parameter Groups:");
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);
        Composite tableWrapper = new Composite(container, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableWrapper.setLayout(gl);

        ppTable = CheckboxTableViewer.newCheckList(tableWrapper, SWT.V_SCROLL | SWT.BORDER);
        ppTable.setContentProvider(ArrayContentProvider.getInstance());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5 * ppTable.getTable().getItemHeight();
        ppTable.getTable().setLayoutData(gd);
        ppTable.setInput(ppValue);
        for (TableItem item : ppTable.getTable().getItems()) {
            item.setChecked(false);
        }

        lbl = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.CENTER;
        gd.heightHint = 20;
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        stepByStepButton = new Button(container, SWT.CHECK);
        gd = new GridData();
        gd.horizontalSpan = 2;
        stepByStepButton.setLayoutData(gd);
        stepByStepButton.setText("Enable step-by-step mode (pauses after each data frame)");
        stepByStepButton.setSelection(false);

        return container;
    }

    @Override
    protected void okPressed() {
        getButton(IDialogConstants.OK_ID).setEnabled(false);

        request = toCreateProcessorRequest();
        YamcsClient client = YamcsPlugin.getYamcsClient();
        client.createProcessor(request).whenComplete((processorClient, exc) -> {
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

    public CreateProcessorRequest getRequest() {
        return request;
    }

    public void initialize(TimeInterval interval, List<String> pps) {
        startTimeValue = interval.calculateStart();
        stopTimeValue = interval.calculateStop();
        ppValue = pps;
    }

    private CreateProcessorRequest toCreateProcessorRequest() {
        JsonObject spec = new JsonObject();
        spec.addProperty("start", RCPUtils.toInstant(startDate, startTime).toString());
        spec.addProperty("stop", RCPUtils.toInstant(stopDate, stopTime).toString());

        spec.add("packetRequest", new JsonObject());

        JsonArray ppFilters = new JsonArray();
        for (TableItem item : ppTable.getTable().getItems()) {
            if (item.getChecked()) {
                ppFilters.add(item.getText());
            }
        }

        if (ppFilters.size() > 0) {
            JsonObject ppObj = new JsonObject();
            ppObj.add("groupNameFilter", ppFilters);
            spec.add("ppRequest", ppObj);
        }

        if (stepByStepButton.getSelection()) {
            JsonObject speed = new JsonObject();
            speed.addProperty("type", "STEP_BY_STEP");
            spec.add("speed", speed);
        }

        String specJson = new Gson().toJson(spec);
        CreateProcessorRequest.Builder resultb = CreateProcessorRequest.newBuilder()
                .setInstance(YamcsPlugin.getInstance())
                .setName(name.getText())
                .setType("Archive")
                .setPersistent(true) // TODO temp
                .setConfig(specJson);

        return resultb.build();
    }
}
