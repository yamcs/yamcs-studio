package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;
import org.yamcs.studio.ui.alphanum.ShowColumnsWizard;

public class ShowColumnsAction extends AlphaNumericAction {

    public ShowColumnsAction(final ParameterTableViewer viewer) {
        super("icons/elcl16/add.png", viewer);
        setToolTipText("Add Parameter");
    }

    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ShowColumnsWizard wizard = new ShowColumnsWizard(getViewer().getColumns());
        WizardDialog dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK)
            viewer.setColumns(wizard.getColumns());

    }


}
