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

import java.io.IOException;
import java.io.InputStream;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.util.AbstractInputStreamRunnable;
import org.csstudio.swt.widgets.util.ResourceUtil;
import org.eclipse.draw2d.Cursors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Figure for a native button widget.
 */
public class NativeButtonFigure extends AbstractSWTWidgetFigure<Button> implements ITextFigure {

    private Button button;
    private Image image;

    public NativeButtonFigure(AbstractBaseEditPart editPart, int buttonStyle) {
        super(editPart, buttonStyle);
    }

    @Override
    protected Button createSWTWidget(Composite parent, int style) {
        button = new Button(parent, style);
        button.setCursor(Cursors.HAND);
        return button;
    }

    @Override
    public void setBackgroundColor(Color bg) {
    }

    public void setImagePath(String path) {
        if (image != null) {
            image.dispose();
            image = null;
            button.setImage(null);
        }
        AbstractInputStreamRunnable uiTask = new AbstractInputStreamRunnable() {
            @Override
            public void runWithInputStream(InputStream stream) {
                image = new Image(null, stream);
                try {
                    stream.close();
                } catch (IOException e) {
                }
                if (!button.isDisposed()) {
                    button.setImage(image);
                }
            }
        };
        if (path != null && !path.isEmpty()) {
            ResourceUtil.pathToInputStreamInJob(path, uiTask, "Load Button Icon...", exception -> {
                image = null;
                ErrorHandlerUtil.handleError("Failed to load button icon.", exception);
            });
        }
    }

    @Override
    public String getText() {
        return button.getText();
    }

    @Override
    protected void dispose() {
        if (image != null) {
            image.dispose();
        }
        super.dispose();
    }
}
