/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.Polyline;

/**
 * Graphical feedback factory for polygon widgets.
 */
public final class PolygonFeedbackFactory extends AbstractPolyFeedbackFactory {

    @Override
    protected Polyline createFeedbackFigure() {
        return new Polygon();
    }
}
