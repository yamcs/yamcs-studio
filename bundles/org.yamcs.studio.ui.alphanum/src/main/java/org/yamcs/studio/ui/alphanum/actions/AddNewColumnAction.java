package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.AddParameterWizard;
import org.yamcs.studio.ui.alphanum.ScrollAlphaNumericEditor;
import org.yamcs.studio.ui.alphanum.ScrollParameterTableViewer;

public class AddNewColumnAction extends Action implements IEditorActionDelegate {

    private ScrollParameterTableViewer table;


    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
        AddParameterWizard wizard = new AddParameterWizard();
        WizardDialog dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK)
            for(ParameterInfo info : wizard.getParameter())
                table.addParameter(info);

    }

    @Override
    public void run(IAction action) {
        run();
        
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO
        
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if(targetEditor == null)
            table = null;
        else
            table = ((ScrollAlphaNumericEditor)targetEditor).getParameterTable();
        
    }

}
