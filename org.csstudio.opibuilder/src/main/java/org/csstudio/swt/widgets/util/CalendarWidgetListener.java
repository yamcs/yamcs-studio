/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

import java.util.Calendar;

/**
 * Listener interface for the CalendarWidget.
 */
public interface CalendarWidgetListener {
    /**
     * The user or another piece of code set the widget to a new time.
     *
     * @param source
     *            The affected widget.
     * @param calendar
     *            The current date and time.
     */
    void updatedCalendar(CalendarWidget source, Calendar calendar);
}
