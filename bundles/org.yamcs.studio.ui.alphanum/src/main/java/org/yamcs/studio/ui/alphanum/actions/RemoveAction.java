package org.yamcs.studio.ui.alphanum.actions;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class RemoveAction extends AlphaNumericAction {
        
    private ISelectionChangedListener listener;

    
    public RemoveAction(final ParameterTableViewer viewer) {
        super("icons/elcl16/remove.png", viewer);
        setToolTipText("Remove");
        listener = new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(event.getSelection().isEmpty())
                    setEnabled(false);
                else
                    setEnabled(true);
            }
        };
        setEnabled(false);

    }
    
    @Override
    public void setViewer(ParameterTableViewer viewer) {
        if(viewer != null) {
            viewer.addSelectionChangedListener(listener);
        }
        super.setViewer(viewer);
    }

    
        @Override
    public void run() {
        List<String> selected = (List<String>)viewer.getStructuredSelection().toList();
        for(String info : selected) {
                viewer.removeParameter(info);
        }
    }
        

}

