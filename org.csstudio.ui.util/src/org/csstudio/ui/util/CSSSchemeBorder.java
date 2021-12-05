/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.SchemeBorder.Scheme;

/**
 * <code>CSSSchemeBorder</code> provide a single source access to the real SchemeBorder, which for some reason is not
 * same for RAP and RCP. RAP implements access to borders as methods, RCP does it with with fields.
 */
public class CSSSchemeBorder {
    public static class SCHEMES {
        public static Scheme LOWERED = SchemeBorder.SCHEMES.LOWERED;
        public static Scheme RAISED = SchemeBorder.SCHEMES.RAISED;
        public static Scheme ETCHED = SchemeBorder.SCHEMES.ETCHED;
        public static Scheme RIDGED = SchemeBorder.SCHEMES.RIDGED;
        public static Scheme BUTTON_CONTRAST = SchemeBorder.SCHEMES.BUTTON_CONTRAST;
        public static Scheme BUTTON_PRESSED = SchemeBorder.SCHEMES.BUTTON_PRESSED;
        public static Scheme BUTTON_RAISED = SchemeBorder.SCHEMES.BUTTON_RAISED;
    }
}
