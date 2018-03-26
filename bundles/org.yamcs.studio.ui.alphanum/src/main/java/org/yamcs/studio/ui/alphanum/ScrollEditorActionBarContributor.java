package org.yamcs.studio.ui.alphanum;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.yamcs.studio.ui.alphanum.actions.AddNewColumnAction;
import org.yamcs.studio.ui.alphanum.actions.RemoveColumnAction;
import org.yamcs.studio.ui.alphanum.actions.ValueConfigAction;


public class ScrollEditorActionBarContributor extends EditorActionBarContributor {
    private IToolBarManager manager;
    final private AddNewColumnAction add = new AddNewColumnAction(null);
    final private RemoveColumnAction remove = new RemoveColumnAction(null);
    final private ValueConfigAction config = new ValueConfigAction(null);



    @Override
    public void contributeToToolBar(final IToolBarManager mgr) {
    	mgr.add(add);
        mgr.add(remove);
        mgr.add(config);
        mgr.add(new Separator());
        this.manager = mgr;
    }

    @Override
    public void setActiveEditor(final IEditorPart target) {
        final ScrollAlphaNumericEditor editor = (ScrollAlphaNumericEditor) target;
        add.setScrollViewer(editor.getParameterTable());
        remove.setScrollViewer(editor.getParameterTable());
        config.setScrollViewer(editor.getParameterTable());
        manager.update(true);

    }


    @Override
    public void dispose() {
        add.setViewer(null);
        remove.setViewer(null);
        config.setViewer(null);
        super.dispose();
    }
}