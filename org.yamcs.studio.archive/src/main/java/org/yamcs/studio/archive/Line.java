package org.yamcs.studio.archive;

import org.eclipse.swt.graphics.Rectangle;

public abstract class Line extends Drawable {

    Rectangle coords;
    private boolean frozen;

    public Line(Timeline timeline) {
        super(timeline);
    }

    public abstract int getHeight();

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }
}
