package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.yamcs.studio.ui.alphanum.AlphaNumericEditor;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class ClearAction extends Action implements IEditorActionDelegate{
    ParameterTableViewer table;

    @Override
    public void run() {
    	table.clear();
    }

    @Override
    public void run(IAction action) {
        run();
        
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ;
        
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if(targetEditor == null)
            table = null;
        else
            table = ((AlphaNumericEditor)targetEditor).getParameterTable();

        
    }
    

	

}
