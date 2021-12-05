/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.util;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SSStyledText {

    private StyledText text;

    public Control init(Composite parent, int style, Object layoutData) {
        text = new StyledText(parent, style);
        text.setLayoutData(layoutData);
        return text;
    }

    public void setText(String content) {
        text.setText(content);
        text.pack();
        // need right margin to avoid bold text to overflow
        text.setSize(text.getSize().x + 20, text.getSize().y);
    }

    public void setStyle(Color color, int fontStyle, int start, int length) {
        StyleRange styleRange = new StyleRange();
        styleRange.fontStyle = fontStyle;
        styleRange.foreground = color;
        styleRange.start = start;
        styleRange.length = length;
        text.setStyleRange(styleRange);
    }

    public Point getSize() {
        return text.getSize();
    }

    public void dispose() {
    }

    public boolean isValid() {
        return text != null && !text.isDisposed();
    }

    public boolean hasFocus() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getShell().isFocusControl() || text.isFocusControl();
    }
}
