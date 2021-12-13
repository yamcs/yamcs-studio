/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.MeterModel;
import org.csstudio.swt.widgets.figures.MeterFigure;

/**
 * Feedback Factory for LED.
 */
public class MeterFeedbackFactory extends AbstractFixRatioSizeFeedbackFactory {

    @Override
    public int getMinimumWidth() {
        return MeterModel.MINIMUM_WIDTH;
    }

    @Override
    public int getHeightFromWidth(int width, AbstractWidgetModel widgetModel) {
        return (int) (MeterFigure.HW_RATIO * (width));
    }

    @Override
    public int getWidthFromHeight(int height, AbstractWidgetModel widgetModel) {
        return (int) (height / MeterFigure.HW_RATIO);
    }
}
