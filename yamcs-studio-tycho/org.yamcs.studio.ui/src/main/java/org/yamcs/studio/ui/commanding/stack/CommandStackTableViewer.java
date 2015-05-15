package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CommandStackTableViewer extends TableViewer {

    public static final String COL_ROW_ID = "#";
    public static final String COL_COMMAND = "Command";

    private CommandStackTableContentProvider contentProvider;

    public CommandStackTableViewer(Composite parent) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        contentProvider = new CommandStackTableContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider); // ! otherwise refresh() deletes everything...
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn rowIdColumn = new TableViewerColumn(this, SWT.CENTER);
        rowIdColumn.getColumn().setText(COL_ROW_ID);
        rowIdColumn.getColumn().setToolTipText("Sequence Number within Stack");
        rowIdColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(contentProvider.indexOf(element) + 1);
            }
        });
        tcl.setColumnData(rowIdColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Telecommand) element).getMetaCommand().getName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        //nameColumn.setEditingSupport(new TextEditingSupport(this));
    }

    public void addTelecommand(Telecommand command) {
        contentProvider.addTelecommand(command);
    }

    private static final class TextEditingSupport extends EditingSupport {

        private TableViewer viewer;
        private CellEditor editor;

        public TextEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            editor = new TextCellEditor(viewer.getTable());
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
            return "123";
        }

        @Override
        protected void setValue(Object element, Object value) {
            String stringValue = String.valueOf(value);

            viewer.update(element, null);
        }
    }
}
