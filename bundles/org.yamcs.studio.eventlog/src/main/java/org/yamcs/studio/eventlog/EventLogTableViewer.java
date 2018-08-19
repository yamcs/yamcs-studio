package org.yamcs.studio.eventlog;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

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

    private Color errorColor;
    private Color warningColor;

    private int messageLineCount;

    private TableLayout tableLayout;

    private EventLogViewerComparator comparator;

    public EventLogTableViewer(Composite parent) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);

        setUseHashlookup(true);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        Activator plugin = Activator.getDefault();
        infoIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level0s.png"));
        watchIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level1s.png"));
        warningIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level2s.png"));
        distressIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level3s.png"));
        criticalIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level4s.png"));
        severeIcon = resourceManager.createImage(plugin.getImageDescriptor("icons/eview16/level5s.png"));

        errorColor = resourceManager.createColor(new RGB(255, 0, 0));
        warningColor = resourceManager.createColor(new RGB(255, 127, 0));

        messageLineCount = EventLogPreferences.getMessageLineCount();

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        tableLayout = new TableLayout();
        getTable().setLayout(tableLayout);

        TableViewerColumn severityColumn = new TableViewerColumn(this, SWT.CENTER);
        severityColumn.getColumn().setText(COL_SEVERITY);
        severityColumn.getColumn().addSelectionListener(getSelectionAdapter(severityColumn.getColumn()));
        severityColumn.getColumn().setToolTipText("Severity Level");
        severityColumn.setLabelProvider(new EventLogColumnLabelProvider() {

            @Override
            public Image getImage(Object element) {
                Event event = (Event) element;
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
                Event event = (Event) element;
                if (event.hasSeverity()) {
                    return "" + event.getSeverity();
                } else {
                    return super.getToolTipText(element);
                }
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(40));

        TableViewerColumn generationColumn = new TableViewerColumn(this, SWT.NONE);
        generationColumn.getColumn().setText(COL_GENERATION);
        generationColumn.getColumn().addSelectionListener(getSelectionAdapter(generationColumn.getColumn()));
        generationColumn.setLabelProvider(new EventLogColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return YamcsUIPlugin.getDefault().formatInstant(event.getGenerationTime());
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(160));

        TableViewerColumn messageColumn = new TableViewerColumn(this, SWT.NONE);
        messageColumn.getColumn().setText(COL_MESSAGE);
        messageColumn.getColumn().addSelectionListener(getSelectionAdapter(messageColumn.getColumn()));
        messageColumn.setLabelProvider(new EventLogColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
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
        tableLayout.addColumnData(new ColumnPixelData(400));

        TableViewerColumn typeColumn = new TableViewerColumn(this, SWT.NONE);
        typeColumn.getColumn().setText(COL_TYPE);
        typeColumn.getColumn().addSelectionListener(getSelectionAdapter(typeColumn.getColumn()));
        typeColumn.setLabelProvider(new EventLogColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return event.hasType() ? event.getType() : "";
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn sourceColumn = new TableViewerColumn(this, SWT.NONE);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addSelectionListener(getSelectionAdapter(sourceColumn.getColumn()));
        sourceColumn.setLabelProvider(new EventLogColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return event.hasSource() ? event.getSource() : "";
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(150));

        if (EventLogPreferences.isShowReceptionColumn()) {
            TableViewerColumn receptionColumn = new TableViewerColumn(this, SWT.NONE);
            receptionColumn.getColumn().setText(COL_RECEPTION);
            receptionColumn.getColumn().addSelectionListener(getSelectionAdapter(receptionColumn.getColumn()));
            receptionColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    Event event = (Event) element;
                    return YamcsUIPlugin.getDefault().formatInstant(event.getReceptionTime());
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(160));
        }

        if (EventLogPreferences.isShowSequenceNumberColumn()) {
            TableViewerColumn seqNumColumn = new TableViewerColumn(this, SWT.RIGHT);
            seqNumColumn.getColumn().setText(COL_SEQNUM);
            seqNumColumn.getColumn().addSelectionListener(getSelectionAdapter(seqNumColumn.getColumn()));
            seqNumColumn.setLabelProvider(new EventLogColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    Event event = (Event) element;
                    return "" + event.getSeqNumber();
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(80));
        }

        comparator = new EventLogViewerComparator();
        setComparator(comparator);

        // !! Keep these values in sync with EventLogViewerComparator constructor
        getTable().setSortColumn(generationColumn.getColumn());
        getTable().setSortDirection(SWT.UP);
    }

    @Override
    public EventLogViewerComparator getComparator() {
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
        public Color getForeground(Object element) {
            Event event = (Event) element;
            if (event.hasSeverity()) {
                switch (event.getSeverity()) {
                case INFO:
                    return null;
                case WATCH:
                case WARNING:
                    return warningColor;
                case DISTRESS:
                case CRITICAL:
                case SEVERE:
                case ERROR:
                    return errorColor;
                }
            }
            return null;
        }
    }
}
