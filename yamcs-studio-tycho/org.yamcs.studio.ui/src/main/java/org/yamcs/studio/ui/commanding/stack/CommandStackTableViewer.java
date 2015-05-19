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

public class CommandStackTableViewer extends TableViewer {

    public static final String COL_ROW_ID = "#";
    public static final String COL_COMMAND = "Command";
    public static final String COL_SPTV = "SPTV";
    public static final String COL_DPTV = "DPTV";
    public static final String COL_RELEASE = "Release";
    public static final String COL_ASRUN = "As-Run";

    private final Image level1Image;
    private final Image level2Image;
    private final Image level3Image;
    private final Image level4Image;
    private final Image level5Image;

    private CommandStackView styleProvider;
    private CommandStackTableContentProvider contentProvider;

    public CommandStackTableViewer(Composite parent, TableColumnLayout tcl, CommandStackView styleProvider) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        this.styleProvider = styleProvider;

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        level1Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level1s.png"));
        level2Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level2s.png"));
        level3Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level3s.png"));
        level4Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level4s.png"));
        level5Image = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/level5s.png"));

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);

        contentProvider = new CommandStackTableContentProvider(this);
        setContentProvider(contentProvider);
        setInput(contentProvider); // ! otherwise refresh() deletes everything...
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn significanceColumn = new TableViewerColumn(this, SWT.CENTER);
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
                case critical:
                    return level3Image;
                case distress:
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

        TableViewerColumn rowIdColumn = new TableViewerColumn(this, SWT.CENTER);
        rowIdColumn.getColumn().setText(COL_ROW_ID);
        rowIdColumn.getColumn().setToolTipText("Sequence Number within Stack");
        rowIdColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandStack stack = CommandStack.getInstance();
                if (element.equals(stack.getNextCommand()) && stack.getErrorMessages().isEmpty() && stack.getCommands().size() > 1) {
                    return "\u2022";
                } else {
                    return String.valueOf(contentProvider.indexOf(element) + 1);
                }
            }

            @Override
            public Color getForeground(Object element) {
                CommandStack stack = CommandStack.getInstance();
                if (element.equals(stack.getNextCommand()) && stack.getErrorMessages().isEmpty() && stack.getCommands().size() > 1) {
                    return getTable().getDisplay().getSystemColor(SWT.COLOR_BLUE);
                } else {
                    return super.getForeground(element);
                }
            }
        });
        rowIdColumn.getColumn().setWidth(50);
        tcl.setColumnData(rowIdColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.setLabelProvider(new CommandSourceColumnLabelProvider(styleProvider));
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn sptvColumn = new TableViewerColumn(this, SWT.CENTER);
        sptvColumn.getColumn().setText(COL_SPTV);
        sptvColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                return cmd.isValid() ? "\u2713" : "\u2718";
            }

            @Override
            public Color getForeground(Object element) {
                StackedCommand cmd = (StackedCommand) element;
                return getTable().getDisplay().getSystemColor(cmd.isValid() ? SWT.COLOR_DARK_GREEN : SWT.COLOR_RED);
            }
        });
        tcl.setColumnData(sptvColumn.getColumn(), new ColumnPixelData(50));

        TableViewerColumn dptvColumn = new TableViewerColumn(this, SWT.CENTER);
        dptvColumn.getColumn().setText(COL_DPTV);
        dptvColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "";
            }

            @Override
            public Color getForeground(Object element) {
                return super.getForeground(element);
                //Telecommand cmd = (Telecommand) element;
                //return getTable().getDisplay().getSystemColor(cmd.isValid() ? SWT.COLOR_DARK_GREEN : SWT.COLOR_RED);
            }
        });
        tcl.setColumnData(dptvColumn.getColumn(), new ColumnPixelData(50));

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

        TableViewerColumn asRunColumn = new TableViewerColumn(this, SWT.CENTER);
        asRunColumn.getColumn().setText(COL_ASRUN);
        asRunColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return null;
            }
        });
        tcl.setColumnData(asRunColumn.getColumn(), new ColumnPixelData(80));
    }

    public void addTelecommand(StackedCommand command) {
        contentProvider.addTelecommand(command);
    }
}
