package org.yamcs.studio.archive;

import java.time.Instant;
import java.util.Date;

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
import org.yamcs.studio.core.YamcsPlugin;

public class JumpToDialog extends TitleAreaDialog {

    private CDateTime date;

    private Instant selectedTime;

    public JumpToDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Jump to a specific time");
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
        lbl.setText("Time:");
        date = new CDateTime(container,
                SWT.BORDER | CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);

        Instant missionTime = YamcsPlugin.getMissionTime();
        if (missionTime != null) {
            date.setSelection(Date.from(missionTime));
        }

        return container;
    }

    @Override
    protected void okPressed() {
        selectedTime = date.getSelection().toInstant();
        super.okPressed();
    }

    public Instant getTime() {
        return selectedTime;
    }
}
