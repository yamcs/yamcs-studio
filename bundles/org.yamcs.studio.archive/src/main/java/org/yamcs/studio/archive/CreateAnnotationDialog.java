package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.studio.core.model.TimeCatalogue;

import com.google.protobuf.Timestamp;

public class CreateAnnotationDialog extends TitleAreaDialog {

    private LocalResourceManager resourceManager;
    private Cursor handCursor;

    private Text tag;
    private String tagValue = "My annotation";

    private DateTime startDate;
    private DateTime startTime;
    private Instant startTimeValue;
    private Button startClosed;

    private DateTime stopDate;
    private DateTime stopTime;
    private Instant stopTimeValue;
    private Button stopClosed;

    private Label colorSelector;
    private RGB colorValue = new RGB(255, 200, 0); // Orange

    private Text description;
    private String descriptionValue = "";

    public CreateAnnotationDialog(Shell parentShell) {
        super(parentShell);
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parentShell);
        handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Annotate time range");
        setMessage("Annotations are stored in Yamcs and shared with other users", IMessageProvider.INFORMATION);
    }

    private void validate() {
        String errorMessage = null;
        if (!startClosed.getSelection() && !stopClosed.getSelection()) {
            errorMessage = "At least one of start or stop has to be specified";
        } else if (startClosed.getSelection() && stopClosed.getSelection()) {
            Instant start = toInstant(startDate, startTime);
            Instant stop = toInstant(stopDate, stopTime);
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

        Label lbl = new Label(container, SWT.NONE);
        lbl.setText("Name");

        Composite tagAndColorWrapper = new Composite(container, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        tagAndColorWrapper.setLayoutData(gd);

        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tagAndColorWrapper.setLayout(gl);
        tag = new Text(tagAndColorWrapper, SWT.BORDER);
        tag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tag.setText(tagValue);

        // Some ugly tricks to get a border around a label, which should have been a button in the first place
        // but at least OSX doesn't support buttons with custom backgrounds. May be getting time to draw GC ourselves..
        Composite labelBorder = new Composite(tagAndColorWrapper, SWT.BORDER);
        labelBorder.setLayout(new FillLayout());
        colorSelector = new Label(labelBorder, SWT.NONE);
        colorSelector.setText("         ");
        colorSelector.setCursor(handCursor);
        colorSelector.setBackground(resourceManager.createColor(colorValue));
        colorSelector.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ColorDialog colorDialog = new ColorDialog(colorSelector.getShell());
                colorDialog.setRGB(colorSelector.getBackground().getRGB());
                RGB newColor = colorDialog.open();
                if (newColor != null) {
                    colorSelector.setBackground(resourceManager.createColor(newColor));
                }
            }
        });

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Description");
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);
        description = new Text(container, SWT.MULTI | SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = description.getLineHeight() * 3;
        description.setLayoutData(gd);
        description.setText(descriptionValue);

        Composite startLabelWrapper = new Composite(container, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        startLabelWrapper.setLayout(gl);
        lbl = new Label(startLabelWrapper, SWT.NONE);
        lbl.setText("Start");
        startClosed = new Button(startLabelWrapper, SWT.CHECK | SWT.NONE);
        startClosed.setSelection(true);
        startClosed.addListener(SWT.Selection, e -> {
            startDate.setVisible(startClosed.getSelection());
            startTime.setVisible(startClosed.getSelection());
            validate();
        });

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

        Composite stopLabelWrapper = new Composite(container, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        stopLabelWrapper.setLayout(gl);
        lbl = new Label(stopLabelWrapper, SWT.NONE);
        lbl.setText("Stop");
        stopClosed = new Button(stopLabelWrapper, SWT.CHECK | SWT.NONE);
        stopClosed.setSelection(true);
        stopClosed.addListener(SWT.Selection, e -> {
            stopDate.setVisible(stopClosed.getSelection());
            stopTime.setVisible(stopClosed.getSelection());
            validate();
        });

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

        return container;
    }

    private static Instant toInstant(DateTime dateWidget, DateTime timeWidget) {
        Calendar cal = Calendar.getInstance(TimeCatalogue.getInstance().getTimeZone());
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal.toInstant();
    }

    /**
     * Save our stuff, because everything is gonna get disposed.
     */
    @Override
    protected void okPressed() {
        tagValue = tag.getText();
        descriptionValue = description.getText();
        colorValue = colorSelector.getBackground().getRGB();
        startTimeValue = (startClosed.getSelection()) ? toInstant(startDate, startTime) : null;
        stopTimeValue = (stopClosed.getSelection()) ? toInstant(stopDate, stopTime) : null;
        super.okPressed();
    }

    public String getTag() {
        return tagValue;
    }

    public RGB getColor() {
        return colorValue;
    }

    public String getDescription() {
        return descriptionValue;
    }

    public void fillFrom(ArchiveTag tag) {
        tagValue = tag.getName();
        if (tag.hasStartUTC()) {
            startTimeValue = Instant.ofEpochSecond(tag.getStartUTC().getSeconds(), tag.getStartUTC().getNanos());
        }
        if (tag.hasStopUTC()) {
            stopTimeValue = Instant.ofEpochSecond(tag.getStopUTC().getSeconds(), tag.getStopUTC().getNanos());
        }
        descriptionValue = (tag.hasDescription()) ? tag.getDescription() : "";
        if (tag.hasColor()) {
            colorValue = TagTimeline.toRGB(tag);
        }
    }

    public void setStartTime(Instant startTime) {
        startTimeValue = startTime;
    }

    public void setStopTime(Instant stopTime) {
        stopTimeValue = stopTime;
    }

    public ArchiveTag buildArchiveTag() {
        ArchiveTag.Builder atb = ArchiveTag.newBuilder();
        atb.setName(tagValue);
        atb.setColor(String.format("#%02x%02x%02x", colorValue.red, colorValue.green, colorValue.blue));
        if (startTimeValue != null) {
            atb.setStartUTC(Timestamp.newBuilder().setSeconds(startTimeValue.getEpochSecond())
                    .setNanos(startTimeValue.getNano()));
        }
        if (stopTimeValue != null) {
            atb.setStopUTC(Timestamp.newBuilder().setSeconds(stopTimeValue.getEpochSecond())
                    .setNanos(stopTimeValue.getNano()));
        }
        if (!descriptionValue.isEmpty()) {
            atb.setDescription(descriptionValue);
        }
        return atb.build();
    }

    @Override
    public boolean close() {
        resourceManager.dispose();
        handCursor.dispose();
        return super.close();
    }
}
