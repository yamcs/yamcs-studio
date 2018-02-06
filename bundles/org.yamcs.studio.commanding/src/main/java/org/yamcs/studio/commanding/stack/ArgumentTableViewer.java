package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Lists the arguments for the selected Telecommand
 */
public class ArgumentTableViewer extends TableViewer {

    public static final String COL_NAME = "Argument";
    public static final String COL_VALUE = "Value";

    public ArgumentTableViewer(Composite parent) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        getTable().setHeaderVisible(false);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        setContentProvider(new ArgumentTableContentProvider());
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_NAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                TelecommandArgument argument = (TelecommandArgument) element;
                return argument.getName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn valueColumn = new TableViewerColumn(this, SWT.NONE);
        valueColumn.getColumn().setText(COL_VALUE);
        valueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                TelecommandArgument argument = (TelecommandArgument) element;
                return argument.getValue();
            }
        });
        tcl.setColumnData(valueColumn.getColumn(), new ColumnWeightData(200));
    }
}
