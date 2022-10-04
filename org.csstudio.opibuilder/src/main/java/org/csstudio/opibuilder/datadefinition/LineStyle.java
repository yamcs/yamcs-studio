/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.datadefinition;

import org.eclipse.swt.SWT;

/**
 * The line sytle: 0: Solid, 1: Dash, 2: Dot, 3: DashDot, 4: DashDotDot.
 */
public enum LineStyle {
    SOLID("Solid", SWT.LINE_SOLID),
    DASH("Dash", SWT.LINE_DASH),
    DOT("Dot", SWT.LINE_DOT),
    DASH_DOT("DashDot", SWT.LINE_DASHDOT),
    Dash_DOTDOT("DashDotDot", SWT.LINE_DASHDOTDOT);

    String description;
    int style;

    LineStyle(String description, int style) {
        this.description = description;
        this.style = style;
    }

    /**
     * @return SWT line style {SWT.LINE_SOLID, SWT.LINE_DASH, SWT.LINE_DOT, SWT.LINE_DASHDOT, SWT.LINE_DASHDOTDOT }
     */
    public int getStyle() {
        return style;
    }

    @Override
    public String toString() {
        return description;
    }

    public static String[] stringValues() {
        var sv = new String[values().length];
        var i = 0;
        for (var p : values()) {
            sv[i++] = p.toString();
        }
        return sv;
    }
}
