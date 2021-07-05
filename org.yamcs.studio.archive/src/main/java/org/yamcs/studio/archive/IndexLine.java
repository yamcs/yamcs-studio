package org.yamcs.studio.archive;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.yamcs.studio.archive.IndexBox.IndexLineSpec;

/**
 * Represents a horizontal TM line composed of a label and a timeline
 */
class IndexLine extends JPanel implements MouseInputListener {
    private final IndexBox indexBox;
    private static final long serialVersionUID = 1L;
    IndexLineSpec pkt;

    IndexLine(IndexBox tmBox, IndexLineSpec pkt) {
        super(null, false);
        this.indexBox = tmBox;
        this.pkt = pkt;
        pkt.assocIndexLine = this;
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder());
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private MouseEvent translateEvent(MouseEvent e) {
        // workaround for this bug
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7181403
        MouseEvent me = SwingUtilities.convertMouseEvent(e.getComponent(), e, indexBox);
        return new MouseEvent(me.getComponent(), me.getID(), me.getWhen(), me.getModifiers(), me.getX(), me.getY(), me.getXOnScreen(),
                me.getYOnScreen(), me.getClickCount(), me.isPopupTrigger(), e.getButton());
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        indexBox.dispatchEvent(translateEvent(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        MouseEvent transEvent = translateEvent(e);
        indexBox.dataView.setPointer(transEvent);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
}
