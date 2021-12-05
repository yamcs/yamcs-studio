/********************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figures;

import org.eclipse.draw2d.IFigure;

/**
 * An interface that for figures that provide text.
 */
public interface ITextFigure extends IFigure {
    public String getText();
}
