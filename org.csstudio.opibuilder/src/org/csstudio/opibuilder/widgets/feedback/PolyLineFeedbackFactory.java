/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.eclipse.draw2d.Polyline;

/**
 * Graphical feedback factory for polyline widgets.
 */
public final class PolyLineFeedbackFactory extends AbstractPolyFeedbackFactory {

    @Override
    protected Polyline createFeedbackFigure() {
        return new Polyline();
    }
}
