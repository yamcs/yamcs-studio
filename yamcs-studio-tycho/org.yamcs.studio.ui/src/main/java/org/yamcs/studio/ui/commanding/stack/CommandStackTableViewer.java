package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.yamcs.studio.ui.CenteredImageLabelProvider;
import org.yamcs.studio.ui.YamcsUIPlugin;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;
import org.yamcs.xtce.Comparison;
import org.yamcs.xtce.Comparison.OperatorType;
import org.yamcs.xtce.ComparisonList;
import org.yamcs.xtce.MatchCriteria;
import org.yamcs.xtce.TransmissionConstraint;

public class CommandStackTableViewer extends TableViewer {

    public static final String COL_ROW_ID = "#";
    public static final String COL_COMMAND = "Command";
    public static final String COL_SIGNIFICANCE = "Lvl";
    public static final String COL_CONSTRAINTS = "Constraints";
    public static final String COL_CONSTRAINTS_TIMEOUT = "T/O";
    public static final String COL_RELEASE = "Release";
    public static final String COL_STATE = "State";

    private CommandStackView styleProvider;
    private CommandStackTableContentProvider contentProvider;
    private ResourceManager resourceManager;

    public CommandStackTableViewer(Composite parent, TableColumnLayout tcl, CommandStackView styleProvider) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        this.styleProvider = styleProvider;
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        contentProvider = new CommandStackTableContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider); // ! otherwise refresh() deletes everything...
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        Image level1Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level1s.png"));
        Image level2Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level2s.png"));
        Image level3Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level3s.png"));
        Image level4Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level4s.png"));
        Image level5Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level5s.png"));

        TableViewerColumn rowIdColumn = new TableViewerColumn(this, SWT.CENTER);
        rowIdColumn.getColumn().setText(COL_ROW_ID);
        rowIdColumn.getColumn().setToolTipText("Sequence Number within Stack");
        rowIdColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(contentProvider.indexOf(element) + 1);
            }
        });
        rowIdColumn.getColumn().setWidth(50);
        tcl.setColumnData(rowIdColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.setLabelProvider(new CommandSourceColumnLabelProvider(styleProvider));
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn significanceColumn = new TableViewerColumn(this, SWT.CENTER);
        significanceColumn.getColumn().setText(COL_SIGNIFICANCE);
        significanceColumn.getColumn().setToolTipText("Significance Level");
        significanceColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                if (cmd.getMetaCommand().getDefaultSignificance() == null)
                    return null;
                switch (cmd.getMetaCommand().getDefaultSignificance().getConsequenceLevel()) {
                case watch:
                    return level1Image;
                case warning:
                    return level2Image;
                case distress:
                    return level3Image;
                case critical:
                    return level4Image;
                case severe:
                    return level5Image;
                default:
                    return null;
                }
            }

            @Override
            public String getToolTipText(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                if (cmd.getMetaCommand().getDefaultSignificance() == null)
                    return null;
                return cmd.getMetaCommand().getDefaultSignificance().getReasonForWarning();
            }
        });
        significanceColumn.getColumn().setWidth(50);
        tcl.setColumnData(significanceColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn constraintsColumn = new TableViewerColumn(this, SWT.LEFT);
        constraintsColumn.getColumn().setText(COL_CONSTRAINTS);
        constraintsColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                StringBuilder buf = new StringBuilder();
                for (TransmissionConstraint constraint : cmd.getMetaCommand().getTransmissionConstraintList())
                    appendConstraint(constraint.getMatchCriteria(), buf);
                return buf.toString();
            }
        });
        tcl.setColumnData(constraintsColumn.getColumn(), new ColumnPixelData(250));

        TableViewerColumn constraintsTimeOutColumn = new TableViewerColumn(this, SWT.CENTER);
        constraintsTimeOutColumn.getColumn().setText(COL_CONSTRAINTS_TIMEOUT);
        constraintsTimeOutColumn.getColumn().setToolTipText("Constraints Timeout");
        constraintsTimeOutColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                long timeout = 0;
                for (TransmissionConstraint constraint : cmd.getMetaCommand().getTransmissionConstraintList())
                    timeout = Math.max(timeout, constraint.getTimeout());

                return (timeout > 0) ? Long.toString(timeout) + " ms" : null;
            }
        });
        tcl.setColumnData(constraintsTimeOutColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn releaseColumn = new TableViewerColumn(this, SWT.CENTER);
        releaseColumn.getColumn().setText(COL_RELEASE);
        releaseColumn.getColumn().setToolTipText("Release Time");
        releaseColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "ASAP";
            }
        });
        tcl.setColumnData(releaseColumn.getColumn(), new ColumnPixelData(80));

        TableViewerColumn stateColumn = new TableViewerColumn(this, SWT.CENTER);
        stateColumn.getColumn().setText(COL_STATE);
        stateColumn.getColumn().setToolTipText("Stack State");
        stateColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                return cmd.getState().getText();
            }

            @Override
            public Color getBackground(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                if (cmd.isArmed())
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
                else if (cmd.getState() == State.ISSUED)
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_GREEN);
                else if (cmd.getState() == State.REJECTED)
                    return styleProvider.getErrorBackgroundColor();

                return super.getBackground(element);
            }

            @Override
            public Color getForeground(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                if (cmd.getState() == State.REJECTED)
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_RED);

                return super.getForeground(element);
            }
        });
        tcl.setColumnData(stateColumn.getColumn(), new ColumnPixelData(80));
    }

    public void appendConstraint(MatchCriteria criteria, StringBuilder buf) {
        if (criteria instanceof ComparisonList) {
            ComparisonList list = (ComparisonList) criteria;
            for (Comparison comparison : list.getComparisonList()) {
                appendConstraint(comparison, buf);
                buf.append(", ");
            }
        } else {
            Comparison comparison = (Comparison) criteria;
            buf.append(comparison.getParameter().getOpsName());
            if (comparison.getComparisonOperator() == OperatorType.EQUALITY)
                buf.append("="); // I don't like the ==. should be same as in spreadsheet
            else
                buf.append(Comparison.operatorToString(comparison.getComparisonOperator()));
            buf.append(comparison.getStringValue());
        }
    }

    public void addTelecommand(StackedCommand command) {
        contentProvider.addTelecommand(command);
    }
}
