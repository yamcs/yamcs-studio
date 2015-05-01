package org.yamcs.studio.core.archive;

import static org.yamcs.utils.TimeEncoding.INVALID_INSTANT;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.yamcs.protobuf.Yamcs.ArchiveRecord;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.utils.TimeEncoding;

/**
 * Main panel of the ArchiveBrowser
 *
 * @author nm
 *
 */
public class ArchivePanel extends JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    ProgressMonitor progressMonitor;

    ArchiveView archiveView;

    private LinkedHashMap<String, NavigatorItem> itemsByName = new LinkedHashMap<>();

    protected Prefs prefs;

    private JPanel navigatorItemPanel; // Contains switchable items
    private NavigatorItem activeItem; // Currently opened item from SideNav
    public ReplayPanel replayPanel;

    int loadCount, recCount;
    boolean passiveUpdate = false;

    long dataStart = TimeEncoding.INVALID_INSTANT;
    long dataStop = TimeEncoding.INVALID_INSTANT;

    volatile boolean lowOnMemoryReported = false;

    public ArchivePanel(ArchiveView archiveView, boolean replayEnabled) {
        super(new BorderLayout());
        this.archiveView = archiveView;

        prefs = new Prefs();

        //
        // transport control panel (only enabled when a HRDP data channel is selected)
        //
        if (replayEnabled) {
            replayPanel = new ReplayPanel();
            replayPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Replay Control"));
            replayPanel.setToolTipText("Doubleclick between the start/stop locators to reposition the replay.");
            replayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            // fixedTop.add(replayPanel);
        }

        // This is a bit clumsy right now with the inner classes, but will make more
        // sense once we add custom components to different DataViewers.
        DataViewer allViewer = new DataViewer(archiveView.yconnector, archiveView.indexReceiver, this, replayEnabled) {
            @Override
            public String getLabelName() {
                return "Archive";
            }

            @Override
            public JComponent createContentPanel() {
                JComponent component = super.createContentPanel();
                addIndex("completeness", "completeness index");
                addIndex("tm", "tm histogram", 1000);
                addIndex("pp", "pp histogram", 1000);
                addIndex("cmdhist", "cmdhist histogram", 1000);
                addVerticalGlue();
                return component;
            }
        };
        itemsByName.put(allViewer.getLabelName(), allViewer);

        navigatorItemPanel = new JPanel(new CardLayout());
        add(navigatorItemPanel, BorderLayout.CENTER);

        for (Map.Entry<String, NavigatorItem> entry : itemsByName.entrySet()) {
            String name = entry.getKey();
            NavigatorItem navigatorItem = entry.getValue();
            navigatorItemPanel.add(navigatorItem.getContentPanel(), name);
        }

        openItem("Archive");

        // Catch mouse events globally, to deal more easily with events on child components
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) { // EDT
                if (activeItem instanceof DataViewer) {
                    DataView dataView = ((DataViewer) activeItem).getDataView();
                    if (!(event.getSource() instanceof JScrollBar)
                            && !(event.getSource() instanceof TagTimeline)
                            && SwingUtilities.isDescendingFrom((Component) event.getSource(), dataView)) {
                        MouseEvent me = SwingUtilities.convertMouseEvent((Component) event.getSource(), (MouseEvent) event, dataView.indexPanel);
                        if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
                            dataView.doMouseDragged(me);
                        } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                            dataView.doMousePressed(me);
                        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
                            dataView.doMouseReleased(me);
                        } else if (event.getID() == MouseEvent.MOUSE_MOVED) {
                            dataView.doMouseMoved(me);
                        } else if (event.getID() == MouseEvent.MOUSE_EXITED) {
                            dataView.doMouseExited(me);
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public DataViewer getDataViewer() {
        return (DataViewer) activeItem;
    }

    public void openItem(String name) {
        NavigatorItem item = getItemByName(name);
        fireIntentionToSwitchActiveItem(item);
    }

    public NavigatorItem getItemByName(String name) {
        return itemsByName.get(name);
    }

    void fireIntentionToSwitchActiveItem(NavigatorItem sourceItem) {
        if (activeItem == sourceItem)
            return;

        CardLayout ncl = (CardLayout) navigatorItemPanel.getLayout();
        ncl.show(navigatorItemPanel, sourceItem.getLabelName());

        if (activeItem != null)
            activeItem.onClose();
        sourceItem.onOpen();
        activeItem = sourceItem;
    }

    public void startReloading() {
        recCount = 0;
        archiveView.setInstance("simulator"); // FIXME

        setBusyPointer();
        archiveView.setRefreshEnabled(false);

        for (NavigatorItem item : itemsByName.values()) {
            item.startReloading();
        }

        if (lowOnMemoryReported) {
            System.gc();
            lowOnMemoryReported = false;
        }
        dataStart = dataStop = INVALID_INSTANT;
    }

    static protected void debugLog(String s) {
        System.out.println(s);
    }

    static protected void debugLogComponent(String name, JComponent c) {
        Insets in = c.getInsets();
        debugLog("component " + name + ": "
                + "min(" + c.getMinimumSize().width + "," + c.getMinimumSize().height + ") "
                + "pref(" + c.getPreferredSize().width + "," + c.getPreferredSize().height + ") "
                + "max(" + c.getMaximumSize().width + "," + c.getMaximumSize().height + ") "
                + "size(" + c.getSize().width + "," + c.getSize().height + ") "
                + "insets(" + in.top + "," + in.left + "," + in.bottom + "," + in.right + ")");
    }

    void playOrStopPressed() {
        // to be reimplemented by subclass ArchiveReplay in YamcsMonitor
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        debugLog(e.getPropertyName() + "/" + e.getOldValue() + "/" + e.getNewValue());
    }

    static class IndexChunkSpec implements Comparable<IndexChunkSpec> {
        long startInstant, stopInstant;
        int tmcount;
        String info;

        IndexChunkSpec(long start, long stop, int tmcount, String info) {
            this.startInstant = start;
            this.stopInstant = stop;
            this.tmcount = tmcount;
            this.info = info;
        }

        /**
         *
         * @return frequency in Hz
         */
        float getFrequency() {
            float freq = (float) (tmcount - 1) / ((stopInstant - startInstant) / 1000.0f);
            freq = Math.round(freq * 1000) / 1000.0f;
            return freq;
        }

        @Override
        public int compareTo(IndexChunkSpec a) {
            return Long.signum(startInstant - a.startInstant);
        }

        //merge two records if close enough to eachother
        public boolean merge(IndexChunkSpec t, long mergeTime) {
            boolean merge = false;
            if (tmcount == 1) {
                if (t.startInstant - stopInstant < mergeTime)
                    merge = true;
            } else {
                float dist = (stopInstant - startInstant) / ((float) (tmcount - 1));
                if (t.startInstant - stopInstant < dist + mergeTime)
                    merge = true;
            }
            if (merge) {
                stopInstant = t.stopInstant;
                tmcount += t.tmcount;
            }
            return merge;
        }

        @Override
        public String toString() {
            return "start: " + startInstant + " stop: " + stopInstant + " count:" + tmcount;
        }
    }

    public void connected() {
        archiveView.setRefreshEnabled(true);
    }

    public void disconnected() {
        archiveView.setRefreshEnabled(false);
    }

    public void setBusyPointer() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void setNormalPointer() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public TimeInterval getRequestedDataInterval() {
        return prefs.getInterval();
    }

    public long getRequestedDataStop() {
        return prefs.getEndTimestamp();
    }

    public long getRequestedDataStart() {
        return prefs.getStartTimestamp();
    }

    public synchronized void receiveArchiveRecords(IndexResult ir) {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.receiveArchiveRecords(ir);
        }

        long start, stop;
        for (ArchiveRecord r : ir.getRecordsList()) {
            start = r.getFirst();
            stop = r.getLast();

            if ((dataStart == INVALID_INSTANT) || (start < dataStart))
                dataStart = start;
            if ((dataStop == INVALID_INSTANT) || (stop > dataStop))
                dataStop = stop;

            recCount++;
            loadCount++;
        }
    }

    public void receiveArchiveRecordsError(final String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            for (NavigatorItem navigatorItem : itemsByName.values()) {
                navigatorItem.receiveArchiveRecordsError(errorMessage);
            }
            JOptionPane.showMessageDialog(ArchivePanel.this, "Error when receiving archive records: " + errorMessage,
                    "error receiving archive records", JOptionPane.ERROR_MESSAGE);
            archiveView.setRefreshEnabled(true);
            setNormalPointer();
        });
    }

    void seekReplay(long newPosition) {
        replayPanel.seekReplay(newPosition);
    }

    public synchronized void archiveLoadFinished() {
        loadCount = 0;
        if ((dataStart != INVALID_INSTANT) && (dataStop != INVALID_INSTANT)) {
            for (NavigatorItem item : itemsByName.values()) {
                item.archiveLoadFinished();
            }
            prefs.savePreferences();
        }

        SwingUtilities.invokeLater(() -> {
            archiveView.setRefreshEnabled(true);
            setNormalPointer();
        });
    }

    public void tagAdded(ArchiveTag ntag) {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.tagAdded(ntag);
        }
    }

    public void tagRemoved(ArchiveTag rtag) {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.tagRemoved(rtag);
        }
    }

    public void tagChanged(ArchiveTag oldTag, ArchiveTag newTag) {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.tagChanged(oldTag, newTag);
        }
    }

    public void tagsAdded(List<ArchiveTag> tagList) {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.receiveTags(tagList);
        }
    }

    // TODO only used by selector. Rework maybe in custom replay launcher
    public Selection getSelection() {
        DataViewer dataViewer = (DataViewer) activeItem;
        return dataViewer.getDataView().getSelection();
    }

    // TODO only used by selector. Rework maybe in custom replay launcher
    public List<String> getSelectedPackets(String tableName) {
        DataViewer dataViewer = (DataViewer) activeItem;
        if (dataViewer.getDataView().indexBoxes.containsKey(tableName)) {
            return dataViewer.getDataView().getSelectedPackets("tm");
        }
        return Collections.emptyList();
    }

    public void onWindowResizing() {
        activeItem.windowResized();
    }

    public void onWindowResized() {
        for (NavigatorItem navigatorItem : itemsByName.values()) {
            navigatorItem.windowResized();
        }
    }
}
