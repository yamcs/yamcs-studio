package org.yamcs.studio.ui.alphanum.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;
import org.yamcs.studio.ui.alphanum.ShowColumnsWizard;

public class ShowColumnsAction extends AlphaNumericAction {

    private List<String> columns;
    
    public ShowColumnsAction(final ParameterTableViewer viewer) {
        super("icons/elcl16/config.png", viewer);
        setToolTipText("Choose Columns");

        columns = new ArrayList<>();
        columns.add(ParameterTableViewer.COL_ENG);
        columns.add(ParameterTableViewer.COL_RAW);
        columns.add(ParameterTableViewer.COL_TIME);
        columns.add(ParameterTableViewer.COL_AQU_TIME);
    }

    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
        ShowColumnsWizard wizard = new ShowColumnsWizard(viewer.getColumns(), columns);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK)
            viewer.setColumns(wizard.getColumns());

    }


}
