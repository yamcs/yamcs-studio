/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.utils.RCPUtils;

public class CommandStackTableViewer extends TableViewer {

    private static final Logger log = Logger.getLogger(CommandStackTableViewer.class.getName());

    public static final String COL_ROW_ID = "#";
    public static final String COL_COMMAND = "Command";
    public static final String COL_SIGNIFICANCE = "Sig.";
    public static final String COL_CONSTRAINTS = "Constraints";
    public static final String COL_CONSTRAINTS_TIMEOUT = "T/O";
    public static final String COL_RELEASE = "Release";
    public static final String COL_STATE = "Stack State";
    public static final String COL_QUEUED = "Q";
    public static final String COL_RELEASED = "R";
    public static final String COL_SENT = "S";
    public static final String COL_COMMENT = "Comment";

    private Image greenBubble;
    private Image redBubble;
    private Image grayBubble;
    private Image waitingImage;

    private CommandStackView styleProvider;
    private CommandStackTableContentProvider contentProvider;
    private ResourceManager resourceManager;

    public CommandStackTableViewer(IViewSite viewSite, Composite parent, TableColumnLayout tcl,
            CommandStackView styleProvider) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.HIDE_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL));
        this.styleProvider = styleProvider;
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/obj16/ok.png"));
        redBubble = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/obj16/nok.png"));
        grayBubble = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/obj16/undef.png"));
        waitingImage = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/obj16/waiting.png"));

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        contentProvider = new CommandStackTableContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider); // ! otherwise refresh() deletes everything...
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        var level0Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level0s.png"));
        var level1Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level1s.png"));
        var level2Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level2s.png"));
        var level3Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level3s.png"));
        var level4Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level4s.png"));
        var level5Image = resourceManager
                .create(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level5s.png"));

        var rowIdColumn = new TableViewerColumn(this, SWT.CENTER);
        rowIdColumn.getColumn().setText(COL_ROW_ID);
        rowIdColumn.getColumn().setToolTipText("Sequence Number within Stack");
        rowIdColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(contentProvider.indexOf(element) + 1);
            }
        });
        tcl.setColumnData(rowIdColumn.getColumn(), new ColumnPixelData(50));

        var nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.setLabelProvider(new CommandSourceColumnLabelProvider(styleProvider));
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(300));

        var significanceColumn = new TableViewerColumn(this, SWT.CENTER);
        significanceColumn.getColumn().setText(COL_SIGNIFICANCE);
        significanceColumn.getColumn().setToolTipText("Significance Level");
        significanceColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getMetaCommand().getSignificance() == null) {
                    return null;
                }
                switch (cmd.getMetaCommand().getSignificance().getConsequenceLevel()) {
                case WATCH:
                    return level1Image;
                case WARNING:
                    return level2Image;
                case DISTRESS:
                    return level3Image;
                case CRITICAL:
                    return level4Image;
                case SEVERE:
                    return level5Image;
                default:
                    return level0Image;
                }
            }

            @Override
            public String getToolTipText(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getMetaCommand().getSignificance() == null) {
                    return super.getToolTipText(element);
                }
                return cmd.getMetaCommand().getSignificance().getReasonForWarning();
            }
        });
        tcl.setColumnData(significanceColumn.getColumn(), new ColumnPixelData(50));

        var constraintsColumn = new TableViewerColumn(this, SWT.LEFT);
        constraintsColumn.getColumn().setText(COL_CONSTRAINTS);
        constraintsColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var cmd = (StackedCommand) element;
                var buf = new StringBuilder();
                for (var i = 0; i < cmd.getMetaCommand().getConstraintCount(); i++) {
                    if (i != 0) {
                        buf.append(" and ");
                    }
                    var constraint = cmd.getMetaCommand().getConstraint(i);
                    buf.append(constraint.getExpression());
                }
                return buf.length() != 0 ? buf.toString() : "-";
            }
        });
        tcl.setColumnData(constraintsColumn.getColumn(), new ColumnPixelData(250));

        var constraintsTimeOutColumn = new TableViewerColumn(this, SWT.CENTER);
        constraintsTimeOutColumn.getColumn().setText(COL_CONSTRAINTS_TIMEOUT);
        constraintsTimeOutColumn.getColumn().setToolTipText("Constraints Timeout");
        constraintsTimeOutColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var cmd = (StackedCommand) element;
                var timeout = -1L;
                for (var constraint : cmd.getMetaCommand().getConstraintList()) {
                    timeout = Math.max(timeout, constraint.getTimeout());
                }

                return (timeout >= 0) ? Long.toString(timeout) + " ms" : "-";
            }
        });
        tcl.setColumnData(constraintsTimeOutColumn.getColumn(), new ColumnPixelData(50));

        var stateColumn = new TableViewerColumn(this, SWT.CENTER);
        stateColumn.getColumn().setText(COL_STATE);
        stateColumn.getColumn().setToolTipText("Stack State");
        stateColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var cmd = (StackedCommand) element;
                return cmd.getStackedState().getText();
            }

            @Override
            public Color getBackground(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.isArmed()) {
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
                } else if (cmd.getStackedState() == StackedState.ISSUED) {
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_GREEN);
                } else if (cmd.getStackedState() == StackedState.REJECTED) {
                    return styleProvider.getErrorBackgroundColor();
                }

                return super.getBackground(element);
            }

            @Override
            public Color getForeground(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getStackedState() == StackedState.REJECTED) {
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_RED);
                }

                return super.getForeground(element);
            }
        });
        tcl.setColumnData(stateColumn.getColumn(), new ColumnPixelData(80));

        var qColumn = new TableViewerColumn(this, SWT.CENTER);
        qColumn.getColumn().setText(COL_QUEUED);
        qColumn.getColumn().setToolTipText("Queued");
        qColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getQueuedState() == null) {
                    return grayBubble;
                }
                switch (cmd.getQueuedState().getStatus()) {
                case "NA":
                case "OK":
                    return greenBubble;
                case "PENDING":
                    return waitingImage;
                case "NOK":
                    return redBubble;
                default:
                    log.warning("Unexpected state " + cmd.getQueuedState());
                    return grayBubble;
                }
            }
        });
        tcl.setColumnData(qColumn.getColumn(), new ColumnPixelData(50));

        var rColumn = new TableViewerColumn(this, SWT.CENTER);
        rColumn.getColumn().setText(COL_RELEASED);
        rColumn.getColumn().setToolTipText("Released");
        rColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getReleasedState() == null) {
                    return grayBubble;
                }
                switch (cmd.getReleasedState().getStatus()) {
                case "NA":
                case "OK":
                    return greenBubble;
                case "PENDING":
                    return waitingImage;
                case "NOK":
                    return redBubble;
                default:
                    log.warning("Unexpected state " + cmd.getReleasedState());
                    return grayBubble;
                }
            }
        });
        tcl.setColumnData(rColumn.getColumn(), new ColumnPixelData(50));

        var sColumn = new TableViewerColumn(this, SWT.CENTER);
        sColumn.getColumn().setText(COL_SENT);
        sColumn.getColumn().setToolTipText("Sent");
        sColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                var cmd = (StackedCommand) element;
                if (cmd.getSentState() == null) {
                    return grayBubble;
                }
                switch (cmd.getSentState().getStatus()) {
                case "NA":
                case "OK":
                    return greenBubble;
                case "PENDING":
                    return waitingImage;
                case "NOK":
                    return redBubble;
                default:
                    log.warning("Unexpected state " + cmd.getSentState());
                    return grayBubble;
                }
            }
        });
        tcl.setColumnData(sColumn.getColumn(), new ColumnPixelData(50));

        var commentColumn = new TableViewerColumn(this, SWT.LEFT);
        commentColumn.getColumn().setText(COL_COMMENT);
        commentColumn.getColumn().setToolTipText("Comment");
        commentColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var comment = ((StackedCommand) element).getComment();
                if (comment != null && comment.contains("\n")) {
                    var idx = comment.indexOf("\n");
                    return comment.substring(0, idx) + "...";
                }
                return comment;
            }

            @Override
            public String getToolTipText(Object element) {
                var comment = ((StackedCommand) element).getComment();
                return comment;
            }
        });
        tcl.setColumnData(commentColumn.getColumn(), new ColumnPixelData(80));

        // Common properties to all columns
        var columns = new ArrayList<TableViewerColumn>();
        columns.add(rowIdColumn);
        columns.add(nameColumn);
        columns.add(significanceColumn);
        columns.add(constraintsColumn);
        columns.add(constraintsTimeOutColumn);
        columns.add(qColumn);
        columns.add(rColumn);
        columns.add(sColumn);
        columns.add(stateColumn);
        columns.add(commentColumn);
        for (var column : columns) {
            // prevent resize to 0
            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (column.getColumn().getWidth() < 5) {
                        column.getColumn().setWidth(5);
                    }
                }
            });
        }
    }

    public int getIndex(StackedCommand command) {
        return contentProvider.indexOf(command);
    }

    public void addTelecommand(StackedCommand command) {
        contentProvider.addTelecommand(command);
    }

    public void insertTelecommand(StackedCommand command, int index) {
        contentProvider.insertTelecommand(command, index);
    }
}
