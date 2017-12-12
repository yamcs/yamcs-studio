package org.yamcs.studio.eventlog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.protobuf.Yamcs;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.utils.TimeEncoding;

public class EventDetailsDialog extends TitleAreaDialog {

    Yamcs.Event event;

    public EventDetailsDialog(Shell parentShell, Yamcs.Event event) {
        super(parentShell);
        this.event = event;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Event Details");

        String titleMessage = "";
        titleMessage += "\tGeneration Time:\t" + TimeEncoding.toString(event.getGenerationTime()) + "\n";
        titleMessage += "\tReception Time:\t" + TimeEncoding.toString(event.getReceptionTime());
        int icon = IMessageProvider.NONE;
        if (event.getSeverity() == EventSeverity.ERROR)
            icon = IMessageProvider.ERROR;
        else if (event.getSeverity() == EventSeverity.WARNING)
            icon = IMessageProvider.WARNING;
        else if (event.getSeverity() == EventSeverity.INFO)
            icon = IMessageProvider.INFORMATION;
        setMessage(titleMessage, icon);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);

        createDetailArea(container);

        return area;
    }

    private void createDetailArea(Composite container)
    {
        org.eclipse.swt.graphics.Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        final StyledText styledText = new StyledText(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        styledText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
        styledText.setEditable(false);
        styledText.setText(event.getMessage());
        styledText.setAlwaysShowScrollBars(false);
        styledText.setFont(terminalFont);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
    }

}
