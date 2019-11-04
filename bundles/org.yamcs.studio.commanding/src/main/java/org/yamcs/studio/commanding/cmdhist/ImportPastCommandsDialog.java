package org.yamcs.studio.commanding.cmdhist;

import java.util.Calendar;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.yamcs.client.ClientException;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.InvalidProtocolBufferException;

public class ImportPastCommandsDialog extends TitleAreaDialog {

    private CommandHistoryView cmdhistView;

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;

    private DateTime stopDate;
    private DateTime stopTime;
    private Calendar stopTimeValue;

    public ImportPastCommandsDialog(Shell parentShell, CommandHistoryView cmdhistView) {
        super(parentShell);
        this.cmdhistView = cmdhistView;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Import Past Commands");
    }

    private void validate() {
        String errorMessage = null;
        Calendar start = RCPUtils.toCalendar(startDate, startTime);
        Calendar stop = RCPUtils.toCalendar(stopDate, stopTime);
        if (start.after(stop)) {
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
        stopTime = new DateTime(stopComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        stopTime.addListener(SWT.Selection, e -> validate());
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
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        if (yamcsClient == null) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not import commands\n",
                    "Disconnected from Yamcs");
            return;
        }

        getButton(IDialogConstants.OK_ID).setEnabled(false);

        long start = TimeEncoding.fromCalendar(RCPUtils.toCalendar(startDate, startTime));
        long stop = TimeEncoding.fromCalendar(RCPUtils.toCalendar(stopDate, stopTime));
        TimeInterval interval = new TimeInterval(start, stop);

        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.downloadCommands(instance, interval, data -> {
            try {
                CommandHistoryEntry commandHistoryEntry = CommandHistoryEntry.parseFrom(data);
                Display.getDefault().asyncExec(() -> {
                    cmdhistView.processCommandHistoryEntry(commandHistoryEntry, true);
                });
            } catch (InvalidProtocolBufferException e) {
                throw new ClientException("Failed to decode server message", e);
            }
        }).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    ImportPastCommandsDialog.super.okPressed();
                });
            } else {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }
        });
    }

    public void initialize(TimeInterval interval, List<String> packets, List<String> ppGroups) {
        startTimeValue = TimeEncoding.toCalendar(interval.calculateStart());
        startTimeValue.setTimeZone(TimeCatalogue.getInstance().getTimeZone());
        stopTimeValue = TimeEncoding.toCalendar(interval.calculateStop());
        stopTimeValue.setTimeZone(TimeCatalogue.getInstance().getTimeZone());
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
