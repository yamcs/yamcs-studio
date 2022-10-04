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

import static org.csstudio.opibuilder.widgets.model.WebBrowserModel.PROP_URL;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.widgets.figures.AbstractWebBrowserFigure;
import org.csstudio.opibuilder.widgets.figures.WebBrowserFigure;
import org.csstudio.opibuilder.widgets.model.WebBrowserModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.browser.Browser;

/**
 * The editpart of web browser widget.
 */
public final class WebBrowserEditPart extends AbstractBaseEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        AbstractWebBrowserFigure<?> figure = new WebBrowserFigure(this, model.isShowToolBar());
        figure.setUrl(model.getURL());
        return figure;
    }

    @Override
    public WebBrowserModel getWidgetModel() {
        return (WebBrowserModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_URL, (oldValue, newValue, refreshableFigure) -> {
            ((AbstractWebBrowserFigure<?>) refreshableFigure).setUrl((String) newValue);
            return false;
        });
    }

    public Browser getBrowser() {
        return ((AbstractWebBrowserFigure<?>) getFigure()).getBrowser();
    }
}
