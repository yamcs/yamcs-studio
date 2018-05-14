package org.yamcs.studio.alphanumeric.actions;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.yamcs.studio.alphanumeric.ParameterTableViewer;

public class ClearAction extends AlphaNumericAction {
    
    private Listener listener;
    
    public ClearAction(final ParameterTableViewer viewer) {
        super("icons/elcl16/removeall.png", viewer);
        setToolTipText("Clear");
        
        listener = new Listener() {

            @Override
            public void handleEvent(Event event) {
                if(getViewer().getTable().getItemCount() == 0)
                    setEnabled(false);
                else
                    setEnabled(true);
                
            }
        };

    }
    
    @Override
    public void setViewer(ParameterTableViewer viewer) {
        if(viewer != null) {
            viewer.addDataChangedListener(listener);
        }
        super.setViewer(viewer);
    }

    @Override
    public void run() {
        viewer.clear();
    }
    

        

}