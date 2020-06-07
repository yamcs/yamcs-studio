package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class CustomizeRangeDialog extends TitleAreaDialog {

    private DateTime startDate;
    private DateTime startTime;
    private Instant startTimeValue;
    private Button startClosed;

    private DateTime stopDate;
    private DateTime stopTime;
    private Instant stopTimeValue;
    private Button stopClosed;

    private boolean startClosedValue;
    private boolean stopClosedValue;

    public CustomizeRangeDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Load archive data");
    }

    private void validate() {
        String errorMessage = null;
        if (!startClosed.getSelection() && !stopClosed.getSelection()) {
            errorMessage = "At least one of start or stop has to be specified";
        } else if (startClosed.getSelection() && stopClosed.getSelection()) {
            Instant start = RCPUtils.toInstant(startDate, startTime);
            Instant stop = RCPUtils.toInstant(stopDate, stopTime);
            if (start.isAfter(stop)) {
                errorMessage = "Stop has to be greater than start";
            }
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

        Composite startLabelWrapper = new Composite(container, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        startLabelWrapper.setLayout(gl);
        Label lbl = new Label(startLabelWrapper, SWT.NONE);
        lbl.setText("Start");
        startClosed = new Button(startLabelWrapper, SWT.CHECK | SWT.NONE);
        startClosed.addListener(SWT.Selection, e -> {
            startDate.setVisible(startClosed.getSelection());
            startTime.setVisible(startClosed.getSelection());
            validate();
        });
        startClosed.setSelection(startClosedValue);

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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(startTimeValue, TimeCatalogue.getInstance().getZoneId());
            Calendar cal = GregorianCalendar.from(zdt);
            startDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }
        startDate.setVisible(startClosedValue);
        startTime.setVisible(startClosedValue);

        Composite stopLabelWrapper = new Composite(container, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        stopLabelWrapper.setLayout(gl);
        lbl = new Label(stopLabelWrapper, SWT.NONE);
        lbl.setText("Stop");
        stopClosed = new Button(stopLabelWrapper, SWT.CHECK | SWT.NONE);
        stopClosed.addListener(SWT.Selection, e -> {
            stopDate.setVisible(stopClosed.getSelection());
            stopTime.setVisible(stopClosed.getSelection());
            validate();
        });
        stopClosed.setSelection(stopClosedValue);

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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(stopTimeValue, TimeCatalogue.getInstance().getZoneId());
            Calendar cal = GregorianCalendar.from(zdt);
            stopDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            stopTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }
        stopDate.setVisible(stopClosedValue);
        stopTime.setVisible(stopClosedValue);

        return container;
    }

    /**
     * Save our stuff, because everything is gonna get disposed
     */
    @Override
    protected void okPressed() {
        startTimeValue = (startClosed.getSelection()) ? RCPUtils.toInstant(startDate, startTime) : null;
        startClosedValue = startClosed.getSelection();
        stopTimeValue = (stopClosed.getSelection()) ? RCPUtils.toInstant(stopDate, stopTime) : null;
        stopClosedValue = stopClosed.getSelection();
        super.okPressed();
    }

    public void setInitialRange(TimeInterval range) {
        startClosedValue = range.hasStart();
        stopClosedValue = range.hasStop();
        startTimeValue = range.calculateStart();
        stopTimeValue = range.calculateStop();
    }

    public boolean hasStartTime() {
        return startClosedValue;
    }

    public boolean hasStopTime() {
        return stopClosedValue;
    }

    public Instant getStartTime() {
        return startTimeValue;
    }

    public Instant getStopTime() {
        return stopTimeValue;
    }
}
