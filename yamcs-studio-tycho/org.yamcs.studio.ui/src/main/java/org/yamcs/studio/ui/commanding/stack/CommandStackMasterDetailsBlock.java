package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class CommandStackMasterDetailsBlock extends MasterDetailsBlock {

    private CommandStackView part;

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

        Composite commandTableWithControls = tk.createComposite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        commandTableWithControls.setLayout(gl);

        /*
         * Composite commandTableWrapper = tk.createComposite(commandTableWithControls, SWT.NONE);
         * commandTableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
         * CommandStackTableViewer commandTableViewer = new
         * CommandStackTableViewer(commandTableWrapper); tk.paintBordersFor(commandTableWrapper);
         */

        Composite tableWrapper = new Composite(commandTableWithControls, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        // Workaround unwanted h_scroll (https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997#c4)
        gd.widthHint = 1;
        tableWrapper.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        Table t = tk.createTable(tableWrapper, SWT.NONE);
        new CommandStackTableViewer(tcl, t);

        Composite commandTableControls = tk.createComposite(commandTableWithControls, SWT.NONE);
        gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        commandTableControls.setLayoutData(gd);
        commandTableControls.setLayout(new GridLayout());
        Button btn = tk.createButton(commandTableControls, "Add...", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btn = tk.createButton(commandTableControls, "Up", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btn = tk.createButton(commandTableControls, "Down", SWT.NONE);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {

    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }
}
