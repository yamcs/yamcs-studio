package org.yamcs.studio.eventlog;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

public class EventLogTableViewer extends TableViewer {

    public static final String COL_SOURCE = "Source";
    public static final String COL_GENERATION = "Generation";
    public static final String COL_RECEPTION = "Reception";
    public static final String COL_MESSAGE = "Message";
    public static final String COL_SEQNUM = "Seq.Nr.";

    private TableLayout tableLayout;

    public EventLogTableViewer(Composite parent) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        tableLayout = new TableLayout();
        getTable().setLayout(tableLayout);

        // sort listener common for all columns
        Listener sortListener = event -> {
            TableColumn column = (TableColumn) event.widget;

            // TODO should this sort logic not move up into this class? Why are columns passed?
            EventLogContentProvider contentProvider = (EventLogContentProvider) getContentProvider();
            contentProvider.sort(column);
        };

        TableViewerColumn messageColumn = new TableViewerColumn(this, SWT.NONE);
        messageColumn.getColumn().setText(COL_MESSAGE);
        messageColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(300));

        TableViewerColumn sourceColumn = new TableViewerColumn(this, SWT.NONE);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn generationColumn = new TableViewerColumn(this, SWT.NONE);
        generationColumn.getColumn().setText(COL_GENERATION);
        generationColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(160));

        TableViewerColumn receptionColumn = new TableViewerColumn(this, SWT.NONE);
        receptionColumn.getColumn().setText(COL_RECEPTION);
        receptionColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(160));

        TableViewerColumn seqNumColum = new TableViewerColumn(this, SWT.RIGHT);
        seqNumColum.getColumn().setText(COL_SEQNUM);
        seqNumColum.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(80));

        if (!EventLogPreferences.isShowSequenceNumberColumn()) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            seqNumColum.getColumn().setResizable(false);
        }
        if (!EventLogPreferences.isShowGenerationColumn()) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            generationColumn.getColumn().setResizable(false);
        }
        if (!EventLogPreferences.isShowReceptionColumn()) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            receptionColumn.getColumn().setResizable(false);
        }

        for (TableColumn tableColumn : getTable().getColumns()) {
            tableColumn.setMoveable(true);
            // prevent resize to 0
            tableColumn.addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (tableColumn.getWidth() < 5) {
                        tableColumn.setWidth(5);
                    }
                }
            });
        }

        // TODO use IMemento or something
        // !! Keep these values in sync with EventLogViewerComparator constructor
        getTable().setSortColumn(receptionColumn.getColumn());
        getTable().setSortDirection(SWT.UP);
    }
}
