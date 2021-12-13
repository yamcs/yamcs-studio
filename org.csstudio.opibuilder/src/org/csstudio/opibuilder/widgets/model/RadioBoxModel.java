/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.eclipse.swt.graphics.RGB;

/**
 * Model of Radio Box.
 */
public class RadioBoxModel extends AbstractChoiceModel {

    public final String ID = "org.csstudio.opibuilder.widgets.radioBox";

    public RadioBoxModel() {
        setPropertyValue(PROP_COLOR_BACKGROUND, new RGB(230, 230, 230));
        setPropertyValue(PROP_SELECTED_COLOR, new RGB(77, 77, 77));
    }

    @Override
    public String getTypeID() {
        return ID;
    }
}
