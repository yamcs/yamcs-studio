package org.yamcs.studio.ui.alphanum.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.ui.alphanum.ScrollParameterTableViewer;
import org.yamcs.studio.ui.alphanum.ShowColumnsWizard;

public class RemoveColumnAction extends AlphaNumericAction {


    private Listener listener;


    public RemoveColumnAction(ScrollParameterTableViewer viewer) {
        super( "icons/elcl16/remove.png", viewer);
        setToolTipText("Remove");

        listener = new Listener() {

            @Override
            public void handleEvent(Event event) {
                if(scrollViewer.getParameters().isEmpty())
                    setEnabled(false);
                else
                    setEnabled(true);

            }
        };
        
    }


    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ShowColumnsWizard wizard = new ShowColumnsWizard(new ArrayList<>(), getColumnsNames());
        wizard.setWindowTitle("Remove columns");
        WizardDialog dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK)
            for(String column: wizard.getColumns())
                if(!scrollViewer.getParameters().contains(column))
                    scrollViewer.removeParameter(column);

    }


    private List<String> getColumnsNames() {
        List<String> names = new ArrayList<>();
        for( String name :scrollViewer.getParameters()) {
            name = name.substring(name.lastIndexOf("/") +1);
            names.add(name);
        }
        return names;


    }
    
    @Override
    public void setScrollViewer(ScrollParameterTableViewer viewer) {
        if(viewer != null) {
            viewer.addDataChangedListener(listener);
        }
        super.setScrollViewer(viewer);
    }

}
