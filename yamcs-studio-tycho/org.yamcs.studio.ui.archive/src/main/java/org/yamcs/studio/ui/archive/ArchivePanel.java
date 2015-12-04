package org.yamcs.studio.ui.archive;

import static org.yamcs.utils.TimeEncoding.INVALID_INSTANT;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.EditProcessorRequest;
import org.yamcs.protobuf.Yamcs.ArchiveRecord;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.Prefs;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.css.OPIUtils;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

/**
 * Main panel of the ArchiveBrowser
 *
 * @author nm
 *
 */
public class ArchivePanel extends JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ArchivePanel.class.getName());

    ProgressMonitor progressMonitor;

    ArchiveView archiveView;

    Prefs prefs;

    private DataViewer dataViewer;

    int loadCount, recCount;
    boolean passiveUpdate = false;

    long dataStart = TimeEncoding.INVALID_INSTANT;
    long dataStop = TimeEncoding.INVALID_INSTANT;

    volatile boolean lowOnMemoryReported = false;

    public ArchivePanel(ArchiveView archiveView) {
        super(new BorderLayout());
        this.archiveView = archiveView;

        prefs = new Prefs();
        dataViewer = new DataViewer(archiveView.indexReceiver, this);
        add(dataViewer, BorderLayout.CENTER);

        // Catch mouse events globally, to deal more easily with events on child components
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) { // EDT
                DataView dataView = dataViewer.getDataView();
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
        }, AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public DataViewer getDataViewer() {
        return dataViewer;
    }

    public void startReloading() {
        recCount = 0;

        setBusyPointer();
        archiveView.setRefreshEnabled(false);

        dataViewer.startReloading();

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

    public void setBusyPointer() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void setNormalPointer() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public TimeInterval getRequestedDataInterval() {
        return prefs.getInterval();
    }

    public synchronized void receiveArchiveRecords(IndexResult ir) {
        dataViewer.receiveArchiveRecords(ir);
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
            JOptionPane.showMessageDialog(ArchivePanel.this, "Error when receiving archive records: " + errorMessage,
                    "error receiving archive records", JOptionPane.ERROR_MESSAGE);
            archiveView.setRefreshEnabled(true);
            setNormalPointer();
        });
    }

    void seekReplay(long newPosition) {
        if (newPosition == TimeEncoding.INVALID_INSTANT)
            return;

        Display.getDefault().asyncExec(() -> {
            ProcessorInfo processor = ManagementCatalogue.getInstance().getCurrentProcessorInfo();
            if (processor == null || processor.getName().equals("realtime"))
                return;

            String seekTime = TimeEncoding.toString(newPosition);
            EditProcessorRequest req = EditProcessorRequest.newBuilder().setSeek(seekTime).build();
            ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
            catalogue.editProcessorRequest(processor.getName(), req, new ResponseHandler() {
                @Override
                public void onMessage(MessageLite responseMsg) {
                    Display.getDefault().asyncExec(() -> {
                        OPIUtils.resetDisplays();
                    });
                }

                @Override
                public void onException(Exception e) {
                    log.log(Level.SEVERE, "Could not seek", e);
                    Display.getDefault().asyncExec(() -> {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage());
                    });
                }
            });
        });
    }

    public synchronized void archiveLoadFinished() {
        loadCount = 0;
        if ((dataStart != INVALID_INSTANT) && (dataStop != INVALID_INSTANT))
            dataViewer.archiveLoadFinished();

        SwingUtilities.invokeLater(() -> {
            archiveView.setRefreshEnabled(true);
            setNormalPointer();
        });
    }

    public void tagAdded(ArchiveTag ntag) {
        dataViewer.tagAdded(ntag);
    }

    public void tagRemoved(ArchiveTag rtag) {
        dataViewer.tagRemoved(rtag);
    }

    public void tagChanged(ArchiveTag oldTag, ArchiveTag newTag) {
        dataViewer.tagChanged(oldTag, newTag);
    }

    public void tagsAdded(List<ArchiveTag> tagList) {
        dataViewer.receiveTags(tagList);
    }

    // TODO only used by selector. Rework maybe in custom replay launcher
    public Selection getSelection() {
        return dataViewer.getDataView().getSelection();
    }

    // TODO only used by selector. Rework maybe in custom replay launcher
    public List<String> getSelectedPackets(String tableName) {
        if (dataViewer.getDataView().indexBoxes.containsKey(tableName))
            return dataViewer.getDataView().getSelectedPackets(tableName);
        return Collections.emptyList();
    }

    public void onWindowResized() {
        dataViewer.windowResized();
    }
}
