package org.yamcs.studio.core.archive;

import java.util.Calendar;
import java.util.TimeZone;

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
import org.yamcs.utils.TimeEncoding;

public class TagDialog extends TitleAreaDialog {

    private LocalResourceManager resourceManager;
    private Cursor handCursor;

    private Text tag;
    private String tagValue = "My tag";

    private DateTime startDate;
    private DateTime startTime;
    private Calendar startTimeValue;
    private Button startClosed;

    private DateTime stopDate;
    private DateTime stopTime;
    private Calendar stopTimeValue;
    private Button stopClosed;

    private Label colorSelector;
    private RGB colorValue = new RGB(255, 200, 0); // Orange

    private Text description;
    private String descriptionValue = "";

    public TagDialog(Shell parentShell) {
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
            Calendar start = TagDialog.toCalendar(startDate, startTime);
            Calendar stop = TagDialog.toCalendar(stopDate, stopTime);
            if (start.after(stop)) {
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
                if (newColor != null)
                    colorSelector.setBackground(resourceManager.createColor(newColor));
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
            startDate.setDate(startTimeValue.get(Calendar.YEAR), startTimeValue.get(Calendar.MONTH), startTimeValue.get(Calendar.DAY_OF_MONTH));
            startTime.setTime(startTimeValue.get(Calendar.HOUR_OF_DAY), startTimeValue.get(Calendar.MINUTE), startTimeValue.get(Calendar.SECOND));
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
            stopDate.setDate(stopTimeValue.get(Calendar.YEAR), stopTimeValue.get(Calendar.MONTH), stopTimeValue.get(Calendar.DAY_OF_MONTH));
            stopTime.setTime(stopTimeValue.get(Calendar.HOUR_OF_DAY), stopTimeValue.get(Calendar.MINUTE), stopTimeValue.get(Calendar.SECOND));
        }

        return container;
    }

    private static Calendar toCalendar(DateTime dateWidget, DateTime timeWidget) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Save our stuff, because everything is gonna get disposed.
     */
    @Override
    protected void okPressed() {
        tagValue = tag.getText();
        descriptionValue = description.getText();
        colorValue = colorSelector.getBackground().getRGB();
        startTimeValue = (startClosed.getSelection()) ? toCalendar(startDate, startTime) : null;
        stopTimeValue = (stopClosed.getSelection()) ? toCalendar(stopDate, stopTime) : null;
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
        if (tag.hasStart())
            setStartTime(tag.getStart());
        if (tag.hasStop())
            setStopTime(tag.getStop());
        descriptionValue = (tag.hasDescription()) ? tag.getDescription() : "";
        if (tag.hasColor())
            colorValue = TagTimeline.toRGB(tag);
    }

    public void setStartTime(long startTime) {
        startTimeValue = TimeEncoding.toCalendar(startTime);
        if (startTimeValue != null)
            startTimeValue.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void setStopTime(long stopTime) {
        stopTimeValue = TimeEncoding.toCalendar(stopTime);
        if (stopTimeValue != null)
            stopTimeValue.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public ArchiveTag buildArchiveTag() {
        ArchiveTag.Builder atb = ArchiveTag.newBuilder();
        atb.setName(tagValue);
        atb.setColor(String.format("#%02x%02x%02x", colorValue.red, colorValue.green, colorValue.blue));
        atb.setStart((startTimeValue != null) ? TimeEncoding.fromCalendar(startTimeValue) : TimeEncoding.INVALID_INSTANT);
        atb.setStop((stopTimeValue != null) ? TimeEncoding.fromCalendar(stopTimeValue) : TimeEncoding.INVALID_INSTANT);
        if (!descriptionValue.isEmpty())
            atb.setDescription(descriptionValue);
        return atb.build();
    }

    @Override
    public boolean close() {
        resourceManager.dispose();
        handCursor.dispose();
        return super.close();
    }
}
