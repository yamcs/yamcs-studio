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
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

public class AddManualEventDialog extends TitleAreaDialog {

    private Calendar generationTimeValue = null;

    private Text messageText;
    private DateTime generationDatePicker;
    private DateTime generationTimePicker;
    private Combo severityCombo;

    protected AddManualEventDialog(Shell shell) {
        super(shell);
    }

    protected AddManualEventDialog(Shell shell, long generationTime) {
        super(shell);
        generationTimeValue = TimeEncoding.toCalendar(generationTime);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add a Manual Event");
    }

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
        lbl.setText("Message");
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        lbl.setLayoutData(gd);
        gd.verticalAlignment = SWT.TOP;
        messageText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.TOP;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        messageText.setLayoutData(gd);
        GC gc = new GC(messageText);
        try {
            gc.setFont(messageText.getFont());
            FontMetrics fm = gc.getFontMetrics();
            gd.heightHint = 5 * fm.getHeight();
        } finally {
            gc.dispose();
        }
        messageText.setText("");

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Event Time");
        Composite startComposite = new Composite(container, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.marginLeft = 0;
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.center = true;
        startComposite.setLayout(rl);
        generationDatePicker = new DateTime(startComposite, SWT.DATE | SWT.LONG | SWT.DROP_DOWN | SWT.BORDER);
        generationTimePicker = new DateTime(startComposite, SWT.TIME | SWT.LONG | SWT.BORDER);
        if (generationTimeValue != null) {
            generationDatePicker.setDate(generationTimeValue.get(Calendar.YEAR),
                    generationTimeValue.get(Calendar.MONTH), generationTimeValue.get(Calendar.DAY_OF_MONTH));
            generationTimePicker.setTime(generationTimeValue.get(Calendar.HOUR_OF_DAY),
                    generationTimeValue.get(Calendar.MINUTE), generationTimeValue.get(Calendar.SECOND));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Severity");
        severityCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        severityCombo.add(EventSeverity.INFO.name());
        severityCombo.add(EventSeverity.WATCH.name());
        severityCombo.add(EventSeverity.WARNING.name());
        severityCombo.add(EventSeverity.DISTRESS.name());
        severityCombo.add(EventSeverity.CRITICAL.name());
        severityCombo.add(EventSeverity.SEVERE.name());
        severityCombo.select(0);

        return container;
    }

    @Override
    protected void okPressed() {
        String message = messageText.getText();
        Calendar cal = RCPUtils.toCalendar(generationDatePicker, generationTimePicker);
        String severityString = severityCombo.getItem(severityCombo.getSelectionIndex());
        EventSeverity severity = EventSeverity.valueOf(severityString);

        EventCatalogue catalogue = EventCatalogue.getInstance();
        catalogue.createEvent(message, cal.getTime(), severity)
                .whenComplete((data, exc) -> {
                    if (exc == null) {
                        Display.getDefault().asyncExec(() -> close());
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

    }

}
