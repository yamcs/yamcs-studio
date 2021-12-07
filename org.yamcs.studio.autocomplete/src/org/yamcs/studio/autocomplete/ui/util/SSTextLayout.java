/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class SSTextLayout {

    private TextLayout textLayout;

    public void init(Display display, String text) {
        textLayout = new TextLayout(display);
        textLayout.setAlignment(SWT.CENTER);
        textLayout.setText(text);
    }

    public void addStyle(Font font, Color color, int x, int y) {
        var textStyle = new TextStyle(font, color, null);
        textLayout.setStyle(textStyle, x, y);
    }

    public void handlePaintItemEvent(Event event, int offsetX, int offsetY) {
        textLayout.draw(event.gc, event.x + offsetX, event.y + offsetY);
    }

    public void handleMeasureItemEvent(Event event) {
        var textLayoutBounds = textLayout.getBounds();
        event.width = textLayoutBounds.width;
        event.height = textLayoutBounds.height;
    }

    public Rectangle getBounds() {
        if (textLayout != null) {
            return textLayout.getBounds();
        }
        return null;
    }
}
