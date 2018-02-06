package org.yamcs.studio.ui.alphanum.actions;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class RemoveAction extends AlphaNumericAction {

    private ISelectionChangedListener listener;
    
    public RemoveAction(final ParameterTableViewer viewer) {
        super("icons/elcl16/remove.png", viewer);
        setToolTipText("Remove");
        setEnabled(false);
        listener = new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(event.getSelection().isEmpty())
                    setEnabled(false);
                else
                    setEnabled(true);
                
            }
        };
        

    }

    @Override
    public void run() {
        List<ParameterInfo> selected = (List<ParameterInfo>)viewer.getStructuredSelection().toList();
        for(ParameterInfo info : selected) {
            viewer.removeParameter(info);
        }
    }

    public void setViewer(ParameterTableViewer viewer) {
        if( getViewer() != null)
            getViewer().removeSelectionChangedListener(listener);
        super.setViewer(viewer);
        if( getViewer() != null)
            viewer.addSelectionChangedListener(listener);
        
    }




}
