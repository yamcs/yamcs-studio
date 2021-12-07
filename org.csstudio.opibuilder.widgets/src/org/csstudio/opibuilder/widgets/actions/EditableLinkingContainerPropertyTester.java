/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.widgets.editparts.LinkingContainerEditpart;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class EditableLinkingContainerPropertyTester extends PropertyTester {

    /**
     * Establish if the receiver object is a LinkingContainer widget suitable for editing in OPIEditor.
     *
     * If:
     * <ul>
     * <li>the receiver is not a linking container
     * <li>no path can be extracted from the widget
     * </ul>
     * return false
     *
     */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IPath displayPath = null;
        IPath embeddedPath = null;
        var editable = false;
        if (property.equals("isEditable")) {
            if (receiver instanceof LinkingContainerEditpart) {
                var lc = (LinkingContainerEditpart) receiver;
                displayPath = lc.getWidgetModel().getDisplayModel().getOpiFilePath();
                embeddedPath = lc.getWidgetModel().getOPIFilePath();
            }
            editable = (displayPath instanceof Path && embeddedPath instanceof Path);
        }
        return editable;
    }
}
