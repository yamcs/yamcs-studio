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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Control;

/**
 * Figure for a web browser widget.
 */
public abstract class AbstractWebBrowserFigure<T extends Control> extends AbstractSWTWidgetFigure<T> {

    public AbstractWebBrowserFigure(AbstractBaseEditPart editpart, int style) {
        super(editpart, style);
    }

    public abstract void setUrl(String url);

    public abstract Browser getBrowser();
}
