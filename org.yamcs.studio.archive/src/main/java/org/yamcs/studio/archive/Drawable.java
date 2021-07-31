package org.yamcs.studio.archive;

import org.eclipse.swt.graphics.GC;

public abstract class Drawable {

    protected Timeline timeline;

    public Drawable(Timeline timeline) {
        this.timeline = timeline;
        timeline.add(this);
    }

    protected void reportMutation() {
        this.timeline.requestRepaint();
    }

    void beforeDraw(GC gc) {
    }

    void drawUnderlay(GC gc) {
    }

    void drawContent(GC gc) {
    }

    void drawOverlay(GC gc) {
    }
}
