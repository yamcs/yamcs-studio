package org.yamcs.studio.eventlog;

import java.time.Instant;
import java.util.Calendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.utils.RCPUtils;

public class ImportPastEventsDialog extends TitleAreaDialog {

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;

    private DateTime stopDate;
    private DateTime stopTime;
    private Calendar stopTimeValue;

    private Instant start;
    private Instant stop;

    public ImportPastEventsDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Import Past Events");
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
        startDate.addListener(SWT.FocusOut, e -> validate());
        startTime = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        startTime.addListener(SWT.Selection, e -> validate());
        startTime.addListener(SWT.FocusOut, e -> validate());
        if (startTimeValue != null) {
            startDate.setDate(startTimeValue.get(Calendar.YEAR), startTimeValue.get(Calendar.MONTH),
                    startTimeValue.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(startTimeValue.get(Calendar.HOUR_OF_DAY), startTimeValue.get(Calendar.MINUTE),
                    startTimeValue.get(Calendar.SECOND));
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
        stopDate.addListener(SWT.FocusOut, e -> validate());
        stopTime = new DateTime(stopComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        stopTime.addListener(SWT.Selection, e -> validate());
        stopTime.addListener(SWT.FocusOut, e -> validate());
        if (stopTimeValue != null) {
            stopDate.setDate(stopTimeValue.get(Calendar.YEAR), stopTimeValue.get(Calendar.MONTH),
                    stopTimeValue.get(Calendar.DAY_OF_MONTH));
            stopTime.setTime(stopTimeValue.get(Calendar.HOUR_OF_DAY), stopTimeValue.get(Calendar.MINUTE),
                    stopTimeValue.get(Calendar.SECOND));
        }

        return container;
    }

    @Override
    protected void okPressed() {
        start = RCPUtils.toInstant(startDate, startTime);
        stop = RCPUtils.toInstant(stopDate, stopTime);
        super.okPressed();
    }

    public Instant getStart() {
        return start;
    }

    public Instant getStop() {
        return stop;
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
