package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.studio.ui.RCPUtils;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;

public class CommandStackView extends ViewPart {

    public static final String ID = "org.yamcs.studio.ui.commanding.stack.CommandStackView";

    private CommandStackTableViewer commandTableViewer;

    private FormToolkit tk;
    private ScrolledForm form;

    private Styler bracketStyler;
    private Styler argNameStyler;
    private Styler numberStyler;
    private Styler errorStyler;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        tk = new FormToolkit(parent.getDisplay());

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        Color errorBackgroundColor = resourceManager.createColor(new RGB(255, 221, 221));
        bracketStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
            }
        };
        argNameStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
            }
        };
        numberStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
            }
        };
        errorStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.background = errorBackgroundColor;
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_RED);
            }
        };

        SashForm sash = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
        sash.setLayout(new FillLayout());

        Composite tableWrapper = new Composite(sash, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(tableWrapper, tcl, this);

        Composite bottomPane = new Composite(sash, SWT.NONE);
        bottomPane.setLayout(new FillLayout());

        form = tk.createScrolledForm(bottomPane);
        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 4;
        layout.leftMargin = 10;
        layout.rightMargin = 10;
        layout.topMargin = 10;
        layout.bottomMargin = 10;
        form.getBody().setLayout(layout);
        createParametersSection();
        createConstraintsSection();
        createLogSection();
        commandTableViewer.addDoubleClickListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                EditStackedCommandDialog dialog = new EditStackedCommandDialog(parent.getShell(), (StackedCommand) sel.getFirstElement());
                if (dialog.open() == Window.OK) {
                    refreshState();
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
                        refreshState();
                    }
                }
            }
        });

        refreshCommandStackState();
        sash.setWeights(new int[] { 60, 40 });
    }

    public Styler getBracketStyler() {
        return bracketStyler;
    }

    public Styler getArgNameStyler() {
        return argNameStyler;
    }

    public Styler getNumberStyler() {
        return numberStyler;
    }

    public Styler getErrorStyler() {
        return errorStyler;
    }

    private Section createParametersSection() {
        Section section = tk.createSection(form.getForm().getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        section.setLayoutData(td);
        section.setText("Parameters");

        // Wrap composite in an extra gridlayout (TableWrapData doesn't support preffered width)
        Composite sectionClient = tk.createComposite(section);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        sectionClient.setLayout(gl);

        Composite composite = tk.createComposite(sectionClient);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200; // Preferred, therefore only log area will grow to available
        composite.setLayoutData(gd);
        composite.setLayout(new GridLayout(2, false));

        section.setClient(sectionClient);
        return section;
    }

    private Section createConstraintsSection() {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        section.setLayoutData(td);
        section.setText("Transmission Constraints");
        Composite sectionClient = tk.createComposite(section);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        sectionClient.setLayout(gl);

        Composite composite = tk.createComposite(sectionClient);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200; // Preferred, therefore only log area will grow to available
        gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        composite.setLayout(gl);

        tk.createLabel(composite, "Conditions");
        Label lbl = tk.createLabel(composite, "ab\ncd", SWT.WRAP);
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        tk.createLabel(composite, "Timeout (ms)");
        lbl = tk.createLabel(composite, "10000");
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        section.setClient(sectionClient);
        return section;
    }

    private Section createLogSection() {
        Section section = tk.createSection(form.getForm().getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        section.setLayoutData(td);
        section.setText("Log");
        Composite sectionClient = tk.createComposite(section);
        sectionClient.setLayout(new FillLayout());

        Text logArea = new Text(sectionClient, SWT.READ_ONLY);

        section.setClient(sectionClient);
        return section;
    }

    public void addTelecommand(StackedCommand command) {
        commandTableViewer.addTelecommand(command);
        refreshState();
        refreshCommandStackState();
    }

    private void refreshState() {
        commandTableViewer.refresh();

        CommandStack stack = CommandStack.getInstance();
        if (stack.getActiveCommand() != null)
            stack.getActiveCommand().setState(State.UNARMED);

        form.reflow(true);
    }

    private void refreshCommandStackState() {
        CommandStackStateProvider executionStateProvider = RCPUtils.findSourceProvider(
                getSite(), CommandStackStateProvider.STATE_KEY_ARMED, CommandStackStateProvider.class);
        executionStateProvider.refreshState(CommandStack.getInstance());
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
