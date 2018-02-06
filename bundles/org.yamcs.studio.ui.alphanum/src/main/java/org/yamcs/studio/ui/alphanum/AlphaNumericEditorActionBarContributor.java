package org.yamcs.studio.ui.alphanum;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.yamcs.studio.ui.alphanum.actions.AddNewParameterAction;
import org.yamcs.studio.ui.alphanum.actions.ClearAction;
import org.yamcs.studio.ui.alphanum.actions.RemoveAction;
import org.yamcs.studio.ui.alphanum.actions.ShowColumnsAction;


public class AlphaNumericEditorActionBarContributor extends EditorActionBarContributor {
    private IToolBarManager manager;
    private final AddNewParameterAction add = new AddNewParameterAction(null);
    private final RemoveAction remove = new RemoveAction(null);
   //private final RestoreAction restore = new RestoreAction(null);
    private final ClearAction clearAll = new ClearAction(null);
    private final ShowColumnsAction showColumns = new ShowColumnsAction(null);



    @Override
    public void contributeToToolBar(final IToolBarManager mgr) {
        mgr.add(new Separator());
        mgr.add(add);
        mgr.add(remove);
       // mgr.add(restore);
        mgr.add(clearAll);
        mgr.add(showColumns);
        mgr.add(new Separator());
        this.manager = mgr;
    }

    @Override
    public void setActiveEditor(final IEditorPart target) {
        final AlphaNumericEditor editor = (AlphaNumericEditor) target;
        add.setViewer(editor.getParameterTable());
        remove.setViewer(editor.getParameterTable());
      //  restore.setViewer(editor.getParameterTable());
        clearAll.setViewer(editor.getParameterTable());
        showColumns.setViewer(editor.getParameterTable());
        manager.update(true);

    }


    @Override
    public void dispose() {
        add.setViewer(null);
        remove.setViewer(null);
     //   restore.setViewer(null);
        clearAll.setViewer(null);
        showColumns.setViewer(null);
        super.dispose();
    }
}
