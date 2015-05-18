package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class CommandStackMasterDetailsBlock extends MasterDetailsBlock implements IDetailsPageProvider {

    private CommandStackView part;
    private CommandStackTableViewer commandStackTableViewer;

    public CommandStackMasterDetailsBlock(CommandStackView part) {
        this.part = part;
    }

    @Override
    public void createContent(IManagedForm managedForm) {
        super.createContent(managedForm);
        sashForm.setWeights(new int[] { 70, 30 });
        sashForm.setOrientation(SWT.HORIZONTAL);
    }

    @Override
    public void createContent(IManagedForm managedForm, Composite parent) {
        super.createContent(managedForm, parent);
        sashForm.setWeights(new int[] { 70, 30 });
        sashForm.setOrientation(SWT.HORIZONTAL);
    }

    @Override
    protected void createMasterPart(IManagedForm managedForm, Composite parent) {
        FormToolkit tk = managedForm.getToolkit();
        //tk.setBorderStyle(SWT.NULL);

        Section section = tk.createSection(parent, SWT.NONE);
        section.clientVerticalSpacing = 0;

        Composite commandTableWithControls = tk.createComposite(section, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        commandTableWithControls.setLayout(gl);

        Composite tableWrapper = new Composite(commandTableWithControls, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        // Work around unwanted h_scroll (https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997#c4)
        gd.widthHint = 1;
        tableWrapper.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        Table t = tk.createTable(tableWrapper, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE); // SINGLE for now
        t.setLinesVisible(true);
        commandStackTableViewer = new CommandStackTableViewer(tcl, t);

        section.setClient(commandTableWithControls);
        SectionPart sectionPart = new SectionPart(section);
        managedForm.addPart(sectionPart);

        commandStackTableViewer.addSelectionChangedListener(evt -> {
            managedForm.fireSelectionChanged(sectionPart, evt.getSelection());
        });

        Composite commandTableControls = tk.createComposite(commandTableWithControls, SWT.NONE);
        gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        commandTableControls.setLayoutData(gd);
        commandTableControls.setLayout(new GridLayout());

        Button btn = tk.createButton(commandTableControls, "Add...", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btn.addListener(SWT.Selection, evt -> {
            AddToStackWizard wizard = new AddToStackWizard();
            WizardDialog dialog = new WizardDialog(part.getViewSite().getShell(), wizard);
            if (dialog.open() == Window.OK) {
                commandStackTableViewer.addTelecommand(wizard.getTelecommand());
            }
        });

        btn = tk.createButton(commandTableControls, "Up", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        btn = tk.createButton(commandTableControls, "Down", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {
        detailsPart.setPageProvider(this);
    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }

    @Override
    public Object getPageKey(Object object) {
        return object;
    }

    @Override
    public IDetailsPage getPage(Object key) {
        // TODO probably dispose of previous page
        return new CommandDetailsPage((Telecommand) key);
    }
}
