/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.datadefinition;

import java.util.EventListener;

/**
 * Definition of listeners that react on manual value change events.
 */
public interface IManualValueChangeListener extends EventListener {
    /**
     * React on a knob event.
     *
     * @param newValue
     *            The new slider value.
     */
    void manualValueChanged(double newValue);
}
