package org.yamcs.studio.commanding.cmdhist;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Lists acknowledgments
 */
public class AckTableViewer extends TableViewer {

    public static final String COL_NAME = "Name";
    public static final String COL_STATUS = "Status";
    public static final String COL_DELTA = "Delta";
    public static final String COL_DATE = "Date";

    private CommandHistoryView commandHistoryView;

    public AckTableViewer(Composite parent, CommandHistoryView commandHistoryView) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.commandHistoryView = commandHistoryView;

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        TableLayout tl = new TableLayout();
        getTable().setLayout(tl);

        addFixedColumns(tl);

        setContentProvider(new AckTableContentProvider());
    }

    private void addFixedColumns(TableLayout tl) {
        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_NAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                Acknowledgment ack = (Acknowledgment) element;
                return ack.getName();
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        TableViewerColumn statusColumn = new TableViewerColumn(this, SWT.NONE);
        statusColumn.getColumn().setText(COL_STATUS);
        statusColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public Image getImage(Object element) {
                Acknowledgment ack = (Acknowledgment) element;
                if (ack.getStatus().equals("OK")) {
                    return commandHistoryView.greenBubble;
                } else if (!ack.getStatus().equals("PENDING")) {
                    return commandHistoryView.redBubble;
                } else {
                    return null;
                }
            }

            @Override
            public String getText(Object element) {
                Acknowledgment ack = (Acknowledgment) element;
                return ack.getStatus();
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        TableViewerColumn deltaColumn = new TableViewerColumn(this, SWT.NONE);
        deltaColumn.getColumn().setText(COL_DELTA);
        deltaColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Acknowledgment ack = (Acknowledgment) element;
                if (!ack.getStatus().equals("PENDING")) {
                    return ack.getDelta();
                } else {
                    return null;
                }
            }
        });
        tl.addColumnData(new ColumnWeightData(200));

        TableViewerColumn dateColumn = new TableViewerColumn(this, SWT.NONE);
        dateColumn.getColumn().setText(COL_DATE);
        dateColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Acknowledgment ack = (Acknowledgment) element;
                return ack.getTime();
            }
        });
        tl.addColumnData(new ColumnWeightData(200));
    }
}
