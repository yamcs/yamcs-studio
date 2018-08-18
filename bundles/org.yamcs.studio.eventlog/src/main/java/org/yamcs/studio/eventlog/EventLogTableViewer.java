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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class EventLogTableViewer extends TableViewer {

    public static final String COL_SOURCE = "Source";
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

        errorColor = resourceManager.createColor(new RGB(255, 221, 221));
        warningColor = resourceManager.createColor(new RGB(248, 238, 199));

        messageLineCount = EventLogPreferences.getMessageLineCount();

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        tableLayout = new TableLayout();
        getTable().setLayout(tableLayout);

        // sort listener common for all columns
        Listener sortListener = event -> {
            TableColumn column = (TableColumn) event.widget;

            // TODO should this sort logic not move up into this class? Why are columns passed?
            EventLogContentProvider contentProvider = (EventLogContentProvider) getContentProvider();
            /// contentProvider.sort(column);
        };

        TableViewerColumn messageColumn = new TableViewerColumn(this, SWT.NONE);
        messageColumn.getColumn().setText(COL_MESSAGE);
        messageColumn.getColumn().addListener(SWT.Selection, sortListener);
        messageColumn.setLabelProvider(new ColumnLabelProvider() {
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

            @Override
            public Color getBackground(Object element) {
                Event event = (Event) element;
                if (event.hasSeverity()) {
                    switch (event.getSeverity()) {
                    case INFO:
                        return null;
                    case WARNING:
                    case WATCH:
                        return warningColor;
                    case ERROR:
                    case CRITICAL:
                    case SEVERE:
                    case DISTRESS:
                        return errorColor;
                    }
                }
                return null;
            }

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
        });
        tableLayout.addColumnData(new ColumnPixelData(300));

        TableViewerColumn sourceColumn = new TableViewerColumn(this, SWT.NONE);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addListener(SWT.Selection, sortListener);
        sourceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                if (event.hasType()) {
                    return event.getSource() + " :: " + event.getType();
                } else {
                    return event.getSource();
                }
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn generationColumn = new TableViewerColumn(this, SWT.NONE);
        generationColumn.getColumn().setText(COL_GENERATION);
        generationColumn.getColumn().addListener(SWT.Selection, sortListener);
        generationColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return YamcsUIPlugin.getDefault().formatInstant(event.getGenerationTime());
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(160));

        TableViewerColumn receptionColumn = new TableViewerColumn(this, SWT.NONE);
        receptionColumn.getColumn().setText(COL_RECEPTION);
        receptionColumn.getColumn().addListener(SWT.Selection, sortListener);
        receptionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return YamcsUIPlugin.getDefault().formatInstant(event.getReceptionTime());
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(160));

        TableViewerColumn seqNumColumn = new TableViewerColumn(this, SWT.RIGHT);
        seqNumColumn.getColumn().setText(COL_SEQNUM);
        seqNumColumn.getColumn().addListener(SWT.Selection, sortListener);
        seqNumColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event event = (Event) element;
                return "" + event.getSeqNumber();
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(80));

        if (!EventLogPreferences.isShowSequenceNumberColumn()) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            seqNumColumn.getColumn().setResizable(false);
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
