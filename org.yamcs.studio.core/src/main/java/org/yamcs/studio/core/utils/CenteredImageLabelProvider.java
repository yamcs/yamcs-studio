/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A table column with SWT.CENTER and just an image, will not be centered on at least Windows platforms. Work around
 * this issue by custom drawing.
 * 
 * <p>
 * FDI's note: reconsider implementation. It causes the default selection color to be limited to the width of the actual
 * columns, rather than the full table width.
 *
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=26045
 */
public abstract class CenteredImageLabelProvider extends OwnerDrawLabelProvider {

    @Override
    public void measure(Event event, Object element) {
    }

    @Override
    public void paint(Event event, Object element) {
        var img = getImage(element);
        if (img != null) {

            Rectangle bounds;
            if (event.item instanceof TreeItem) {
                bounds = ((TreeItem) event.item).getBounds(event.index);
            } else {
                bounds = ((TableItem) event.item).getBounds(event.index);
            }
            var imgBounds = img.getBounds();

            bounds.width /= 2;
            bounds.width -= imgBounds.width / 2;
            bounds.height /= 2;
            bounds.height -= imgBounds.height / 2;

            var x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
            var y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
            event.gc.drawImage(img, x, y);
        }
    }

    @Override
    protected void erase(Event event, Object element) {
        // NOP. Prevent background-recoloring upon selection
    }

    public abstract Image getImage(Object element);
}
