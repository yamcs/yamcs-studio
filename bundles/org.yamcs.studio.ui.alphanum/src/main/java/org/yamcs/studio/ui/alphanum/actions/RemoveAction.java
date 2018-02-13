package org.yamcs.studio.ui.alphanum.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.yamcs.studio.ui.alphanum.AlphaNumericEditor;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public class RemoveAction extends Action implements IEditorActionDelegate {

    private ParameterTableViewer table;
    private ISelectionChangedListener listener;
    
    public RemoveAction() {
        setEnabled(false);
        
        listener = new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(event.getSelection().isEmpty())
                    setEnabled(false);
                else
                    setEnabled(true);
                
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        List<String> selected = (List<String>)table.getStructuredSelection().toList();
        for(String info : selected) {
            table.removeParameter(info);
        }
    }
    @Override
    public void run(IAction action) {
        run();
        
    }
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if(selection == null || selection.isEmpty())
            setEnabled(false);
        else 
            setEnabled(true);
    }
    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if(table != null)
            table.removeSelectionChangedListener(listener);
        
        if(targetEditor == null)
            table = null;
        else {
            table = ((AlphaNumericEditor)targetEditor).getParameterTable();
            table.addSelectionChangedListener(listener);
        }
    }





}
