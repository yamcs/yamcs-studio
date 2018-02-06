package org.yamcs.studio.archive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.eclipse.swt.graphics.RGB;
import org.yamcs.protobuf.Yamcs.ArchiveTag;

public class TagTimeline extends JPanel implements MouseInputListener {
    private static final long serialVersionUID = 1L;
    private final TagBox tagBox;
    List<ArchiveTag> tags;
    ZoomSpec zoom;
    int leftDelta;
    BufferedImage image = null;
    int row;
    Font f;

    TagTimeline(TagBox tagBox, List<ArchiveTag> tags, ZoomSpec zoom, int row, int leftDelta) {
        super();
        this.tagBox = tagBox;
        this.zoom = zoom;
        this.tags = tags;
        this.row = row;
        this.leftDelta = leftDelta;
        JLabel l = new JLabel("X");
        f = deriveFont(l.getFont());
        l.setFont(f);
        setMinimumSize(new Dimension(0, 2 + l.getPreferredSize().height));
        setPreferredSize(getMinimumSize());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 2 + l.getPreferredSize().height));
        addMouseListener(this);
        setOpaque(false);
    }

    private static Font deriveFont(Font f) {
        return f.deriveFont(Font.PLAIN, f.getSize2D() - 2);
    }

    private MouseEvent translateEvent(MouseEvent e, Component dest) {
        return SwingUtilities.convertMouseEvent(e.getComponent(), e, dest);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        long t = zoom.convertPixelToInstant(e.getX());
        int index = time2Tag(tags, t);
        tagBox.doMousePressed(translateEvent(e, tagBox), row, index);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        getParent().dispatchEvent(translateEvent(e, getParent()));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    static int time2Tag(List<ArchiveTag> tagList, long t) {
        int min = 0;
        int max = tagList.size() - 1;
        int mid;
        while (min <= max) {
            mid = (min + max) >> 1;
            ArchiveTag atmid = tagList.get(mid);
            if (!atmid.hasStart() || atmid.getStart() <= t) {
                if (!atmid.hasStop() || atmid.getStop() >= t) {
                    return mid;
                } else {
                    min = mid + 1;
                }
            } else {
                max = mid - 1;
            }
        }
        return -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D big = image.createGraphics();

            big.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            big.fillRect(0, 0, getWidth(), getHeight());

            big.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            for (ArchiveTag at : tags) {
                // TODO store these values in a map, rather than calculating all the time
                RGB rgb = toRGB(at);
                Color bgcolor = new Color(rgb.red, rgb.green, rgb.blue);

                int brightness = (int) Math.sqrt(.241 * rgb.red * rgb.red + .691 * rgb.green * rgb.green + .068 * rgb.blue * rgb.blue);
                Color fgcolor = (brightness < 130) ? Color.WHITE : Color.BLACK;

                big.setColor(bgcolor);
                long start = (at.hasStart()) ? at.getStart() : zoom.startInstant;
                int x1 = zoom.convertInstantToPixel(start);

                long stop = (at.hasStop()) ? at.getStop() : zoom.stopInstant;
                int x2 = zoom.convertInstantToPixel(stop);
                if (x1 <= 0 && x2 < 0)
                    continue;

                if (x1 < 0)
                    x1 = 0;

                int width = (x2 - x1 <= 1) ? 1 : x2 - x1 - 1;
                big.fillRect(x1 - leftDelta, 0, width, getHeight());
                big.setColor(fgcolor);
                big.setFont(f);
                Rectangle2D bounds = f.getStringBounds(at.getName(), big.getFontRenderContext());
                if (width > bounds.getWidth()) {
                    LineMetrics lm = f.getLineMetrics(at.getName(), big.getFontRenderContext());
                    big.drawString(at.getName(), x1 - leftDelta + 1, (int) lm.getAscent() + 1);
                }
                big.setColor(Color.DARK_GRAY);
                big.drawRect(x1 - leftDelta, 0, width - 1, getHeight() - 1);
            }
            //  border.paintBorder(this, big, 0, 0, getWidth(),getHeight() );
        }

        g.drawImage(image, 0, 0, this);

    }

    /**
     * These are the only colors encoded by old versions of the swing clients. They are encoded as
     * textual color names rather than hex values.
     * <p>
     * We keep this in place for a good while, because there are still archives around that use
     * these color values.
     */
    private static enum SwingTagColor {
        BLACK(Color.BLACK),
        BLUE(Color.BLUE),
        CYAN(Color.CYAN),
        GRAY(Color.GRAY),
        GREEN(Color.GREEN),
        MAGENTA(Color.MAGENTA),
        ORANGE(Color.ORANGE),
        PINK(Color.PINK),
        RED(Color.RED),
        YELLOW(Color.YELLOW);

        private Color awtColor;

        private SwingTagColor(Color awtColor) {
            this.awtColor = awtColor;
        }
    }

    /**
     * Returns the RGB value for a specific tag. Compatible with the Swing archive-browser which
     * does not support the full RGB spectrum.
     * <p>
     * Defaults to white if no color is specified, or the color could not be interpreted
     */
    public static RGB toRGB(ArchiveTag tag) {
        if (!tag.hasColor())
            return new RGB(0xff, 0xff, 0xff);
        if (tag.getColor().startsWith("#")) {
            int r = Integer.valueOf(tag.getColor().substring(1, 3), 16);
            int g = Integer.valueOf(tag.getColor().substring(3, 5), 16);
            int b = Integer.valueOf(tag.getColor().substring(5, 7), 16);
            return new RGB(r, g, b);
        } else {
            SwingTagColor swingColor = SwingTagColor.valueOf(tag.getColor().toUpperCase());
            if (swingColor != null) {
                java.awt.Color awtColor = swingColor.awtColor;
                return new RGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
            } else {
                return new RGB(0xff, 0xff, 0xff);
            }
        }
    }
}
