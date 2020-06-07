package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class JumpToDialog extends TitleAreaDialog {

    private DateTime date;
    private DateTime time;

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
        Composite startComposite = new Composite(container, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        startComposite.setLayout(rl);
        date = new DateTime(startComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        time = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);

        Instant missionTime = YamcsPlugin.getMissionTime();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(missionTime, YamcsPlugin.getZoneId());
        Calendar now = GregorianCalendar.from(zdt);
        if (now != null) {
            date.setDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            time.setTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
        }

        return container;
    }

    @Override
    protected void okPressed() {
        selectedTime = RCPUtils.toInstant(date, time);
        super.okPressed();
    }

    public Instant getTime() {
        return selectedTime;
    }
}
