package org.yamcs.studio.archive;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public interface Scale {

    int getPreferredUnitWidth();

    int measureUnitWidth();

    void drawContent(GC gc, Rectangle coords);
}
