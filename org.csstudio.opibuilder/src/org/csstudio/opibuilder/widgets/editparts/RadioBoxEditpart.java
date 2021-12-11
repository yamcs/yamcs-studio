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
import org.csstudio.swt.widgets.figures.AbstractChoiceFigure;
import org.csstudio.swt.widgets.figures.RadioBoxFigure;

/**
 * Editpart of Radio Box widget.
 */
public class RadioBoxEditpart extends AbstractChoiceEditPart {

    @Override
    protected AbstractChoiceFigure createChoiceFigure() {
        return new RadioBoxFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
    }

}
