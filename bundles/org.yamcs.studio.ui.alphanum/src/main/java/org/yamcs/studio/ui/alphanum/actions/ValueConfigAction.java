package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.ui.alphanum.ScrollAlphaNumericEditor;
import org.yamcs.studio.ui.alphanum.ScrollParameterTableViewer;

//TODO
public class ValueConfigAction extends Action implements IEditorActionDelegate {

    private ScrollParameterTableViewer table;

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
                    table.setValue("ENG");
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
                    table.setValue("RAW");
                }
            }
        });

        Point loc = shell.getDisplay().getActiveShell().getLocation();
        Rectangle rect = shell.getDisplay().getActiveShell().getBounds();

        Point mLoc = new Point(loc.x-1, loc.y+rect.height);

        menu.setVisible(true);

    }

    @Override
    public void run(IAction action) {
        run();

    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if(targetEditor == null)
            table = null;
        else
            table = ((ScrollAlphaNumericEditor)targetEditor).getParameterTable();

    }


}
