/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.properties;

import org.eclipse.draw2d.IFigure;

/**
 * The handler to execute corresponding changes when property value changed. For example, refresh the widget figure when
 * color property changed.
 */
public interface IWidgetPropertyChangeHandler {
    /**
     * Handle the change of an widget property by applying graphical operations to the given figure.
     *
     * @param oldValue
     *            The old property value.
     * @param newValue
     *            The new property value.
     * @param figure
     *            The figure to apply graphical operations to.
     * @return Not used. Reserved for future use.
     */
    boolean handleChange(Object oldValue, Object newValue, IFigure figure);
}
