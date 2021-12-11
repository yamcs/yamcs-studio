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
 * Model of Choice Button.
 */
public class ChoiceButtonModel extends AbstractChoiceModel {

    public final String ID = "org.csstudio.opibuilder.widgets.choiceButton";

    public ChoiceButtonModel() {
        setBackgroundColor(new RGB(240, 240, 240));
    }

    @Override
    public String getTypeID() {
        return ID;
    }

}
