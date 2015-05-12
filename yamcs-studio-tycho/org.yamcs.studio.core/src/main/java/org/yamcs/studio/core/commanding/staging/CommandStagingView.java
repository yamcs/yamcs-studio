package org.yamcs.studio.core.commanding.staging;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class CommandStagingView extends ViewPart {

    public static final String COL_ROW_ID = "#";
    public static final String COL_COMMAND = "Command";

    private LocalResourceManager resourceManager;

    private TableViewer tableViewer;

    private CommandStagingViewerContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        tableContentProvider = new CommandStagingViewerContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...
        tableViewer.setComparator(new CommandStagingViewerComparator());
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn rowIdColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        rowIdColumn.getColumn().setText(COL_ROW_ID);
        rowIdColumn.getColumn().setToolTipText("Sequence Number within Stack");
        rowIdColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(((Telecommand) element).getRowId());
            }
        });
        tcl.setColumnData(rowIdColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Telecommand) element).getCommandText();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        tableViewer.getTable().setSortColumn(rowIdColumn.getColumn());
        tableViewer.getTable().setSortDirection(SWT.DOWN);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }
}
