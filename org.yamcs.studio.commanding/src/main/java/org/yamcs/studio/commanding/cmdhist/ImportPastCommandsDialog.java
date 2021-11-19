package org.yamcs.studio.commanding.cmdhist;

import java.time.Instant;
import java.util.Calendar;
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

public class ImportPastCommandsDialog extends TitleAreaDialog {

    private CDateTime startDate;
    private Calendar startTimeValue;

    private CDateTime stopDate;
    private Calendar stopTimeValue;

    private Instant start;
    private Instant stop;

    public ImportPastCommandsDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Import Past Commands");
    }

    private void validate() {
        String errorMessage = null;
        Date start = startDate.getSelection();
        Date stop = stopDate.getSelection();
        if (start != null && stop != null && start.after(stop)) {
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
        startDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        startDate.addListener(SWT.Selection, e -> validate());
        var gd = new GridData();
        gd.widthHint = 200;
        startDate.setLayoutData(gd);
        if (startTimeValue != null) {
            startDate.setSelection(startTimeValue.getTime());
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Stop:");
        stopDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        stopDate.addListener(SWT.Selection, e -> validate());
        gd = new GridData();
        gd.widthHint = 200;
        stopDate.setLayoutData(gd);
        if (stopTimeValue != null) {
            stopDate.setSelection(stopTimeValue.getTime());
        }

        return container;
    }

    @Override
    protected void okPressed() {
        if (startDate.hasSelection()) {
            start = startDate.getSelection().toInstant();
        }
        if (stopDate.hasSelection()) {
            stop = stopDate.getSelection().toInstant();
        }
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
