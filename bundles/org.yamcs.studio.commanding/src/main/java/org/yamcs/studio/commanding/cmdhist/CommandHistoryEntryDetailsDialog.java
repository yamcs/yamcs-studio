package org.yamcs.studio.commanding.cmdhist;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.studio.core.StringConverter;
import org.yamcs.studio.core.YamcsPlugin;

public class CommandHistoryEntryDetailsDialog extends TrayDialog {

    private SashForm sashForm;

    private Text originLabel;
    private Text dateLabel;
    private Text userLabel;
    private Label completedImageLabel;
    private Label completedLabel;
    private Text binaryLabel;
    private Text commentLabel;
    private Text commandStringText;

    private Button prevButton;
    private Button nextButton;

    private CommandHistoryView commandHistoryView;

    private CommandHistoryRecord rec;
    private CommandHistoryRecord previousRec;
    private CommandHistoryRecord nextRec;

    private AckTableViewer localAckTableViewer;
    private AckTableViewer extraAckTableViewer;

    public CommandHistoryEntryDetailsDialog(Shell shell, CommandHistoryView commandHistoryView,
            CommandHistoryRecord rec) {
        super(shell);
        setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
        this.commandHistoryView = commandHistoryView;
        updateRecord(rec);
    }

    private void updateRecord(CommandHistoryRecord rec) {
        this.rec = rec;
        previousRec = commandHistoryView.getPreviousRecord(rec);
        nextRec = commandHistoryView.getNextRecord(rec);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Command Details");
    }

    @Override
    public void create() {
        super.create();
        getShell().setSize(600, 550);

        applyDialogFont(buttonBar);
        getButton(IDialogConstants.OK_ID).setFocus();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.OK_ID == buttonId) {
            okPressed();
        } else if (IDialogConstants.CANCEL_ID == buttonId) {
            cancelPressed();
        } else if (IDialogConstants.BACK_ID == buttonId) {
            backPressed();
        } else if (IDialogConstants.NEXT_ID == buttonId) {
            nextPressed();
        }
    }

    private void backPressed() {
        IStructuredSelection sel = new StructuredSelection(previousRec);
        commandHistoryView.getTableViewer().setSelection(sel, true);
        updateRecord(previousRec);
        updateProperties();
        updateButtonState();
    }

    private void nextPressed() {
        IStructuredSelection sel = new StructuredSelection(nextRec);
        commandHistoryView.getTableViewer().setSelection(sel, true);
        updateRecord(nextRec);
        updateProperties();
        updateButtonState();
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

        createSashForm(container);
        createDetailsSection(sashForm);
        createAckSection(sashForm);

        sashForm.setWeights(new int[] { 300, 400 });

        updateProperties();
        updateButtonState();

        Dialog.applyDialogFont(container);
        return container;
    }

    private void createSashForm(Composite parent) {
        sashForm = new SashForm(parent, SWT.VERTICAL);
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        sashForm.setLayout(layout);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        sashForm.setSashWidth(10);
    }

    private void createDetailsSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 200;
        container.setLayoutData(data);

        createTextSection(container);
        createToolbarButtonBar(container);
    }

    private void createAckSection(Composite parent) {
        Composite ackContainer = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        ackContainer.setLayout(layout);
        ackContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label ackLabel = new Label(ackContainer, SWT.NONE);
        ackLabel.setText("Yamcs acknowledgments:");
        createLocalAckTable(ackContainer);

        ackLabel = new Label(ackContainer, SWT.NONE);
        ackLabel.setText("Extra acknowledgments:");
        createExtraAckTable(ackContainer);
    }

    private void createLocalAckTable(Composite parent) {
        Composite tableContainer = new Composite(parent, SWT.NONE);
        tableContainer.setLayout(new FillLayout());
        tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        localAckTableViewer = new AckTableViewer(tableContainer, commandHistoryView);
    }

    private void createExtraAckTable(Composite parent) {
        Composite tableContainer = new Composite(parent, SWT.NONE);
        tableContainer.setLayout(new FillLayout());
        tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        extraAckTableViewer = new AckTableViewer(tableContainer, commandHistoryView);
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
        dateLabel = new Text(textContainer, SWT.BORDER);
        dateLabel.setEditable(false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dateLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Completion");
        completedImageLabel = new Label(textContainer, SWT.NONE);
        completedLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        completedLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("User");
        userLabel = new Text(textContainer, SWT.BORDER);
        userLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        userLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Origin");
        originLabel = new Text(textContainer, SWT.BORDER);
        originLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        originLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Binary");
        binaryLabel = new Text(textContainer, SWT.BORDER);
        binaryLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        binaryLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Comment");
        commentLabel = new Text(textContainer, SWT.BORDER);
        commentLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        commentLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Command String");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);
        commandStringText = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        commandStringText.setEditable(false);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.grabExcessVerticalSpace = true;
        commandStringText.setLayoutData(gd);
    }

    private void createToolbarButtonBar(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        // layout.numColumns = 1;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        ((GridData) comp.getLayoutData()).verticalAlignment = SWT.BOTTOM;

        Composite container = new Composite(comp, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        prevButton = createButton(container, IDialogConstants.BACK_ID, "", false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        prevButton.setLayoutData(gd);
        prevButton.setToolTipText("Previous Entry");
        prevButton.setImage(commandHistoryView.prevImage);

        nextButton = createButton(container, IDialogConstants.NEXT_ID, "", false);
        gd = new GridData();
        nextButton.setLayoutData(gd);
        nextButton.setToolTipText("Next Entry");
        nextButton.setImage(commandHistoryView.nextImage);

        layout.numColumns = 1;
    }

    private void updateProperties() {
        dateLabel.setText(YamcsPlugin.getDefault().formatInstant(rec.getGenerationTime()));
        commandStringText.setText(rec.getCommandString());

        userLabel.setText(rec.getUsername());

        if (rec.getOrigin() != null && !"".equals(rec.getOrigin())) {
            originLabel.setText(rec.getOrigin());
        } else {
            originLabel.setText("-");
        }

        if (rec.getComment() != null) {
            commentLabel.setText(rec.getComment());
        } else {
            commentLabel.setText("-");
        }

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
            binaryLabel.setText(hexString);
        } else {
            binaryLabel.setText("");
        }

        localAckTableViewer.setInput(rec.getLocalAcknowledgments().toArray());
        extraAckTableViewer.setInput(rec.getExtraAcknowledgments().toArray());
    }

    private void updateButtonState() {
        prevButton.setEnabled(previousRec != null);
        nextButton.setEnabled(nextRec != null);
    }
}
