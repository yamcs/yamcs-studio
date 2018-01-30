package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.AddParameterWizard;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class AddNewParameterAction extends AlphaNumericAction {

	//TODO come with icons
    public AddNewParameterAction(final ParameterTableViewer viewer) {
    	super("icons/elcl16/add.png", viewer);
        setToolTipText("Add Parameter");
    }

	@Override
    public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        AddParameterWizard wizard = new AddParameterWizard();
        WizardDialog dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK)
        	for(ParameterInfo info : wizard.getParameter())
        	    viewer.addParameter(info);

    }
	

}
