/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.editparts;

import org.eclipse.draw2d.IFigure;

/**
 * The delegate interface that describes the common functions of Native Text and draw2d Text Input.
 */
public interface ITextInputEditPartDelegate {

    IFigure doCreateFigure();

    void registerPropertyChangeHandlers();

    void updatePropSheet();

    void createEditPolicies();
}
