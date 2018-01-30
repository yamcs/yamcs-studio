package org.yamcs.studio.ui.alphanum.actions;

import java.util.List;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class RemoveAction extends AlphaNumericAction {
	

    public RemoveAction(final ParameterTableViewer viewer) {
    	super("icons/elcl16/remove.png", viewer);
        setToolTipText("Remove");

    }

	@Override
    public void run() {
    	List<ParameterInfo> selected = (List<ParameterInfo>)viewer.getStructuredSelection().toList();
        for(ParameterInfo info : selected) {
        	viewer.removeParameter(info);
        }
    }
	

}
