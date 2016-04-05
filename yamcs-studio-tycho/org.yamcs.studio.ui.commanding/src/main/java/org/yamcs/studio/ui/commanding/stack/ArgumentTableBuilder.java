package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Mdb.ArgumentInfo;

/*
 * Build an editable table to enter command's argument
 * TODO: provide support to specific type editor (e.g. combobox for enums)
 */
public class ArgumentTableBuilder {

    StackedCommand command;

    public ArgumentTableBuilder(StackedCommand command) {
        this.command = command;
    }

    public TableViewer createArgumentTable(Composite shell) {

        TableColumnLayout tcl = new TableColumnLayout();
        Composite argumentsComposite = new Composite(shell, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        argumentsComposite.setLayout(tcl);

        TableViewer argumentTable = new TableViewer(argumentsComposite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        Table table = argumentTable.getTable();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        argumentTable.setContentProvider(new ArrayContentProvider());

        // create columns
        String[] titles = { "Argument", "Value" };
        TableViewerColumn column = createTableViewerColumn(argumentTable, titles[0], 0);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ArgumentAssignement)
                    return ((ArgumentAssignement) element).arg.getName();
                return super.getText(element);
            }
        });
        tcl.setColumnData(column.getColumn(), new ColumnPixelData(10));

        column = createTableViewerColumn(argumentTable, titles[1], 1);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ArgumentAssignement)
                    return ((ArgumentAssignement) element).value;
                return super.getText(element);
            }
        });
        column.setEditingSupport(new ParameterEditingSupport(argumentTable));
        tcl.setColumnData(column.getColumn(), new ColumnWeightData(100));

        return argumentTable;
    }

    private TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String header, int idx) {
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT, idx);
        column.getColumn().setText(header);
        column.getColumn().setResizable(true);
        return column;
    }

    static class ArgumentAssignement {
        public ArgumentAssignement(ArgumentInfo arg, String value) {
            this.arg = arg;
            this.value = value;
        }

        ArgumentInfo arg;
        String value;
    }

    class ParameterEditingSupport extends EditingSupport {
        private final TableViewer viewer;
        private final CellEditor editor;

        public ParameterEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            this.editor = new TextCellEditor(viewer.getTable());

        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return ((ArgumentAssignement) element).value;
        }

        @Override
        protected void setValue(Object element, Object userInputValue) {

            ArgumentAssignement aa = (ArgumentAssignement) element;
            String value = String.valueOf(userInputValue);

            // update command
            if (value.trim().isEmpty()) {
                command.addAssignment(aa.arg, null);
            } else {
                command.addAssignment(aa.arg, value);
            }

            // update table
            aa.value = String.valueOf(value);
            viewer.update(element, null);
        }

    }

    public class ArrayContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ArrayList<?>)
                return ((ArrayList<?>) inputElement).toArray();
            return null;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

}
