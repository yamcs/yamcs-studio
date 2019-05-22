package org.yamcs.studio.commanding.stack;

import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class ExportCommandStackSpellDialog extends TitleAreaDialog {

    // input data
    Collection<StackedCommand> scs;
    String filename;
    
    // controls
    Button delaysButton;
    Text spacecraftNameText;
    Text procedureNameText;
    
    // dialog results:
    public boolean exportDelays;
    public String procedureName;
    public String spaceCraftName;
    

    public ExportCommandStackSpellDialog(Shell parent,  Collection<StackedCommand> scs, String filename) {
        super (parent);
        this.scs = scs;
        this.filename = filename;        
    }

    @Override
    public void create() {
        super.create();
        setTitle("SPELL Procedure Export Options");    
        getButton(IDialogConstants.OK_ID).setFocus();    
    }


    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.OK_ID == buttonId) {
            this.exportDelays = delaysButton.getSelection();
            this.procedureName = procedureNameText.getText();
            this.spaceCraftName = spacecraftNameText.getText();
            okPressed();
        } else {
            cancelPressed();
        } 
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        createDetailsSection(container);

        Dialog.applyDialogFont(container);
        return container;
    }

    private void createDetailsSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(data);

        createTextSection(container);
    }

    private void createTextSection(Composite parent) {
        Composite textContainer = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = layout.marginWidth = 0;
        textContainer.setLayout(layout);
        textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(textContainer, SWT.NONE);
        label.setText("Export Stack Delays:");
        delaysButton = new Button(textContainer,SWT.CHECK);       
        
        label = new Label(textContainer, SWT.NONE);
        label.setText("Spacecraft Name:");
        spacecraftNameText = new Text(textContainer, SWT.NONE);
        spacecraftNameText.setText("spacecraft");
                
        label = new Label(textContainer, SWT.NONE);
        label.setText("Procedure Name:");
        procedureNameText = new Text(textContainer, SWT.NONE);
        String defaultName =  this.filename;
        procedureNameText.setText(defaultName);
        
    }
    
}
