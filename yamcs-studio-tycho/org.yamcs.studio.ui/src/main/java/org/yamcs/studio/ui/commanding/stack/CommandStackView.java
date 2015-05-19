package org.yamcs.studio.ui.commanding.stack;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

public class CommandStackView extends ViewPart {

    private CommandStackTableViewer commandTableViewer;
    private Label messagePanel;
    private Label nextCommandLabel;
    private Button armToggle;
    private Button goButton;

    private FormToolkit tk;
    private ScrolledForm form;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        sash.setLayout(new FillLayout());

        Composite tableWrapper = new Composite(sash, SWT.NONE);
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(tableWrapper, tcl);

        Composite rightPane = new Composite(sash, SWT.NONE);
        rightPane.setLayout(new FillLayout());
        tk = new FormToolkit(rightPane.getDisplay());
        form = tk.createScrolledForm(rightPane);
        form.setText("Stack Status");
        tk.decorateFormHeading(form.getForm());
        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = 15;
        form.getBody().setLayout(layout);
        createSPTVChecksSection(form.getForm());
        createNextCommandSection(form.getForm());

        commandTableViewer.addDoubleClickListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                EditStackedCommandDialog dialog = new EditStackedCommandDialog(parent.getShell(), (Telecommand) sel.getFirstElement());
                if (dialog.open() == Window.OK) {
                    commandTableViewer.refresh();
                    refreshMessagePanel();
                }
            }
        });
        commandTableViewer.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    IStructuredSelection sel = (IStructuredSelection) commandTableViewer.getSelection();
                    if (!sel.isEmpty()) {
                        CommandStack.getInstance().getCommands().removeAll(sel.toList());
                        commandTableViewer.refresh();
                        refreshMessagePanel();
                    }
                }
            }
        });

        sash.setWeights(new int[] { 70, 30 });
    }

    private Section createSPTVChecksSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("SPTV Checks");
        Composite sectionClient = tk.createComposite(section);
        sectionClient.setLayout(new GridLayout());

        messagePanel = tk.createLabel(sectionClient, "Empty Stack");
        messagePanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        section.setClient(sectionClient);
        return section;
    }

    private Section createNextCommandSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Next Command");
        Composite sectionClient = tk.createComposite(section);
        sectionClient.setLayout(new GridLayout());

        nextCommandLabel = tk.createLabel(sectionClient, "Empty Stack");
        nextCommandLabel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite controls = tk.createComposite(sectionClient, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.CENTER;
        controls.setLayoutData(gd);
        controls.setLayout(new RowLayout());
        armToggle = tk.createButton(controls, "Arm", SWT.TOGGLE);
        armToggle.setEnabled(false);
        goButton = tk.createButton(controls, "Issue", SWT.PUSH);
        goButton.setEnabled(false);

        armToggle.addListener(SWT.Selection, evt -> goButton.setEnabled(armToggle.getSelection()));

        section.setClient(sectionClient);
        return section;
    }

    private Section createConstraintsSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Constraints");
        Composite sectionClient = tk.createComposite(section);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        sectionClient.setLayout(gl);

        tk.createLabel(sectionClient, "Transmission Constraints");
        Label lbl = tk.createLabel(sectionClient, "ab\ncd", SWT.WRAP);
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        tk.createLabel(sectionClient, "Timeout");
        lbl = tk.createLabel(sectionClient, "10000");
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        section.setClient(sectionClient);
        return section;
    }

    public void addTelecommand(Telecommand command) {
        commandTableViewer.addTelecommand(command);
        refreshMessagePanel();
    }

    private void refreshMessagePanel() {
        CommandStack stack = CommandStack.getInstance();
        String text = "";
        List<String> errorMessages = stack.getErrorMessages();

        if (stack.getCommands().isEmpty()) {
            messagePanel.setText("Empty Stack");
        } else if (errorMessages.isEmpty()) {
            messagePanel.setText("\u2713 passed");
        } else {
            boolean first = true;
            for (String message : errorMessages) {
                if (!first)
                    text += "\n";
                first = false;
                text += message;
            }
            messagePanel.setText(text);
        }

        if (!errorMessages.isEmpty()) {
            nextCommandLabel.setText("Fix SPTV checks first ");
        } else if (stack.getCommands().isEmpty()) {
            nextCommandLabel.setText("Empty Stack");
        } else {
            Telecommand cmd = stack.getNextCommand();
            nextCommandLabel.setText("#" + stack.indexOf(cmd));
        }

        if (errorMessages.isEmpty() && !stack.getCommands().isEmpty()) {
            armToggle.setSelection(false);
            armToggle.setEnabled(true);
        } else {
            armToggle.setSelection(false);
            armToggle.setEnabled(false);
        }

        form.reflow(true);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        tk.dispose();
        super.dispose();
    }
}
