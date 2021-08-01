package org.yamcs.studio.archive;

import java.time.Instant;
import java.util.Date;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CustomizeRangeDialog extends TitleAreaDialog {

    private CDateTime startDate;
    private Instant startTimeValue;

    private CDateTime stopDate;
    private Instant stopTimeValue;

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
        if (startDate.getSelection().after(stopDate.getSelection())) {
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

        Composite startLabelWrapper = new Composite(container, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        startLabelWrapper.setLayout(gl);
        Label lbl = new Label(startLabelWrapper, SWT.NONE);
        lbl.setText("Start");

        startDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        startDate.addListener(SWT.Selection, e -> validate());
        if (startTimeValue != null) {
            startDate.setSelection(Date.from(startTimeValue));
        }

        Composite stopLabelWrapper = new Composite(container, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        stopLabelWrapper.setLayout(gl);
        lbl = new Label(stopLabelWrapper, SWT.NONE);
        lbl.setText("Stop");

        stopDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        stopDate.addListener(SWT.Selection, e -> validate());
        if (stopTimeValue != null) {
            stopDate.setSelection(Date.from(stopTimeValue));
        }

        return container;
    }

    /**
     * Save our stuff, because everything is gonna get disposed
     */
    @Override
    protected void okPressed() {
        startTimeValue = startDate.getSelection().toInstant();
        stopTimeValue = stopDate.getSelection().toInstant();
        super.okPressed();
    }

    public void setInitialRange(Instant start, Instant stop) {
        startTimeValue = start;
        stopTimeValue = stop;
    }

    public Instant getStartTime() {
        return startTimeValue;
    }

    public Instant getStopTime() {
        return stopTimeValue;
    }
}
