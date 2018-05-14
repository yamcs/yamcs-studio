package org.yamcs.studio.alphanumeric;

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

public class ChooseColumnsPage extends WizardPage {

    private List<String> choosenColumns;
    private List<String> columns;

    protected ChooseColumnsPage(List<String> choosenColumns, List<String> columns) {
        super("Select columns");
        setTitle("Select columns:");

        this.choosenColumns = new ArrayList<>();
        this.choosenColumns.addAll(choosenColumns);
        this.columns = columns;
    }

    public List<String> getColumns() {
        return choosenColumns;
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
        
        for(String column: columns) {
            Button checkBox = new Button(composite,SWT.CHECK);
            checkBox.setText(column);
            if(choosenColumns.contains(column)){
                checkBox.setSelection(true);
            }

            checkBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    Button btn = (Button) event.getSource();
                    if(btn.getSelection())
                        choosenColumns.add(btn.getText());
                    else
                        choosenColumns.remove(btn.getText());

                }
            });
        }

    }

}
