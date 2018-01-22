package org.yamcs.studio.ui.alphanum;

import org.eclipse.jface.wizard.Wizard;
import org.yamcs.protobuf.Mdb.ParameterInfo;

public class AddParameterWizard extends Wizard {

	AddParameterPage page;
	
	@Override
	public boolean performFinish() {
		return true;
	}
	
	@Override
	public void addPages() {
		page = new AddParameterPage();
		addPage(page);
		
	}

	public ParameterInfo getParameter() {
		return page.getParameter();
	}

}
