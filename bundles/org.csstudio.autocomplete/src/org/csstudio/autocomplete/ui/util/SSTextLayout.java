/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.autocomplete.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * @author Fred Arnaud (Sopra Group) - ITER
 */
public class SSTextLayout {

    private TextLayout textLayout;

    public void init(Display display, String text) {
        textLayout = new TextLayout(display);
        textLayout.setAlignment(SWT.CENTER);
        textLayout.setText(text);
    }

    public void addStyle(Font font, Color color, int x, int y) {
        TextStyle textStyle = new TextStyle(font, color, null);
        textLayout.setStyle(textStyle, x, y);
    }

    public void handlePaintItemEvent(Event event, int offsetX, int offsetY) {
        textLayout.draw(event.gc, event.x + offsetX, event.y + offsetY);
    }

    public void handleMeasureItemEvent(Event event) {
        Rectangle textLayoutBounds = textLayout.getBounds();
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
