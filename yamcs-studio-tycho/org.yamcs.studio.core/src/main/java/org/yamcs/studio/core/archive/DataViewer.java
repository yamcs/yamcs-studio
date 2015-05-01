package org.yamcs.studio.core.archive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs;

/**
 * Adds controls to a wrapped {@link org.yamcs.ui.archivebrowser.DataView}
 */
public abstract class DataViewer extends NavigatorItem implements ActionListener {

    private ArchivePanel archivePanel;
    private DataView dataView;
    public JToolBar buttonToolbar;

    JButton newTagButton;
    boolean replayEnabled;

    public DataViewer(YamcsConnector yconnector, ArchiveIndexReceiver indexReceiver, ArchivePanel archivePanel, boolean replayEnabled) {
        super(yconnector, indexReceiver);
        this.archivePanel = archivePanel;
        this.replayEnabled = replayEnabled;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(createButtonToolbar(), BorderLayout.NORTH);
        dataView = new DataView(archivePanel, this);
        dataView.addActionListener(this);
        contentPanel.add(dataView, BorderLayout.CENTER);
        return contentPanel;
    }

    @Override
    public void onOpen() {
    }

    @Override
    public void onClose() {
    }

    public void signalSelectionChange(Selection selection) {
        if (selection != null) {
            if (replayEnabled) {
                archivePanel.replayPanel.applySelectionButton.setEnabled(true);
            }
        } else {
            if (replayEnabled) {
                archivePanel.replayPanel.applySelectionButton.setEnabled(false);
            }
        }
    }

    public void addIndex(String tableName, String name) {
        addIndex(tableName, name, -1);
    }

    public void addIndex(String tableName, String name, int mergeTime) {
        dataView.addIndex(tableName, name, mergeTime);
        if (replayEnabled && "tm".equals(tableName)) {
            // TODO move up
            archivePanel.replayPanel.setDataViewer(this);
        }
    }

    public void addVerticalGlue() {
        dataView.addVerticalGlue();
    }

    private JToolBar createButtonToolbar() {
        buttonToolbar = new JToolBar("Button Toolbar");
        buttonToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonToolbar.setBackground(Color.WHITE);
        buttonToolbar.setFloatable(false);
        buttonToolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        newTagButton = new JButton("New Tag");
        newTagButton.setVisible(archivePanel.archiveView.indexReceiver.supportsTags());
        newTagButton.setEnabled(false);
        newTagButton.setToolTipText("Define a new tag for the current selection");
        newTagButton.addActionListener(this);
        newTagButton.setActionCommand("new-tag-button");
        newTagButton.setVisible(false);
        buttonToolbar.add(newTagButton);
        return buttonToolbar;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equalsIgnoreCase("completeness_selection_finished")) {
            if (indexReceiver.supportsTags())
                newTagButton.setEnabled(true);
        } else if (cmd.toLowerCase().endsWith("selection_finished")) {
            if (indexReceiver.supportsTags())
                newTagButton.setEnabled(true);
            if (cmd.startsWith("pp") || cmd.startsWith("tm")) {
                //packetRetrieval.setEnabled(true);
                //parameterRetrieval.setEnabled(true);
            } else if (cmd.startsWith("cmdhist")) {
                //cmdHistRetrieval.setEnabled(true);
            }
        } else if (cmd.equalsIgnoreCase("selection_reset")) {
            if (newTagButton != null)
                newTagButton.setEnabled(false);
        } else if (cmd.equalsIgnoreCase("new-tag-button")) {
            Selection sel = dataView.getSelection();
            dataView.headerPanel.tagBox.createNewTag(sel.getStartInstant(), sel.getStopInstant());
        } else if (cmd.equalsIgnoreCase("insert-tag")) {
            TagBox.TagEvent te = (TagBox.TagEvent) e;
            indexReceiver.insertTag(archivePanel.archiveView.getInstance(), te.newTag);
        } else if (cmd.equalsIgnoreCase("update-tag")) {
            TagBox.TagEvent te = (TagBox.TagEvent) e;
            indexReceiver.updateTag(archivePanel.archiveView.getInstance(), te.oldTag, te.newTag);
        } else if (cmd.equalsIgnoreCase("delete-tag")) {
            TagBox.TagEvent te = (TagBox.TagEvent) e;
            indexReceiver.deleteTag(archivePanel.archiveView.getInstance(), te.oldTag);
        }
    }

    public DataView getDataView() {
        return dataView;
    }

    @Override
    public void startReloading() {
        SwingUtilities.invokeLater(() -> {
            archivePanel.archiveView.setZoomInEnabled(false);
            archivePanel.archiveView.setZoomOutEnabled(false);
            archivePanel.archiveView.setZoomClearEnabled(false);
            if (replayEnabled) {
                archivePanel.replayPanel.applySelectionButton.setEnabled(false);
            }
        });

        for (IndexBox ib : dataView.indexBoxes.values()) {
            ib.startReloading();
        }
        dataView.headerPanel.tagBox.tags.clear();
    }

    @Override
    public void windowResized() {
        if (dataView.zoomStack.isEmpty() || dataView.zoomStack.size() == 1) {
            dataView.refreshDisplay(true);
            if (!dataView.zoomStack.isEmpty()) {
                dataView.setViewLocationFromZoomstack();
            }
        }
    }

    @Override
    public void receiveArchiveRecords(Yamcs.IndexResult ir) {
        if ("completeness".equals(ir.getType())) {
            if (dataView.indexBoxes.containsKey("completeness")) {
                dataView.indexBoxes.get("completeness").receiveArchiveRecords(ir.getRecordsList());
            }
        } else if ("histogram".equals(ir.getType())) {
            String tableName = ir.getTableName();
            if (dataView.indexBoxes.containsKey(tableName)) {
                dataView.indexBoxes.get(tableName).receiveArchiveRecords(ir.getRecordsList());
            }
        } else {
            System.out.println("Received archive records of type " + ir.getType() + " don't know what to do with them");
        }
    }

    @Override
    public void archiveLoadFinished() {
        dataView.archiveLoadFinished();
    }

    @Override
    public void receiveTags(List<Yamcs.ArchiveTag> tagList) {
        dataView.headerPanel.tagBox.addTags(tagList);
    }

    @Override
    public void tagAdded(Yamcs.ArchiveTag ntag) {
        dataView.headerPanel.tagBox.addTag(ntag);
    }

    @Override
    public void tagRemoved(Yamcs.ArchiveTag rtag) {
        dataView.headerPanel.tagBox.removeTag(rtag);
    }

    @Override
    public void tagChanged(Yamcs.ArchiveTag oldTag, Yamcs.ArchiveTag newTag) {
        dataView.headerPanel.tagBox.updateTag(oldTag, newTag);
    }
}
