package org.yamcs.studio.archive;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.yamcs.protobuf.Yamcs.ArchiveRecord;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.archive.ArchivePanel.IndexChunkSpec;

/**
 * Represents a collection of IndexLine shown vertically
 *
 * @author nm
 *
 */
public class IndexBox extends Box {
    private static final long serialVersionUID = 1L;

    public static final Color BORDER_COLOR = new Color(216, 216, 216);
    private static final Color PACKET_LABEL_COLOR = new Color(102, 102, 102);
    DataView dataView;

    JLabel popupLabelItem;
    static final int tmRowHeight = 20;

    HashMap<String, IndexLineSpec> allPackets;
    HashMap<String, ArrayList<IndexLineSpec>> groups;
    HashMap<String, TreeSet<IndexChunkSpec>> tmData;
    private ZoomSpec zoom;
    private String name;

    /**
     * because the histogram contains regular splits each 3600 seconds, merge here the records that are close enough to
     * each other. -1 means no merging
     */
    long mergeTime = -1;
    Preferences prefs;

    private JPanel topPanel;
    private JPanel centerPanel;
    private List<IndexLine> indexLines = new ArrayList<>();

    private JLabel titleLabel;

    IndexBox(DataView dataView, String name) {
        super(BoxLayout.Y_AXIS);
        topPanel = new JPanel(new GridBagLayout()); // In panel, so that border can fill width
        Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR);
        Border insideBorder = BorderFactory.createEmptyBorder(10, 0, 2, 0);
        topPanel.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
        topPanel.setBackground(Color.WHITE);

        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;
        titleLabel = new JLabel(name);
        titleLabel.setBackground(Color.red);
        titleLabel
                .setMaximumSize(new Dimension(titleLabel.getMaximumSize().width, titleLabel.getPreferredSize().height));
        // titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setFont(titleLabel.getFont().deriveFont(~Font.BOLD));
        topPanel.setMaximumSize(
                new Dimension(topPanel.getMaximumSize().width, titleLabel.getPreferredSize().height + 13));
        topPanel.add(titleLabel, cons);
        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(topPanel);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(centerPanel);

        this.dataView = dataView;
        this.name = name;

        allPackets = new HashMap<>();
        groups = new HashMap<>();
        tmData = new HashMap<>();
    }

    void removeIndexLines() {
        centerPanel.removeAll();
        indexLines.clear();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2d.setPaint(new GradientPaint(0, topPanel.getHeight(), new Color(251, 251, 251), 0, panelHeight, Color.WHITE));
        g2d.fillRect(0, topPanel.getHeight(), panelWidth, panelHeight - topPanel.getHeight());
    }

    public void setToZoom(ZoomSpec zoom) {
        this.zoom = zoom;
        removeIndexLines();
        if (groups.isEmpty()) {
            showEmptyLabel("No " + name + " data loaded");
        } else {
            boolean empty = true;
            for (ArrayList<IndexLineSpec> plvec : groups.values()) {
                for (IndexLineSpec pkt : plvec) {
                    if (pkt.enabled) {
                        empty = false;
                        // create panel that contains the index blocks
                        IndexLine line = new IndexLine(this, pkt);

                        centerPanel.add(line);
                        indexLines.add(line);
                        redrawTmPanel(pkt);
                    }
                }
            }

            if (empty) {
                showEmptyLabel("Right click for " + name + " data");
            }
        }
    }

    private void showEmptyLabel(String msg) {
        JLabel nodata = new JLabel(msg);
        nodata.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, nodata.getFont().getSize()));
        nodata.setForeground(Color.lightGray);
        Box b = Box.createHorizontalBox();
        b.setBorder(BorderFactory.createEmptyBorder());
        b.add(nodata);
        b.add(Box.createHorizontalGlue());
        b.setMaximumSize(new Dimension(b.getMaximumSize().width, b.getPreferredSize().height));
        centerPanel.add(b);
    }

    public void receiveArchiveRecords(List<ArchiveRecord> records) {
        String[] nameparts;
        synchronized (tmData) {
            // progressMonitor.setProgress(30);
            // progressMonitor.setNote("Receiving data");

            for (ArchiveRecord r : records) {
                // debugLog(r.packet+"\t"+r.num+"\t"+new Date(r.first)+"\t"+new Date(r.last));
                NamedObjectId id = r.getId();
                String grpName = null;
                String shortName = null;
                // split the id into group->name
                if (!id.hasNamespace()) {
                    int idx = id.getName().lastIndexOf("/");
                    if (idx != -1) {
                        grpName = id.getName().substring(0, idx + 1);
                        shortName = id.getName().substring(idx + 1);
                    }
                }
                if (grpName == null) {
                    nameparts = id.getName().split("[_\\.]", 2);
                    if (nameparts.length > 1) {
                        grpName = nameparts[0];
                        shortName = nameparts[1].replaceFirst("INST_", "").replaceFirst("Tlm_Pkt_", "");
                    } else {
                        grpName = "";
                        shortName = id.getName();
                    }
                }
                if (!tmData.containsKey(id.getName())) {
                    tmData.put(id.getName(), new TreeSet<IndexChunkSpec>());
                }
                TreeSet<IndexChunkSpec> al = tmData.get(id.getName());
                long first = Instant.ofEpochSecond(r.getFirst().getSeconds()).toEpochMilli();
                long last = Instant.ofEpochSecond(r.getLast().getSeconds()).toEpochMilli();
                IndexChunkSpec tnew = new IndexChunkSpec(first, last, r.getNum(), null);
                IndexChunkSpec told = al.floor(tnew);
                if ((told == null) || (mergeTime == -1) || (!told.merge(tnew, mergeTime))) {
                    al.add(tnew);
                }
                if (!allPackets.containsKey(id.getName())) {
                    IndexLineSpec pkt = new IndexLineSpec(id.getName(), grpName, shortName);
                    allPackets.put(id.getName(), pkt);
                    ArrayList<IndexLineSpec> plvec;
                    if ((plvec = groups.get(grpName)) == null) {
                        plvec = new ArrayList<>();
                        groups.put(grpName, plvec);
                    }
                    plvec.add(pkt);
                }
            }
            titleLabel.setText(name);
        }
    }

    public void startReloading() {
        allPackets.clear();
        groups.clear();
        tmData.clear();
    }

    public List<String> getPacketsForSelection(Selection selection) {
        ArrayList<String> packets = new ArrayList<>();
        for (ArrayList<IndexLineSpec> plvec : groups.values()) {
            for (IndexLineSpec pkt : plvec) {
                if (pkt.enabled) {
                    packets.add(pkt.lineName);
                }
            }
        }
        return packets;
    }

    public void dataLoadFinished() {
        for (Entry<String, IndexLineSpec> entry : allPackets.entrySet()) {
            IndexLineSpec pkt = entry.getValue();
            if (pkt != null) {
                pkt.enabled = true;
            } else {
                ArchivePanel.debugLog("could not enable packet '" + entry.getKey() + "', removing line from view");
            }
        }
        titleLabel.setText(name);
    }

    void redrawTmPanel(IndexLineSpec pkt) {
        IndexLine indexLine = pkt.assocIndexLine;
        indexLine.setOpaque(false);
        final int stopx = zoom.getPixels();
        final Insets in = indexLine.getInsets();
        final int panelw = zoom.getPixels();
        JLabel pktlab;
        Font font = null;
        int x1, y = 0;// , count = 0;

        indexLine.removeAll();

        // debugLog("redrawTmPanel() "+pkt.name+" mark 1");
        // set labels
        x1 = 10;
        indexLine.setBackground(Color.RED);
        do {
            pktlab = new JLabel(pkt.lineName);
            pktlab.setForeground(PACKET_LABEL_COLOR);
            if (font == null) {
                font = pktlab.getFont();
                font = font.deriveFont((float) (font.getSize() - 3));
            }
            pktlab.setFont(font);
            pktlab.setBounds(x1 + in.left, in.top, pktlab.getPreferredSize().width, pktlab.getPreferredSize().height);
            indexLine.add(pktlab);

            if (y == 0) {
                y = in.top + pktlab.getSize().height;
                indexLine.setPreferredSize(new Dimension(panelw, y + tmRowHeight + in.bottom));
                indexLine.setMinimumSize(indexLine.getPreferredSize());
                indexLine.setMaximumSize(indexLine.getPreferredSize());
            }
            x1 += 600;
        } while (x1 < panelw - pktlab.getSize().width);

        TreeSet<IndexChunkSpec> ts = tmData.get(pkt.lineName);
        if (ts != null) {
            Timeline tmt = new Timeline(this, pkt, ts, zoom, in.left);
            tmt.setBounds(in.left, y, stopx, tmRowHeight);
            indexLine.add(tmt);
        }

        // centerPanel.setPreferredSize(new Dimension(panelw, centerPanel.getPreferredSize().height));
        // centerPanel.setMaximumSize(centerPanel.getPreferredSize());

        indexLine.revalidate();
        indexLine.repaint();

        // System.out.println("indexLine.preferred size: "+indexLine.getPreferredSize());
    }

    class IndexLineSpec implements Comparable<IndexLineSpec> {
        String shortName, lineName;
        String grpName;
        boolean enabled;
        JComponent assocLabel;
        IndexLine assocIndexLine;

        public IndexLineSpec(String lineName, String grpName, String shortName) {
            this.lineName = lineName;
            this.grpName = grpName;
            this.shortName = shortName;
            enabled = true;
            assocIndexLine = null;
            assocLabel = null;
        }

        @Override
        public String toString() {
            return shortName;
        }

        @Override
        public int compareTo(IndexLineSpec o) {
            return shortName.compareTo(o.shortName);
        }
    }

    public void setMergeTime(long mt) {
        this.mergeTime = mt;
    }
}
