/********************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * RCP 'View' for debugging a Perspective
 */
public class PlaceHolderView extends ViewPart {
    /** View ID registered in plugin.xml */
    public static final String ID = "org.csstudio.opibuilder.placeHolder";

    @Override
    public void createPartControl(Composite parent) {
        var site = getViewSite();
        parent.setLayout(new FillLayout());

        var text = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setText("Placeholder for displays that should appear in this location.\n"
                + "Close after all displays have been arranged.");
        setPartName(site.getSecondaryId());
    }

    @Override
    public void setFocus() {
        // NOP
    }
}
