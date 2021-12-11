/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.ButtonBorder.ButtonScheme;

/**
 * <code>CSSButtonBorder</code> provide a single source access to the real ButtonBorder, which for some reason is not
 * same for RAP and RCP. RAP implements access to borders as methods, RCP does it with with fields.
 */
public class CSSButtonBorder {
    public static class SCHEMES {
        public static ButtonScheme BUTTON_SCROLLBAR = ButtonBorder.SCHEMES.BUTTON_SCROLLBAR;
        public static ButtonScheme BUTTON_CONTRAST = ButtonBorder.SCHEMES.BUTTON_CONTRAST;
    }
}
