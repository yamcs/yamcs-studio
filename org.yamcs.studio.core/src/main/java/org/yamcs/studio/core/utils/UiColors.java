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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class UiColors {

    static Display display = Display.getCurrent();

    // Red-ish
    public static final Color ERROR_FAINT_BG = new Color(display, new RGB(255, 221, 221));
    public static final Color ERROR_FAINT_FG = display.getSystemColor(SWT.COLOR_RED);

    // Yellow-ish
    public static final Color WARNING_FAINT_BG = new Color(display, new RGB(248, 238, 199));
    public static final Color WARNING_FAINT_FG = display.getSystemColor(SWT.COLOR_BLACK);

    // Grey-ish
    public static final Color DISABLED_FAINT_BG = new Color(display, new RGB(216, 216, 216));
    public static final Color DISABLED_FAINT_FG = display.getSystemColor(SWT.COLOR_BLACK);

    // Green-ish
    public static final Color GOOD_FAINT_BG = new Color(display, new RGB(221, 255, 221));
    public static final Color GOOD_FAINT_FG = display.getSystemColor(SWT.COLOR_BLACK);

    public static final Color GOOD_BRIGHT_BG = display.getSystemColor(SWT.COLOR_GREEN);
    public static final Color GOOD_BRIGHT_FG = display.getSystemColor(SWT.COLOR_BLACK);

    // Other
    public static final Color BORDER_COLOR = new Color(display, new RGB(216, 216, 216));
}
