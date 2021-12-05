/********************************************************************************
 * Copyright (c) 2008 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractWidgetEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.opibuilder.widgets.figures.ImageFigure;
import org.csstudio.opibuilder.widgets.model.ImageModel;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.csstudio.swt.widgets.symbol.util.PermutationMatrix;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
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
        ImageModel model = getWidgetModel();
        // create AND initialize the view properly
        final ImageFigure figure = new ImageFigure();

        // Resize when new image is loaded
        figure.setImageLoadedListener(figure1 -> {
            ImageFigure imageFigure = (ImageFigure) figure1;
            autoSizeWidget(imageFigure);
        });

        // Image default parameters
        SymbolImageProperties sip = new SymbolImageProperties();
        sip.setTopCrop(model.getTopCrop());
        sip.setBottomCrop(model.getBottomCrop());
        sip.setLeftCrop(model.getLeftCrop());
        sip.setRightCrop(model.getRightCrop());
        sip.setStretch(model.getStretch());
        sip.setAutoSize(model.isAutoSize());
        sip.setMatrix(model.getPermutationMatrix());
        sip.setAlignedToNearestSecond(model.isAlignedToNearestSecond());
        sip.setBackgroundColor(new Color(Display.getDefault(), model.getBackgroundColor()));
        sip.setAnimationDisabled(model.isStopAnimation());
        figure.setSymbolProperties(sip, model);

        figure.setFilePath(model.getFilename());
        return figure;
    }

    /**
     * Register change handlers for the four crop properties.
     */
    protected void registerCropPropertyHandlers() {
        // top
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setTopCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_TOPCROP, handle);

        // bottom
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setBottomCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_BOTTOMCROP, handle);

        // left
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setLeftCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_LEFTCROP, handle);

        // right
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setRightCrop((Integer) newValue);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_RIGHTCROP, handle);
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        // changes to the filename property
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            String absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                IPath path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }
            imageFigure.setFilePath(absolutePath);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_IMAGE_FILE, handle);

        // changes to the stretch property
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setStretch((Boolean) newValue);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_STRETCH, handle);

        // changes to the autosize property
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setAutoSize((Boolean) newValue);
            ImageModel model = (ImageModel) getModel();
            Dimension d = imageFigure.getAutoSizedDimension();
            if ((Boolean) newValue && !model.getStretch() && d != null) {
                model.setSize(d.width, d.height);
            }
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_AUTOSIZE, handle);

        // changes to the stop animation property
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setAnimationDisabled((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_NO_ANIMATION, handle);

        // changes to the align to nearest second property
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.setAlignedToNearestSecond((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_ALIGN_TO_NEAREST_SECOND, handle);

        // changes to the border width property
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.resizeImage();
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(ImageModel.PROP_BORDER_STYLE, handle);

        // size change handlers - so we can stretch accordingly
        handle = (oldValue, newValue, figure) -> {
            ImageFigure imageFigure = (ImageFigure) figure;
            imageFigure.resizeImage();
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_HEIGHT, handle);
        setPropertyChangeHandler(ImageModel.PROP_WIDTH, handle);

        FigureTransparencyHelper.addHandler(this, figure);

        registerCropPropertyHandlers();
        registerImageRotationPropertyHandlers();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ImageFigure) getFigure()).dispose();
    }

    private void autoSizeWidget(final ImageFigure imageFigure) {
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
                ImageModel model = (ImageModel) getModel();
                imageFigure.setAutoSize(model.isAutoSize());
                Dimension d = imageFigure.getAutoSizedDimension();
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
            ImageFigure imageFigure = (ImageFigure) figure;
            int newDegree = getWidgetModel().getDegree((Integer) newValue);
            int oldDegree = getWidgetModel().getDegree((Integer) oldValue);

            PermutationMatrix oldMatrix = new PermutationMatrix(
                    (double[][]) getPropertyValue(ImageModel.PERMUTATION_MATRIX));
            PermutationMatrix newMatrix = PermutationMatrix.generateRotationMatrix(newDegree - oldDegree);
            PermutationMatrix result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(ImageModel.PROP_DEGREE, (Integer) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);

            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_DEGREE, handler);

        // flip horizontal rotation property
        handler = (oldValue, newValue, figure) -> {
            if (oldValue == null || newValue == null) {
                return false;
            }
            ImageFigure imageFigure = (ImageFigure) figure;
            // imageFigure.setFlipH((Boolean) newValue);
            PermutationMatrix newMatrix = PermutationMatrix.generateFlipHMatrix();
            PermutationMatrix oldMatrix = imageFigure.getPermutationMatrix();
            PermutationMatrix result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(ImageModel.PROP_FLIP_HORIZONTAL, (Boolean) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_FLIP_HORIZONTAL, handler);

        // flip vertical rotation property
        handler = (oldValue, newValue, figure) -> {
            if (oldValue == null || newValue == null) {
                return false;
            }
            ImageFigure imageFigure = (ImageFigure) figure;
            // imageFigure.setFlipV((Boolean) newValue);
            PermutationMatrix newMatrix = PermutationMatrix.generateFlipVMatrix();
            PermutationMatrix oldMatrix = imageFigure.getPermutationMatrix();
            PermutationMatrix result = newMatrix.multiply(oldMatrix);

            // As we use only % Pi/2 angles, we can round to integer values
            // => equals work better
            result.roundToIntegers();

            setPropertyValue(ImageModel.PERMUTATION_MATRIX, result.getMatrix());
            setPropertyValue(ImageModel.PROP_FLIP_VERTICAL, (Boolean) newValue);
            imageFigure.setPermutationMatrix(result);
            autoSizeWidget(imageFigure);
            return false;
        };
        setPropertyChangeHandler(ImageModel.PROP_FLIP_VERTICAL, handler);
    }
}
