package org.yamcs.studio.eventlog;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.studio.core.ui.utils.ColumnData;
import org.yamcs.studio.core.ui.utils.ColumnDef;
import org.yamcs.studio.core.ui.utils.ViewerColumnsDialog;

public class EventLogTableViewer extends TableViewer {

    public static final String COL_SEVERITY = "Sev.";
    public static final String COL_SOURCE = "Source";
    public static final String COL_TYPE = "Type";
    public static final String COL_GENERATION = "Generation";
    public static final String COL_RECEPTION = "Reception";
    public static final String COL_MESSAGE = "Message";
    public static final String COL_SEQNUM = "Seq.Nr.";

    private Image infoIcon;
    private Image watchIcon;
    private Image warningIcon;
    private Image distressIcon;
    private Image criticalIcon;
    private Image severeIcon;

    private int messageLineCount;

    private ColumnData columnData;

    private Map<RGB, Color> colorCache = new HashMap<>();

    private ResourceManager resourceManager;

    private EventLogSorter comparator;
    private ControlAdapter columnResizeListener = new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
            syncCurrentWidthsToModel();
            saveColumnState();
        }
    };

    public EventLogTableViewer(Composite parent) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);

        setUseHashlookup(true);

        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        EventLogPlugin plugin = EventLogPlugin.getDefault();
        infoIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level0s.png"));
        watchIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level1s.png"));
        warningIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level2s.png"));
        distressIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level3s.png"));
        criticalIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level4s.png"));
        severeIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level5s.png"));

        messageLineCount = plugin.getMessageLineCount();

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        TableLayout tableLayout = new TableLayout();
        getTable().setLayout(tableLayout);

        columnData = createDefaultColumnData();
        restoreColumnState();
        createColumns();

        comparator = new EventLogSorter();
        setComparator(comparator);
    }

    public ColumnData createDefaultColumnData() {
        ColumnData data = new ColumnData();
        data.addColumn(COL_SEVERITY, 40);
        data.addColumn(COL_GENERATION, 160);
        data.addColumn(COL_MESSAGE, 400);
        data.addColumn(COL_TYPE, 150);
        data.addColumn(COL_SOURCE, 150);
        data.addColumn(COL_RECEPTION, 160, false, true, true);
        data.addColumn(COL_SEQNUM, 80, false, true, true);

        return data;
    }

    @Override
    public EventLogSorter getComparator() {
        return comparator;
    }

    private SelectionAdapter getSelectionAdapter(TableColumn column) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comparator.setColumn(column);
                int dir = comparator.getDirection();
                getTable().setSortDirection(dir);
                getTable().setSortColumn(column);
                refresh();
            }
        };
        return selectionAdapter;
    }

    private class EventLogColumnLabelProvider extends ColumnLabelProvider {

        @Override
        public Color getBackground(Object element) {
            EventLogItem item = (EventLogItem) element;
            if (item.bg != null) {
                return colorCache.computeIfAbsent(item.bg, rgb -> resourceManager.createColor(rgb));
            }
            return super.getBackground(element);
        }

        @Override
        public Color getForeground(Object element) {
            EventLogItem item = (EventLogItem) element;
            if (item.fg != null) {
                return colorCache.computeIfAbsent(item.fg, rgb -> resourceManager.createColor(rgb));
            }
            return super.getForeground(element);
        }
    }

    public void openConfigureColumnsDialog(Shell shell) {
        syncCurrentWidthsToModel();
        ViewerColumnsDialog dialog = new EventLogViewColumnsDialog(shell, EventLogTableViewer.this,
                columnData);
        if (dialog.open() == Dialog.OK) {
            columnData.clear();
            dialog.getVisible().forEach(c -> {
                columnData.addColumn(c.name, c.width, c.visible, c.resizable, c.moveable);
            });
            dialog.getNonVisible().forEach(c -> {
                columnData.addColumn(c.name, c.width, c.visible, c.resizable, c.moveable);
            });
            createColumns();
        }
    }

    private void syncCurrentWidthsToModel() {
        for (TableColumn column : getTable().getColumns()) {
            ColumnDef def = columnData.getColumn(column.getText());
            if (def != null) {
                def.width = column.getWidth();
            }
        }
    }

    private void createColumns() {
        Table table = getTable();
        TableLayout layout = new TableLayout();

        TableColumn[] currentColumns = getTable().getColumns();
        for (TableColumn currentColumn : currentColumns) {
            currentColumn.dispose();
        }

        for (ColumnDef def : columnData.getVisibleColumns()) {
            if (def.name.equals(COL_SEVERITY)) {
                TableViewerColumn severityColumn = new TableViewerColumn(this, SWT.CENTER);
                severityColumn.getColumn().setText(COL_SEVERITY);
                severityColumn.getColumn().addControlListener(columnResizeListener);
                severityColumn.getColumn().addSelectionListener(getSelectionAdapter(severityColumn.getColumn()));
                severityColumn.getColumn().setToolTipText("Severity Level");
                severityColumn.setLabelProvider(new EventLogColumnLabelProvider() {

                    @Override
                    public Image getImage(Object element) {
                        Event event = ((EventLogItem) element).event;
                        if (event.hasSeverity()) {
                            switch (event.getSeverity()) {
                            case INFO:
                                return infoIcon;
                            case WATCH:
                                return watchIcon;
                            case WARNING:
                                return warningIcon;
                            case DISTRESS:
                                return distressIcon;
                            case CRITICAL:
                                return criticalIcon;
                            case SEVERE:
                            case ERROR:
                                return severeIcon;
                            }
                        }
                        return null;
                    }

                    @Override
                    public String getText(Object element) {
                        return null;
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        if (event.hasSeverity()) {
                            return "" + event.getSeverity();
                        } else {
                            return super.getToolTipText(element);
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_GENERATION)) {
                TableViewerColumn generationColumn = new TableViewerColumn(this, SWT.NONE);
                generationColumn.getColumn().setText(COL_GENERATION);
                generationColumn.getColumn().addControlListener(columnResizeListener);
                generationColumn.getColumn().addSelectionListener(getSelectionAdapter(generationColumn.getColumn()));
                generationColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        Instant generationTime = Instant.ofEpochSecond(event.getGenerationTime().getSeconds(),
                                event.getGenerationTime().getNanos());
                        return YamcsUIPlugin.getDefault().formatInstant(generationTime);
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));

                // Add chevron
                // !! Keep these values in sync with EventLogViewerComparator constructor
                table.setSortColumn(generationColumn.getColumn());
                table.setSortDirection(SWT.UP);
            } else if (def.name.equals(COL_TYPE)) {
                TableViewerColumn typeColumn = new TableViewerColumn(this, SWT.NONE);
                typeColumn.getColumn().setText(COL_TYPE);
                typeColumn.getColumn().addControlListener(columnResizeListener);
                typeColumn.getColumn().addSelectionListener(getSelectionAdapter(typeColumn.getColumn()));
                typeColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        return event.hasType() ? event.getType() : "";
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_SOURCE)) {
                TableViewerColumn sourceColumn = new TableViewerColumn(this, SWT.NONE);
                sourceColumn.getColumn().setText(COL_SOURCE);
                sourceColumn.getColumn().addControlListener(columnResizeListener);
                sourceColumn.getColumn().addSelectionListener(getSelectionAdapter(sourceColumn.getColumn()));
                sourceColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        return event.hasSource() ? event.getSource() : "";
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_MESSAGE)) {
                TableViewerColumn messageColumn = new TableViewerColumn(this, SWT.NONE);
                messageColumn.getColumn().setText(COL_MESSAGE);
                messageColumn.getColumn().addControlListener(columnResizeListener);
                messageColumn.getColumn().addSelectionListener(getSelectionAdapter(messageColumn.getColumn()));
                messageColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        String message = event.getMessage();
                        if (messageLineCount > 0) {
                            String lineSeparator = "\n";
                            String[] messageLines = message.split(lineSeparator);
                            message = "";
                            int i = 0;
                            for (; i < messageLineCount && i < messageLines.length; i++) {
                                if (!message.isEmpty()) {
                                    message += lineSeparator;
                                }
                                message += messageLines[i];
                            }
                            if (i + 1 < messageLines.length) {
                                message += " [...]";
                            }
                        }
                        return message;
                    }

                    @Override
                    public Font getFont(Object element) {
                        // Install a monospaced font, for alignment reasons
                        return JFaceResources.getFont(JFaceResources.TEXT_FONT);
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_RECEPTION)) {
                TableViewerColumn receptionColumn = new TableViewerColumn(this, SWT.NONE);
                receptionColumn.getColumn().setText(COL_RECEPTION);
                receptionColumn.getColumn().addControlListener(columnResizeListener);
                receptionColumn.getColumn().addSelectionListener(getSelectionAdapter(receptionColumn.getColumn()));
                receptionColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        Instant receptionTime = Instant.ofEpochSecond(event.getReceptionTime().getSeconds(),
                                event.getReceptionTime().getNanos());
                        return YamcsUIPlugin.getDefault().formatInstant(receptionTime);
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_SEQNUM)) {
                TableViewerColumn seqNumColumn = new TableViewerColumn(this, SWT.RIGHT);
                seqNumColumn.getColumn().setText(COL_SEQNUM);
                seqNumColumn.getColumn().addControlListener(columnResizeListener);
                seqNumColumn.getColumn().addSelectionListener(getSelectionAdapter(seqNumColumn.getColumn()));
                seqNumColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        Event event = ((EventLogItem) element).event;
                        return "" + event.getSeqNumber();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            }
        }

        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.layout(true); // !! Ensures column widths are applied when recreating columns
        refresh(); // !! Ensures table renders correctly for old data when adding a new column

        saveColumnState();
    }

    private void saveColumnState() {
        IDialogSettings settings = EventLogPlugin.getDefault().getCommandHistoryTableSettings();

        List<ColumnDef> visibleColumns = columnData.getVisibleColumns();
        String[] visibleNames = visibleColumns.stream().map(c -> c.name).toArray(String[]::new);
        Integer[] visibleWidths = visibleColumns.stream().map(c -> c.width).toArray(Integer[]::new);

        settings.put("visible-cols", visibleNames);
        for (int i = 0; i < visibleNames.length; i++) {
            settings.put("visible-width-" + i, visibleWidths[i]);
        }

        List<ColumnDef> hiddenColumns = columnData.getHiddenColumns();
        String[] hiddenNames = hiddenColumns.stream().map(c -> c.name).toArray(String[]::new);
        settings.put("hidden-cols", hiddenNames);
    }

    private void restoreColumnState() {
        IDialogSettings settings = EventLogPlugin.getDefault().getCommandHistoryTableSettings();

        String[] oldVisibleNames = settings.getArray("visible-cols");
        if (oldVisibleNames != null) {
            int[] oldVisibleWidths = new int[oldVisibleNames.length];
            for (int i = 0; i < oldVisibleNames.length; i++) {
                oldVisibleWidths[i] = settings.getInt("visible-width-" + i);
            }
            String[] oldHiddenNames = settings.getArray("hidden-cols");

            ColumnData restoredData = new ColumnData();

            // Add visible columns we still remember from a previous session
            for (int i = 0; i < oldVisibleNames.length; i++) {
                ColumnDef def = columnData.getColumn(oldVisibleNames[i]);
                if (def != null) {
                    restoredData.addColumn(def.name, oldVisibleWidths[i], true, def.resizable, def.moveable);
                } else {
                    // Ignore. Maybe a static column was renamed.
                }
            }

            // Add hidden columns we still remember from a previous session
            for (int i = 0; i < oldHiddenNames.length; i++) {
                ColumnDef def = columnData.getColumn(oldHiddenNames[i]);
                if (def != null) {
                    restoredData.addColumn(def.name, def.width, false, def.resizable, def.moveable);
                } else {
                    // Ignore. Maybe a static column was renamed.
                }
            }

            // Ensure that any newly introduced columns remain known (to the right for now)
            for (ColumnDef def : columnData.getColumns()) {
                if (restoredData.getColumn(def.name) == null) {
                    restoredData.addColumn(def.name, def.width, def.visible, def.resizable, def.moveable);
                }
            }

            columnData = restoredData;
        }
    }
}
