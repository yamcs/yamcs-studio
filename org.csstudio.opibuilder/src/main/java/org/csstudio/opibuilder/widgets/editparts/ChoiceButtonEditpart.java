/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.model.ChoiceButtonModel;
import org.csstudio.swt.widgets.figures.AbstractChoiceFigure;
import org.csstudio.swt.widgets.figures.ChoiceButtonFigure;

/**
 * The editpart of choice button widget.
 */
public class ChoiceButtonEditpart extends AbstractChoiceEditPart {

    @Override
    protected AbstractChoiceFigure createChoiceFigure() {
        var figure = new ChoiceButtonFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
        return figure;
    }

    @Override
    public ChoiceButtonModel getWidgetModel() {
        return (ChoiceButtonModel) getModel();
    }
}
