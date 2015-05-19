package org.yamcs.studio.ui.commanding.stack;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
import org.yamcs.studio.ui.YamcsUIPlugin;

public class CommandStackView extends ViewPart {

    private LocalResourceManager resourceManager;
    private CommandStackTableViewer commandTableViewer;
    private Label messagePanel;
    private Label nextCommandLabel;
    private Button armToggle;
    private Button goButton;

    private FormToolkit tk;
    private ScrolledForm form;

    private Image yesImage;

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        yesImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/yes.png"));

        parent.setLayout(new FillLayout());

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout(gl);

        Composite tableWrapper = new Composite(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        tableWrapper.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(tableWrapper, tcl);

        Composite rightPane = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        rightPane.setLayoutData(gd);
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
            nextCommandLabel.setText("Fix all SPTV Checks first.");
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
