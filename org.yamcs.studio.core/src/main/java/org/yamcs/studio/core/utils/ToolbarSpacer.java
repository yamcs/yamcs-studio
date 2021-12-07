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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * A custom spacer. Eclipse separators look a bit crappy, and don't even seem to work in 4.3
 * 
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=413213
 */
public class ToolbarSpacer extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        var bla = new Label(parent, SWT.NONE);
        bla.setText("   ");
        return bla;
    }
}
