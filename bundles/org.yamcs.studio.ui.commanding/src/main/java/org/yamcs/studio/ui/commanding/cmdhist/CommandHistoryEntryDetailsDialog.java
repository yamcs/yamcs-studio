package org.yamcs.studio.ui.commanding.cmdhist;

import java.text.DateFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.utils.StringConverter;

public class CommandHistoryEntryDetailsDialog extends TrayDialog {

    private Label sourceLabel;
    private Label dateLabel;
    private Label completedImageLabel;
    private Label completedLabel;
    private Label commandStringLabel;
    private Label sourceIdLabel;
    private Text commandBinary;

    private CommandHistoryView commandHistoryView;
    private CommandHistoryRecord rec;

    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public CommandHistoryEntryDetailsDialog(Shell shell, CommandHistoryView commandHistoryView,
            CommandHistoryRecord rec) {
        super(shell);
        setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
        this.commandHistoryView = commandHistoryView;
        this.rec = rec;
    }

    @Override
    public void create() {
        super.create();
        getShell().setSize(500, 550);

        applyDialogFont(buttonBar);
        getButton(IDialogConstants.OK_ID).setFocus();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        createTextSection(container);

        updateProperties();
        Dialog.applyDialogFont(container);
        return container;
    }

    private void createTextSection(Composite parent) {
        Composite textContainer = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = layout.marginWidth = 0;
        textContainer.setLayout(layout);
        textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(textContainer, SWT.NONE);
        label.setText("Date");
        dateLabel = new Label(textContainer, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dateLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Completed");
        completedImageLabel = new Label(textContainer, SWT.NONE);
        completedLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        completedLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Command String");
        commandStringLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        commandStringLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Source");
        sourceLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        sourceLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Source ID");
        sourceIdLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        sourceIdLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Binary");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);
        commandBinary = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        commandBinary.setEditable(false);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.grabExcessVerticalSpace = true;
        commandBinary.setLayoutData(gd);
    }

    private void updateProperties() {
        dateLabel.setText(rec.getGenerationTime());
        commandStringLabel.setText(rec.getCommandString());
        sourceLabel.setText(rec.getUsername() + "@" + rec.getOrigin());
        sourceIdLabel.setText(String.valueOf(rec.getSequenceNumber()));

        switch (rec.getCommandState()) {
        case COMPLETED:
            completedImageLabel.setImage(commandHistoryView.checkmarkImage);
            completedLabel.setText("Completed");
            break;
        case FAILED:
            completedImageLabel.setImage(commandHistoryView.errorImage);
            completedLabel.setText(rec.getPTVInfo().getFailureMessage());
            break;
        default:
            completedImageLabel.setImage(null);
            completedLabel.setText("");
        }

        if (rec.getBinary() != null) {
            String hexString = StringConverter.arrayToHexString(rec.getBinary().toByteArray());
            commandBinary.setText(hexString);
        } else {
            commandBinary.setText("");
        }
    }
}
