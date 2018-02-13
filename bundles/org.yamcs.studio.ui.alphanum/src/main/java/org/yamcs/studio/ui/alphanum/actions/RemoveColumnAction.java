package org.yamcs.studio.ui.alphanum.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.ScrollAlphaNumericEditor;
import org.yamcs.studio.ui.alphanum.ScrollParameterTableViewer;

public class RemoveColumnAction extends Action {


    
    public RemoveColumnAction() {
        super("icons/elcl16/remove.png");
        setToolTipText("Remove");
        setEnabled(false);

        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        ScrollParameterTableViewer table = ((ScrollAlphaNumericEditor)editor).getParameterTable();
        
        List<ParameterInfo> selected = (List<ParameterInfo>)table.getStructuredSelection().toList();
        for(ParameterInfo info : selected) {
            table.removeParameter(info);
        }
    }


}
