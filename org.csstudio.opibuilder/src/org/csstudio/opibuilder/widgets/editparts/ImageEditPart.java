/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
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
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_ALIGN_TO_NEAREST_SECOND;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_AUTOSIZE;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_BOTTOMCROP;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_DEGREE;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_FLIP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_FLIP_VERTICAL;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_IMAGE_FILE;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_LEFTCROP;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_NO_ANIMATION;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_RIGHTCROP;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_STRETCH;
import static org.csstudio.opibuilder.widgets.model.ImageModel.PROP_TOPCROP;

import org.csstudio.opibuilder.editparts.AbstractWidgetEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.opibuilder.widgets.figures.ImageFigure;
import org.csstudio.opibuilder.widgets.model.ImageModel;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.csstudio.swt.widgets.symbol.util.PermutationMatrix;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * EditPart controller for the image widget.
 */
public final class ImageEditPart extends AbstractWidgetEditPart {

    private int maxAttempts;

    @Override
    public ImageModel getWidgetModel() {
        return (ImageModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        // create AND initialize the view properly
        var figure = new ImageFigure();

        // Resize when new image is loaded
        figure.setImageLoadedListener(figure1 -> {
            var imageFigure = (ImageFigure) figure1;
            autoSizeWidget(imageFigure);
        });

        // Image default parameters
        var props = new SymbolImageProperties();
        props.setTopCrop(model.getTopCrop());
        props.setBottomCrop(model.getBottomCrop());
        props.setLeftCrop(model.getLeftCrop());
        props.setRightCrop(model.getRightCrop());
        props.setStretch(model.getStretch());
        props.setAutoSize(model.isAutoSize());
        props.setMatrix(model.getPermutationMatrix());
        props.setAlignedToNearestSecond(model.isAlignedToNearestSecond());
        props.setBackgroundColor(new Color(Display.getDefault(), model.getBackgroundColor()));
        props.setAnimationDisabled(model.isStopAnimation());
        figure.setSymbolProperties(props, model);

        figure.setFilePath(model.getFilename());
        return figure;
    }

    /**
     * Register change handlers for the four crop properties.
     */
    protected void registerCropPropertyHandlers() {
        setPropertyChangeHandler(PROP_TOPCROP, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setTopCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        });

        setPropertyChangeHandler(PROP_BOTTOMCROP, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setBottomCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        });

        setPropertyChangeHandler(PROP_LEFTCROP, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setLeftCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        });

        setPropertyChangeHandler(PROP_RIGHTCROP, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setRightCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        });
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_IMAGE_FILE, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            var absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                var path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }
            imageFigure.setFilePath(absolutePath);
            autoSizeWidget(imageFigure);
            return false;
        });

        setPropertyChangeHandler(PROP_STRETCH, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setStretch((Boolean) newValue);
            autoSizeWidget(imageFigure);
            return false;
        });

        setPropertyChangeHandler(PROP_AUTOSIZE, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setAutoSize((Boolean) newValue);
            var model = (ImageModel) getModel();
            var d = imageFigure.getAutoSizedDimension();
            if ((Boolean) newValue && !model.getStretch() && d != null) {
                model.setSize(d.width, d.height);
            }
            return false;
        });

        setPropertyChangeHandler(PROP_NO_ANIMATION, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setAnimationDisabled((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_ALIGN_TO_NEAREST_SECOND, (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.setAlignedToNearestSecond((Boolean) newValue);
            return false;
        });

        // changes to the border width property
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.resizeImage();
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handle);

        // size change handlers - so we can stretch accordingly
        handle = (oldValue, newValue, figure) -> {
            var imageFigure = (ImageFigure) figure;
            imageFigure.resizeImage();
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(PROP_HEIGHT, handle);
        setPropertyChangeHandler(PROP_WIDTH, handle);

        FigureTransparencyHelper.addHandler(this, figure);

        registerCropPropertyHandlers();
        registerImageRotationPropertyHandlers();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ImageFigure) getFigure()).dispose();
    }

    private void autoSizeWidget(ImageFigure imageFigure) {
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
                var model = (ImageModel) getModel();
                imageFigure.setAutoSize(model.isAutoSize());
                var d = imageFigure.getAutoSizedDimension();
                if (model.isAutoSize() && !model.getStretch() && d != null) {
                    model.setSize(d.width, d.height);
                }

            }
        };
        Display.getDefault().timerExec(100, task);
    }

    /**
     * Registers image rotation property change handlers for the properties defined in {@link MonitorBoolSymbolModel}.
     */
    public void registerImageRotationPropertyHandlers() {
        // degree rotation property
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            if (oldValue == null || newValue == null) {
                return false;
            }
            var imageFigure = (ImageFigure) figure;
            var newDegree = getWidgetModel().getDegree((Integer) newValue);
            var oldDegree = getWidgetModel().getDegree((Integer) oldValue);

            var oldMatrix = new PermutationMatrix((double[][]) getPropertyValue(ImageModel.PERMUTATION_MATRIX));
            var newMatrix = PermutationMatrix.generateRotationMatrix(newDegree - oldDegree);
            var result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(PROP_DEGREE, (Integer) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);

            return false;
        };
        setPropertyChangeHandler(PROP_DEGREE, handler);

        // flip horizontal rotation property
        handler = (oldValue, newValue, figure) -> {
            if (oldValue == null || newValue == null) {
                return false;
            }
            var imageFigure = (ImageFigure) figure;
            // imageFigure.setFlipH((Boolean) newValue);
            var newMatrix = PermutationMatrix.generateFlipHMatrix();
            var oldMatrix = imageFigure.getPermutationMatrix();
            var result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(PROP_FLIP_HORIZONTAL, (Boolean) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(PROP_FLIP_HORIZONTAL, handler);

        // flip vertical rotation property
        handler = (oldValue, newValue, figure) -> {
            if (oldValue == null || newValue == null) {
                return false;
            }
            var imageFigure = (ImageFigure) figure;
            // imageFigure.setFlipV((Boolean) newValue);
            var newMatrix = PermutationMatrix.generateFlipVMatrix();
            var oldMatrix = imageFigure.getPermutationMatrix();
            var result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(PROP_FLIP_VERTICAL, (Boolean) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(PROP_FLIP_VERTICAL, handler);
    }
}
