/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class UserLogFormatter extends Formatter {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    private final Date date = new Date();

    @Override
    public String format(LogRecord record) {
        var buf = new StringBuilder();

        // Displayed dates use the local timezone (overridable via -Duser.timezone)
        date.setTime(record.getMillis());
        buf.append("[");
        buf.append(dateFormatter.format(date));
        buf.append("] ");

        buf.append(formatMessage(record));
        buf.append("\n");

        return buf.toString();
    }
}
