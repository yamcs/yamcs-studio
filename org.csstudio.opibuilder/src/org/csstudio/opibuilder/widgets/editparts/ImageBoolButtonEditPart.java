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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_ALIGN_TO_NEAREST_SECOND;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_AUTOSIZE;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_NO_ANIMATION;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_OFF_IMAGE;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_ON_IMAGE;
import static org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel.PROP_STRETCH;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.opibuilder.widgets.figures.ImageBoolButtonFigure;
import org.csstudio.opibuilder.widgets.model.ImageBoolButtonModel;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * EditPart controller for the image widget.
 */
public final class ImageBoolButtonEditPart extends AbstractBoolControlEditPart {

    private int maxAttempts;

    @Override
    public ImageBoolButtonModel getWidgetModel() {
        return (ImageBoolButtonModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        // create AND initialize the view properly
        var figure = new ImageBoolButtonFigure();
        initializeCommonFigureProperties(figure, model);

        var props = new SymbolImageProperties();
        props.setStretch(model.isStretch());
        props.setAutoSize(model.isAutoSize());
        props.setAnimationDisabled(model.isStopAnimation());
        props.setAlignedToNearestSecond(model.isAlignedToNearestSecond());
        props.setBackgroundColor(new Color(Display.getDefault(), model.getBackgroundColor()));
        figure.setSymbolProperties(props, model);
        figure.setImageLoadedListener(figure1 -> {
            var symbolFigure = (ImageBoolButtonFigure) figure1;
            autoSizeWidget(symbolFigure);
        });
        figure.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                autoSizeWidget(figure);
            }
        });
        figure.setOnImagePath(model.getOnImagePath());
        figure.setOffImagePath(model.getOffImagePath());
        return figure;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        // Save CPU usage
        // removeAllPropertyChangeHandlers(AbstractPVWidgetModel.PROP_PVVALUE);
        // value
        // IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
        // public boolean handleChange(Object oldValue,
        // final Object newValue,
        // final IFigure refreshableFigure) {
        // if(newValue == null)
        // return false;
        // ImageBoolButtonFigure figure = (ImageBoolButtonFigure) refreshableFigure;
        // //figure.setValue(ValueUtil.getDouble((IValue)newValue));
        // autoSizeWidget(figure);
        // return true;
        // }
        // };
        // setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);

        setPropertyChangeHandler(PROP_ON_IMAGE, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            var absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                var path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }
            imageFigure.setOnImagePath(absolutePath);
            autoSizeWidget(imageFigure);
            return true;
        });

        setPropertyChangeHandler(PROP_OFF_IMAGE, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            var absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                var path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }
            imageFigure.setOffImagePath(absolutePath);
            autoSizeWidget(imageFigure);
            return true;
        });

        setPropertyChangeHandler(PROP_STRETCH, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setStretch((Boolean) newValue);
            autoSizeWidget(imageFigure);
            return true;
        });

        FigureTransparencyHelper.addHandler(this, figure);

        setPropertyChangeHandler(PROP_AUTOSIZE, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        });

        setPropertyChangeHandler(PROP_NO_ANIMATION, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setAnimationDisabled((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_ALIGN_TO_NEAREST_SECOND, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setAlignedToNearestSecond((Boolean) newValue);
            return false;
        });

        // changes to the border width property
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handle);

        // size change handlers - so we can stretch accordingly
        handle = (oldValue, newValue, figure) -> {
            var imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(PROP_HEIGHT, handle);
        setPropertyChangeHandler(PROP_WIDTH, handle);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ImageBoolButtonFigure) getFigure()).dispose();
    }

    private void autoSizeWidget(ImageBoolButtonFigure imageFigure) {
        if (!getWidgetModel().isAutoSize()) {
            return;
        }
        maxAttempts = 10;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (maxAttempts-- > 0 && imageFigure.isLoadingImage()) {
                    Display.getDefault().timerExec(100, this);
                    return;
                }
                var model = getWidgetModel();
                var d = imageFigure.getAutoSizedDimension();
                if (model.isAutoSize() && !model.isStretch() && d != null) {
                    model.setSize(d.width, d.height);
                }
            }
        };
        Display.getDefault().timerExec(100, task);
    }
}
