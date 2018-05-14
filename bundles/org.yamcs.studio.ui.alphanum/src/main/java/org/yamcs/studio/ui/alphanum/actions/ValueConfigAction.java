package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.ui.alphanum.ScrollParameterTableViewer;

public class ValueConfigAction extends AlphaNumericAction {
    
   
    public ValueConfigAction(ScrollParameterTableViewer viewer) {
        super( "icons/elcl16/config.png", viewer);
        setToolTipText("Choose the value to be displayed in the table");
        
    }
    

    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        Menu menu = new Menu(shell, SWT.POP_UP);


        MenuItem item1 = new MenuItem(menu, SWT.RADIO);
        item1.setText("Eng Value");
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSelected = ((MenuItem)e.widget).getSelection();
                if(isSelected){ 
                    getScrollViewer().setValue(ScrollParameterTableViewer.ENG);
                }

            }
        });

        MenuItem item2 = new MenuItem(menu, SWT.RADIO);
        item2.setText("Raw Value");

        item2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSelected = ((MenuItem)e.widget).getSelection();
                if(isSelected){ 
                    getScrollViewer().setValue(ScrollParameterTableViewer.RAW);
                }
            }
        });
        
        if(getScrollViewer().getValue().equals(ScrollParameterTableViewer.RAW))
            item2.setSelection(true);
        else
            item1.setSelection(true);


        menu.setVisible(true);

    }


}
