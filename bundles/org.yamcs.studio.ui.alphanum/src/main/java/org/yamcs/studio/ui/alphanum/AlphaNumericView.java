package org.yamcs.studio.ui.alphanum;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.InstanceListener;

public class AlphaNumericView extends ViewPart implements StudioConnectionListener, InstanceListener{

	
	public static final String ID = "org.yamcs.studio.ui.alphanum.AlphaNumericView";
	
	ParameterTableViewer parameterTable;
	
	@Override
	public void instanceChanged(String oldInstance, String newInstance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStudioConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStudioDisconnect() {
		// TODO Auto-generated method stub
		
	}

     public AlphaNumericView() {
             super();
     }
     
     @Override
     public void setFocus() {
    	 if(parameterTable != null)
    		 parameterTable.getTable().setFocus();
     }
     
     public void createPartControl(Composite parent) {
    	 
         FillLayout fl = new FillLayout();
         fl.marginHeight = 0;
         fl.marginWidth = 0;
         parent.setLayout(fl);

         SashForm sash = new SashForm(parent, SWT.VERTICAL);

         Composite tableWrapper = new Composite(sash, SWT.NONE);
         tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
         TableColumnLayout tcl = new TableColumnLayout();

         tableWrapper.setLayout(tcl);
         parameterTable = new ParameterTableViewer(this, tableWrapper, tcl);
         
         parameterTable.refresh();
     }

	public void addParameters(List<ParameterInfo> parameters) {
		for(ParameterInfo info: parameters) {
			parameterTable.addParameter(info);
		}
		parameterTable.refresh();
		
	}


	
}
