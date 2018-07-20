package org.yamcs.studio.eventlog;

import java.util.Calendar;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.security.YamcsAuthorizations;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

public class AddManualEventDialog extends TitleAreaDialog {

    static int sequenceNumber = 0;
    Calendar generationTimeValue = null;

    private Text messageText;
    private DateTime generationDatePicker;
    private DateTime generationTimePicker;
    Label userLbl = null;
    Text userText = null;
    Combo severityCombo;

    protected AddManualEventDialog(Shell shell) {
        super(shell);
    }

    protected AddManualEventDialog(Shell shell, long generationTime) {
        super(shell);
        generationTimeValue = TimeEncoding.toCalendar(generationTime);
        // defaultGenerationTime = generationTime;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add a Manual Event");
    }

    // private void validate() {
    // String errorMessage = null;
    // Calendar start = RCPUtils.toCalendar(startDate, startTime);
    // Calendar stop = RCPUtils.toCalendar(stopDate, stopTime);
    // if (start.after(stop))
    // errorMessage = "Stop has to be greater than start";
    //
    // setErrorMessage(errorMessage);
    // getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
    // }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;

        layout.verticalSpacing = 5;
        container.setLayout(layout);

        Label lbl = new Label(container, SWT.NONE);
        lbl.setText("Generation Time:");
        Composite startComposite = new Composite(container, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        startComposite.setLayout(rl);
        generationDatePicker = new DateTime(startComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        // generationDatePicker.addListener(SWT.Selection, e -> validate());
        // generationDatePicker.addListener(SWT.FocusOut, e -> validate());
        generationTimePicker = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        // generationTimePicker.addListener(SWT.Selection, e -> validate());
        // generationTimePicker.addListener(SWT.FocusOut, e -> validate());
        if (generationTimeValue != null) {
            generationDatePicker.setDate(generationTimeValue.get(Calendar.YEAR),
                    generationTimeValue.get(Calendar.MONTH), generationTimeValue.get(Calendar.DAY_OF_MONTH));
            generationTimePicker.setTime(generationTimeValue.get(Calendar.HOUR_OF_DAY),
                    generationTimeValue.get(Calendar.MINUTE), generationTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Source:");
        lbl = new Label(container, SWT.NONE);
        lbl.setText("Manual");

        lbl = new Label(container, SWT.NONE);
        lbl.setText("User:");
        if (YamcsAuthorizations.getInstance().isAuthorizationEnabled()) {
            userLbl = new Label(container, SWT.NONE);
            userLbl.setText(YamcsAuthorizations.getInstance().getUsername());
        } else {
            userText = new Text(container, SWT.SINGLE);
            userText.setLayoutData(new GridData(GridData.FILL_BOTH));
            userText.setText("");
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Message:");
        messageText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.verticalAlignment = SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        messageText.setLayoutData(data);
        GC gc = new GC(messageText);
        try {
            gc.setFont(messageText.getFont());
            FontMetrics fm = gc.getFontMetrics();

            /* Set the height to 5 rows of characters */
            data.heightHint = 5 * fm.getHeight();
        } finally {
            gc.dispose();
        }
        messageText.setText("");

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Severity:");
        severityCombo = new Combo(container, SWT.DROP_DOWN);
        severityCombo.add(EventSeverity.INFO.name(), EventSeverity.INFO_VALUE);
        severityCombo.add(EventSeverity.WARNING.name(), EventSeverity.WARNING_VALUE);
        severityCombo.add(EventSeverity.ERROR.name(), EventSeverity.ERROR_VALUE);
        severityCombo.select(EventSeverity.INFO_VALUE);

        return container;
    }

    @Override
    protected void okPressed() {
        String source = "Manual";
        if (userText != null && !userText.getText().isEmpty()) {
            source += " :: " + userText.getText();
        } else if (userLbl != null) {
            source += " :: " + userLbl.getText();
        }
        String message = messageText.getText();
        long generationTime = TimeEncoding
                .fromCalendar(RCPUtils.toCalendar(generationDatePicker, generationTimePicker));
        long receptionTime = TimeCatalogue.getInstance().getMissionTime();
        EventSeverity severity = EventSeverity.internalGetValueMap()
                .findValueByNumber(severityCombo.getSelectionIndex() > 3 ? severityCombo.getSelectionIndex() + 1
                        : severityCombo.getSelectionIndex());

        EventCatalogue catalogue = EventCatalogue.getInstance();
        try {
            catalogue.createEvent(source, sequenceNumber++, message, generationTime, receptionTime, severity)
                    .whenComplete((data, exc) -> {
                        if (exc == null) {
                            Display.getDefault().asyncExec(() -> {
                                // MessageBox m = new MessageBox(getShell(),
                                // SWT.OK | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL);
                                // m.setText("Ok");
                                // m.setMessage("Added the manual event successfully.\n" + new String(data));
                                // m.open();
                                close();
                            });
                        } else {
                            Display.getDefault().asyncExec(() -> {
                                MessageBox m = new MessageBox(getShell(),
                                        SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
                                m.setText("Error");
                                m.setMessage(exc.getMessage());
                                m.open();
                            });
                        }
                    });
        } catch (Exception e) {
            Display.getDefault().asyncExec(() -> {
                MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
                m.setText("Error");
                m.setMessage("Error: " + e.getMessage());
                m.open();
            });
        }

    }

}
