package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ChooseScrollColumnsPage extends WizardPage {

    private List<String> columns;
    private List<String> selected;

    protected ChooseScrollColumnsPage(List<String> columns) {
        super("Choose columns");
        setTitle("Choose the colums to be removed");
        selected = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.columns.addAll(columns);
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = false;
        composite.setLayout(gl);

        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        List<Button> checkboxes = new ArrayList<>();

        Button checkBox;
        
        for(String column: columns) {
            checkBox = new Button(composite,SWT.CHECK);
            checkBox.setText(column);
            checkboxes.add(checkBox);
        }
        
        
        checkBox = new Button(composite,SWT.CHECK);
        checkBox.setText(ParameterTableViewer.COL_RAW);
        checkboxes.add(checkBox);

        checkBox = new Button(composite,SWT.CHECK);
        checkBox.setText(ParameterTableViewer.COL_TIME);
        checkboxes.add(checkBox);

        checkBox = new Button(composite,SWT.CHECK);
        checkBox.setText(ParameterTableViewer.COL_AQU_TIME);
        checkboxes.add(checkBox);

        for(Button check : checkboxes) {
            check.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    Button btn = (Button) event.getSource();
                    if(btn.getSelection())
                        selected.add(btn.getText());
                    else
                        columns.remove(btn.getText());

                }
            });
        }


    }

}
