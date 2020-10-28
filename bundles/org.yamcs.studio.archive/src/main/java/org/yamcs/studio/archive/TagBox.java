package org.yamcs.studio.archive;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Yamcs.ArchiveTag;

public class TagBox extends Box implements MouseListener {
    private static final long serialVersionUID = 1L;
    private DataView dataView;
    boolean drawPreviewLocator;
    long startLocator, stopLocator, currentLocator;

    final long DO_NOT_DRAW = Long.MIN_VALUE;

    JLabel tagLabelItem;
    JPopupMenu editTagPopup;
    JMenuItem removeTagMenuItem, editTagMenuItem;
    int selectedRow = -1, selectedIndex = -1;

    List<List<ArchiveTag>> tags = new ArrayList<>();// all tags loaded from yarch

    ZoomSpec zoom;

    TagBox(DataView dataView) {
        super(BoxLayout.PAGE_AXIS);
        this.dataView = dataView;

        startLocator = stopLocator = currentLocator = DO_NOT_DRAW;
        drawPreviewLocator = false;
        setOpaque(false);

        buildPopup();
        addMouseListener(this);
        /*
         * insertTag(ArchiveTag.newBuilder().setName("plus infinity").setStart(450).build()); insertTag
         * (ArchiveTag.newBuilder().setName("cucucurigo long laaaaaaabel").setStart(100).setStop
         * (300).setColor("red").build()); insertTag(ArchiveTag.newBuilder().setName("tag2").setStart
         * (TimeEncoding.parse("2009-04-15T05:18:00"
         * )).setStop(TimeEncoding.parse("2009-06-03T18:40:37")).setColor("blue").build());
         * insertTag(ArchiveTag.newBuilder().setName("minus infinity").setStop(150).build());
         * insertTag(ArchiveTag.newBuilder().setName("plus infinity").setStart(450).build()); //
         * insertTag(ArchiveTag.newBuilder().setName("plus infinity").setStart(450).build()); insertTag
         * (ArchiveTag.newBuilder().setName("tag3").setStart(TimeEncoding.parse("2009-07-07T12:29:44"
         * )).setStop(TimeEncoding.parse("2009-07-07T13:28:26")).setColor("orange").build());
         */
    }

    /**
     * insert a tag in tags, in order ensuring no overlap.
     *
     * @param tag
     */
    private void insertTag(ArchiveTag tag) {
        boolean inserted = false;
        for (List<ArchiveTag> atl : tags) {
            int min = 0, max = atl.size() - 1;
            while (min <= max) {
                int mid = (min + max) >> 1;
                ArchiveTag midtag = atl.get(mid);
                if (tag.hasStop() && midtag.hasStart() && tag.getStop() < midtag.getStart()) {
                    max = mid - 1;
                } else if (tag.hasStart() && midtag.hasStop() && tag.getStart() > midtag.getStop()) {
                    min = mid + 1;
                } else {
                    break; // overlap
                }
            }
            if (min > max) {
                atl.add(min, tag);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            List<ArchiveTag> atl = new ArrayList<>();
            atl.add(tag);
            tags.add(atl);
        }
    }

    protected void buildPopup() {
        editTagPopup = new JPopupMenu();
        tagLabelItem = new JLabel();
        tagLabelItem.setEnabled(false);
        Box hbox = Box.createHorizontalBox();
        hbox.add(Box.createHorizontalGlue());
        hbox.add(tagLabelItem);
        hbox.add(Box.createHorizontalGlue());
        editTagPopup.insert(hbox, 0);
        editTagPopup.addSeparator();
        editTagMenuItem = new JMenuItem("Edit Annotation");
        editTagMenuItem.addActionListener(evt -> {
            ArchiveTag selectedTag = tags.get(selectedRow).get(selectedIndex);
            Display.getDefault().asyncExec(() -> {
                CreateAnnotationDialog dialog = new CreateAnnotationDialog(Display.getCurrent().getActiveShell());
                dialog.fillFrom(selectedTag);
                if (dialog.open() == Window.OK) {
                    SwingUtilities.invokeLater(() -> {
                        dataView.emitActionEvent(
                                new TagEvent(this, "update-tag", selectedTag, dialog.buildArchiveTag()));
                    });
                }
            });
        });
        editTagPopup.add(editTagMenuItem);

        removeTagMenuItem = new JMenuItem("Remove Annotation");
        removeTagMenuItem.addActionListener(evt -> {
            ArchiveTag selectedTag = tags.get(selectedRow).get(selectedIndex);
            int answer = JOptionPane.showConfirmDialog(null, "Remove " + selectedTag.getName() + " ?", "Are you sure?",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                dataView.emitActionEvent(new TagEvent(this, "delete-tag", selectedTag, null));
            }
        });
        editTagPopup.add(removeTagMenuItem);
    }

    public void createNewTag(Instant start, Instant stop) {
        Display.getDefault().asyncExec(() -> {
            CreateAnnotationDialog dialog = new CreateAnnotationDialog(Display.getCurrent().getActiveShell());
            dialog.setStartTime(start);
            dialog.setStopTime(stop);
            if (dialog.open() == Window.OK) {
                SwingUtilities.invokeLater(() -> {
                    dataView.emitActionEvent(new TagEvent(this, "insert-tag", null, dialog.buildArchiveTag()));
                });
            }
        });
    }

    public void doMousePressed(MouseEvent e, int row, int index) {
        selectedRow = row;
        selectedIndex = index;
        if (e.isPopupTrigger()) {
            showPopup(e);
        } else if (e.getButton() == MouseEvent.BUTTON1 && selectedRow != -1 && selectedIndex != -1) {
            dataView.selectedTag(tags.get(selectedRow).get(selectedIndex));
        }
    }

    // On windows, the popup trigger comes from the release event
    public void doMouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    void showPopup(final MouseEvent e) {
        if (selectedIndex != -1) {
            ArchiveTag selectedTag = tags.get(selectedRow).get(selectedIndex);
            tagLabelItem.setText(selectedTag.getName());
            editTagPopup.validate();
            editTagPopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /*
     * void hidePopup(final MouseEvent e) { if(true)return; if (e.isPopupTrigger()) { if ((packetPopup != null) &&
     * (popupLabelItem != null)) { popupLabelItem.setVisible(false); removePayloadMenuItem.setVisible(false);
     * removePacketMenuItem.setVisible(false); removeExceptPacketMenuItem.setVisible(false);
     * copyOpsnameMenuItem.setVisible(false); changeColorMenuItem.setVisible(false); packetPopup.validate();
     * packetPopup.show(e.getComponent(), e.getX(), e.getY()); } } }
     */

    void setToZoom(ZoomSpec zoom) {
        this.zoom = zoom;
        redrawTags();
    }

    void redrawTags() { // Draw reverse, so that 'most' tags stick to scale
        removeAll();
        if (!tags.isEmpty()) {
            int row = tags.size() - 1;
            Insets in = this.getInsets();
            for (ListIterator<List<ArchiveTag>> it = tags.listIterator(tags.size()); it.hasPrevious();) {
                List<ArchiveTag> lat = it.previous();
                TagTimeline tt = new TagTimeline(this, lat, zoom, row--, in.left);
                add(tt);
            }
        }
        revalidate();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        doMousePressed(e, -1, -1);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        doMouseReleased(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void addTags(List<ArchiveTag> tagList) {
        for (ArchiveTag tag : tagList) {
            insertTag(tag);
        }
        if (!dataView.zoomStack.empty()) {
            redrawTags();
        }
    }

    public void addTag(ArchiveTag tag) {
        insertTag(tag);
        redrawTags();
    }

    public void removeTag(ArchiveTag rtag) {
        long t = rtag.hasStart() ? rtag.getStart() : rtag.getStop();
        for (List<ArchiveTag> tagList : tags) {
            int id = TagTimeline.time2Tag(tagList, t);
            if (id != -1) {
                if (rtag.equals(tagList.get(id))) {
                    tagList.remove(id);
                    if (tagList.isEmpty()) {
                        tags.remove(tagList);
                    }
                    redrawTags();
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Could not find  " + rtag.toString() + " to remove");
    }

    public void updateTag(ArchiveTag oldTag, ArchiveTag newTag) {
        long t = oldTag.hasStart() ? oldTag.getStart() : oldTag.getStop();
        for (List<ArchiveTag> tagList : tags) {
            int id = TagTimeline.time2Tag(tagList, t);
            if (id != -1) {
                if (oldTag.equals(tagList.get(id))) {
                    tagList.remove(id);
                    insertTag(newTag);
                    redrawTags();
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Could not find  " + oldTag.toString() + " to remove");
    }

    @SuppressWarnings("serial")
    public static class TagEvent extends ActionEvent {
        public ArchiveTag newTag;
        public ArchiveTag oldTag;

        public TagEvent(Object source, String command, ArchiveTag oldTag, ArchiveTag newTag) {
            super(source, ActionEvent.ACTION_PERFORMED, command);
            this.newTag = newTag;
            this.oldTag = oldTag;
        }
    }
}
