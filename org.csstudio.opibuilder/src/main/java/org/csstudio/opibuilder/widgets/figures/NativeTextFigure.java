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
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Figure for a native text widget.
 */
public class NativeTextFigure extends AbstractSWTWidgetFigure<Text> implements ITextFigure {

    private Text text;

    private boolean readOnly;

    public NativeTextFigure(AbstractBaseEditPart editPart, int style) {
        super(editPart, style);
    }

    @Override
    protected Text createSWTWidget(Composite parent, int style) {
        text = new Text(parent, style);
        readOnly = (style & SWT.READ_ONLY) != 0;
        return text;
    }

    public Dimension getAutoSizeDimension() {
        var preferredSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return new Dimension(preferredSize.x + getInsets().getWidth(), preferredSize.y + getInsets().getHeight());
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if (runmode && getSWTWidget() != null && !getSWTWidget().isDisposed()) {
            // Its parent should be always enabled so the text can be enabled.
            text.getParent().setEnabled(true);
            text.setEnabled(true);
            if (!readOnly) {
                getSWTWidget().setEditable(value);
            }
        }
    }

    @Override
    public String getText() {
        return text.getText();
    }
}
