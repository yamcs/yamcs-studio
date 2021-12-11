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

import org.csstudio.opibuilder.widgets.model.KnobModel;

/**
 * Feedback Factory for Knob.
 */
public class KnobFeedbackFactory extends AbstractFixRatioSizeFeedbackFactory {

    @Override
    public int getMinimumWidth() {
        return KnobModel.MINIMUM_SIZE;
    }

}
