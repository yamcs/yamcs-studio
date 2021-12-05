/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.widgets.editparts.TextDirectEditPolicy;

/**
 * The model for widgets have text property, so the widget can be directly edited by installing
 * {@link TextDirectEditPolicy}.
 */
public interface ITextModel {

    public void setText(String text);

    public String getText();

}
