package org.yamcs.studio.ui.alphanum.actions;

import org.yamcs.studio.ui.alphanum.ParameterTableViewer;


public class RestoreAction extends AlphaNumericAction {
	

    public RestoreAction(final ParameterTableViewer viewer) {
    	super("/elcl16/export_log", viewer);
        setToolTipText("Restore");

    }

    @Override
    public void run() {
    	viewer.restoreParameters();

    }
    

}
