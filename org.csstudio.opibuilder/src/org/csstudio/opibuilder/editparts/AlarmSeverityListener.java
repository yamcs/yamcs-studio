/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.editparts;

import org.eclipse.draw2d.IFigure;
import org.yamcs.studio.data.vtype.AlarmSeverity;

/**
 * Interface for listening to changes of alarm severity.
 */
public interface AlarmSeverityListener {

    /**
     * This method is called when an alarm severity of the subjected PV is changed.
     *
     * @param severity
     *            New severity.
     * @param figure
     *            Figure related to the subjected PV.
     * @return True if some actions are performed.
     */
    public boolean severityChanged(AlarmSeverity severity, IFigure figure);
}
