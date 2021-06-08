/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.AbstractWebBrowserFigure;
import org.csstudio.opibuilder.widgets.figures.WebBrowserFigure;
import org.csstudio.opibuilder.widgets.model.WebBrowserModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.browser.Browser;

/**
 * The editpart of web browser widget.
 *
 * @author Xihui Chen
 *
 */
public final class WebBrowserEditPart extends AbstractBaseEditPart {

    @Override
    protected IFigure doCreateFigure() {
        WebBrowserModel model = getWidgetModel();
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

        // URL
        IWidgetPropertyChangeHandler urlHandler = (oldValue, newValue, refreshableFigure) -> {
            ((AbstractWebBrowserFigure<?>) refreshableFigure).setUrl((String) newValue);
            return false;
        };
        setPropertyChangeHandler(WebBrowserModel.PROP_URL, urlHandler);
    }

    public Browser getBrowser() {
        return ((AbstractWebBrowserFigure<?>) getFigure()).getBrowser();
    }
}
