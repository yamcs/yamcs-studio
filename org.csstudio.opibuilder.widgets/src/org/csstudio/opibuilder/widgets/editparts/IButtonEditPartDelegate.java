/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.eclipse.draw2d.IFigure;

public interface IButtonEditPartDelegate {

    public abstract IFigure doCreateFigure();

    public abstract void hookMouseClickAction();

    public abstract void deactivate();

    public abstract void registerPropertyChangeHandlers();

    public abstract void setValue(Object value);

    public abstract Object getValue();

    public abstract boolean isSelected();

}
