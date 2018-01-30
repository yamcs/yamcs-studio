package org.yamcs.studio.ui.alphanum.actions;

import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class ClearAction extends AlphaNumericAction {
	
    public ClearAction(final ParameterTableViewer viewer) {
    	super("icons/elcl16/removeall.png", viewer);
        setToolTipText("Clear");
        
    }

    @Override
    public void run() {
    	viewer.clear();
    }
    

	

}
