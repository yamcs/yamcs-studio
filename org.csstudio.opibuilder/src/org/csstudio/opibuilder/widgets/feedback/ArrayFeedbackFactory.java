/********************************************************************************
 * Copyright (c) 2012, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.feedback;

import org.csstudio.opibuilder.feedback.DefaultGraphicalFeedbackFactory;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.ArrayModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * The feedback factory for array widget.
 */
public class ArrayFeedbackFactory extends DefaultGraphicalFeedbackFactory {

    @Override
    public void showChangeBoundsFeedback(AbstractWidgetModel widgetModel, PrecisionRectangle bounds,
            IFigure feedbackFigure, ChangeBoundsRequest request) {
        var arrayModel = (ArrayModel) widgetModel;

        if (arrayModel.getChildren().isEmpty()) {
            super.showChangeBoundsFeedback(widgetModel, bounds, feedbackFigure, request);
            return;
        }

        var sizeDelta = request.getSizeDelta();
        if (arrayModel.isHorizontal()) {
            var eWidth = arrayModel.getChildren().get(0).getWidth();
            bounds.width -= sizeDelta.width;
            sizeDelta.width = Math.round((float) sizeDelta.width / eWidth) * eWidth;
            bounds.width += sizeDelta.width;
        } else {
            var eHeight = arrayModel.getChildren().get(0).getHeight();
            bounds.height -= sizeDelta.height;
            sizeDelta.height = Math.round((float) sizeDelta.height / eHeight) * eHeight;
            bounds.height += sizeDelta.height;
        }

        super.showChangeBoundsFeedback(widgetModel, bounds, feedbackFigure, request);
    }
}
