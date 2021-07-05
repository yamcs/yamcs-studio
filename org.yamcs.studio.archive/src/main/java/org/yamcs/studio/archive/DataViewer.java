package org.yamcs.studio.archive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.yamcs.protobuf.Yamcs;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.studio.archive.TagBox.TagEvent;

/**
 * Adds controls to a wrapped {@link org.yamcs.archivebrowser.DataView} TODO merge with DataView. There are no more
 * controls to be found here.
 */
public class DataViewer extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private ArchivePanel archivePanel;
    private DataView dataView;

    private ArchiveIndexReceiver indexReceiver;

    public DataViewer(ArchiveIndexReceiver indexReceiver, ArchivePanel archivePanel) {
        super(new BorderLayout());
        this.indexReceiver = indexReceiver;
        this.archivePanel = archivePanel;

        setBorder(BorderFactory.createEmptyBorder());
        setBackground(Color.WHITE);
        dataView = new DataView(archivePanel, this);
        dataView.addActionListener(this);
        add(dataView, BorderLayout.CENTER);

        addIndex("completeness", "completeness index");
        addIndex("tm", "tm histogram", 1000);
        addIndex("pp", "pp histogram", 1000);
        addIndex("cmdhist", "cmdhist histogram", 1000);
        addVerticalGlue();
    }

    public void clearView() {
        this.dataView.clearArchiveRecords();
    }

    public void signalSelectionChange(Selection selection) {
        // TODO should probably update swt buttons here
    }

    public void addIndex(String tableName, String name) {
        addIndex(tableName, name, -1);
    }

    public void addIndex(String tableName, String name, int mergeTime) {
        dataView.addIndex(tableName, name, mergeTime);
    }

    public void addVerticalGlue() {
        dataView.addVerticalGlue();
    }

    public void zoomIn() {
        dataView.zoomIn();
        archivePanel.archiveView.setZoomOutEnabled(true);
    }

    public void zoomOut() {
        dataView.zoomOut();
        archivePanel.archiveView.setZoomOutEnabled(dataView.zoomStack.size() > 1);
    }

    public void clearZoom() {
        dataView.showAll();
        archivePanel.archiveView.setZoomOutEnabled(false);
    }

    public void tagSelectedRange() {
        Selection sel = dataView.getSelection();
        Instant start = Instant.ofEpochMilli(sel.getStartInstant());
        Instant stop = Instant.ofEpochMilli(sel.getStopInstant());
        dataView.headerPanel.tagBox.createNewTag(start, stop);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equalsIgnoreCase("completeness_selection_finished")) {
            archivePanel.archiveView.setTagEnabled(true);
        } else if (cmd.toLowerCase().endsWith("selection_finished")) {
            archivePanel.archiveView.setTagEnabled(true);
            if (cmd.startsWith("pp") || cmd.startsWith("tm")) {
                // packetRetrieval.setEnabled(true);
                // parameterRetrieval.setEnabled(true);
            } else if (cmd.startsWith("cmdhist")) {
                // cmdHistRetrieval.setEnabled(true);
            }
        } else if (cmd.equalsIgnoreCase("selection_reset")) {
            archivePanel.archiveView.setTagEnabled(false);
        } else if (cmd.equalsIgnoreCase("insert-tag")) {
            TagEvent te = (TagEvent) e;
            indexReceiver.createTag(te.newTag);
        } else if (cmd.equalsIgnoreCase("update-tag")) {
            TagEvent te = (TagEvent) e;
            indexReceiver.updateTag(te.oldTag, te.newTag);
        } else if (cmd.equalsIgnoreCase("delete-tag")) {
            TagEvent te = (TagEvent) e;
            indexReceiver.deleteTag(te.oldTag);
        }
    }

    public DataView getDataView() {
        return dataView;
    }

    public void startReloading() {
        SwingUtilities.invokeLater(() -> {
            archivePanel.archiveView.setZoomInEnabled(false);
            archivePanel.archiveView.setZoomOutEnabled(false);
            archivePanel.archiveView.setZoomClearEnabled(false);
        });

        for (IndexBox ib : dataView.indexBoxes.values()) {
            ib.startReloading();
        }
        dataView.headerPanel.tagBox.tags.clear();
    }

    public void windowResized() {
        if (dataView.zoomStack.isEmpty() || dataView.zoomStack.size() == 1) {
            dataView.refreshDisplay(true);
            if (!dataView.zoomStack.isEmpty()) {
                dataView.setViewLocationFromZoomstack();
            }
        }
    }

    public void receiveArchiveRecords(Yamcs.IndexResult ir) {
        if ("COMPLETENESS".equals(ir.getType())) {
            if (dataView.indexBoxes.containsKey("completeness")) {
                dataView.indexBoxes.get("completeness").receiveArchiveRecords(ir.getRecordsList());
            }
        } else if ("HISTOGRAM".equals(ir.getType())) {
            String tableName = ir.getTableName();
            if (dataView.indexBoxes.containsKey(tableName)) {
                dataView.indexBoxes.get(tableName).receiveArchiveRecords(ir.getRecordsList());
            }
        } else {
            System.out.println("Received archive records of type " + ir.getType() + " don't know what to do with them");
        }
    }

    public void archiveLoadFinished() {
        dataView.archiveLoadFinished();
    }

    public void receiveTags(List<ArchiveTag> tagList) {
        dataView.headerPanel.tagBox.addTags(tagList);
    }

    public void tagAdded(ArchiveTag ntag) {
        dataView.headerPanel.tagBox.addTag(ntag);
    }

    public void tagRemoved(ArchiveTag rtag) {
        dataView.headerPanel.tagBox.removeTag(rtag);
    }

    public void tagChanged(ArchiveTag oldTag, ArchiveTag newTag) {
        dataView.headerPanel.tagBox.updateTag(oldTag, newTag);
    }

    public void dispose() {
        dataView.dispose();
    }
}
