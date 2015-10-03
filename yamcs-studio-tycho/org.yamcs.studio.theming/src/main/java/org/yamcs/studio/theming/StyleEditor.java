package org.yamcs.studio.theming;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class StyleEditor extends EditorPart {
    
    public static final String ID = "org.yamcs.studio.theming.styleeditor";
    
    private FileEditorInput input;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        this.input = (FileEditorInput) input;
        setSite(site);
        setInput(input);
        setPartName(input.getName());
    }
    
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout());
        Label lbl = new Label(parent, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL_BOTH));
        lbl.setText("abc" + input.getName());
    }
    
    @Override
    public void doSave(IProgressMonitor monitor) {
    }
    
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void setFocus() {
    }
}
