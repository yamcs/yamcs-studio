/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.figures;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.swt.widgets.natives.SpreadSheetTable;
import org.eclipse.swt.widgets.Composite;

/**
 * A figure that holds a {@link SpreadSheetTable}.
 */
public class SpreadSheetTableFigure extends AbstractSWTWidgetFigure<SpreadSheetTable> {

    public SpreadSheetTableFigure(AbstractBaseEditPart editpart) {
        super(editpart);
    }

    @Override
    protected SpreadSheetTable createSWTWidget(Composite parent, int style) {
        return new SpreadSheetTable(parent);
    }
}
