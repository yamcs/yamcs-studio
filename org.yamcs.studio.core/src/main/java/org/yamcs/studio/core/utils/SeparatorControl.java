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
 * A custom separator. Eclipse don't seem to work in 4.3 when using menu contributions in the plugin.xml.
 *
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=413213
 */
public class SeparatorControl extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        var bla = new Label(parent, SWT.VERTICAL | SWT.SEPARATOR);
        bla.setText("   ");
        return bla;
    }
}
