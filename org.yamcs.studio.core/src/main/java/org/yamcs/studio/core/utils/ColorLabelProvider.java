package org.yamcs.studio.core.utils;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

public abstract class ColorLabelProvider extends OwnerDrawLabelProvider {

    @Override
    protected void measure(Event event, Object element) {
    }

    @Override
    protected void paint(Event event, Object element) {
        var bounds = ((TableItem) event.item).getBounds(event.index);
        var xoffset = Math.max((bounds.width - 40) / 2, 0);
        event.gc.setBackground(getColor(element));
        event.gc.fillRectangle(bounds.x + xoffset + 2, bounds.y + 2, 40, bounds.height - (2 * 2));
        event.gc.setForeground(getBorderColor(element));
        event.gc.drawRectangle(bounds.x + xoffset + 2, bounds.y + 2, 40, bounds.height - (2 * 2));
    }

    @Override
    protected void erase(Event event, Object element) {
        // NOP. Prevent background-recoloring upon selection
    }

    public abstract Color getColor(Object element);

    public abstract Color getBorderColor(Object element);
}
