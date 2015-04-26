package org.yamcs.studio.core.eventlog;

import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.utils.TimeEncoding;

public class EventLogView extends ViewPart {

    private static final Logger log = Logger.getLogger(EventLogView.class.getName());

    public static final String COL_SOURCE = "Source";
    public static final String COL_RECEIVED = "Received";
    public static final String COL_TYPE = "Type";
    public static final String COL_DESCRIPTION = "Description";
    public static final String COL_GENERATED = "Generated";

    private Composite parent;
    private TableViewer tableViewer;
    private TableColumnLayout tcl;

    private EventLogContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        addFixedColumns();

        tableContentProvider = new EventLogContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...
    }

    private void addFixedColumns() {
        TableViewerColumn sourceColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Event) element).getSource();
            }
        });
        tcl.setColumnData(sourceColumn.getColumn(), new ColumnPixelData(100));

        TableViewerColumn receivedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        receivedColumn.getColumn().setText(COL_RECEIVED);
        receivedColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return TimeEncoding.toString(((Event) element).getReceptionTime());
            }
        });
        tcl.setColumnData(receivedColumn.getColumn(), new ColumnPixelData(150));

        TableViewerColumn typeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        typeColumn.getColumn().setText(COL_TYPE);
        typeColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Event) element).getType();
            }
        });
        tcl.setColumnData(typeColumn.getColumn(), new ColumnPixelData(100));

        TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        descriptionColumn.getColumn().setText(COL_DESCRIPTION);
        descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Event) element).getMessage();
            }
        });
        tcl.setColumnData(descriptionColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn generatedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        generatedColumn.getColumn().setText(COL_GENERATED);
        generatedColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return TimeEncoding.toString(((Event) element).getGenerationTime());
            }
        });
        tcl.setColumnData(generatedColumn.getColumn(), new ColumnPixelData(150));
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
