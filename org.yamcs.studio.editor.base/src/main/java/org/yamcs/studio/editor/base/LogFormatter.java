/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss.SSS");
    Date d = new Date();

    @Override
    public String format(LogRecord r) {
        var sb = new StringBuilder();

        d.setTime(r.getMillis());
        sb.append(sdf.format(d)).append(" ");

        var name = r.getLoggerName();
        sb.append(name).append(" [").append(r.getThreadID()).append("] ");
        sb.append("[").append(r.getLevel()).append("] ").append(r.getMessage());

        var t = r.getThrown();
        if (t != null) {
            sb.append(": ").append(t.toString()).append("\n");
            addStack(sb, t);
            var cause = t.getCause();
            while (cause != null && cause != t) {
                sb.append("Caused by: ").append(cause.toString()).append("\n");
                addStack(sb, cause);
                cause = cause.getCause();
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private void addStack(StringBuilder sb, Throwable t) {
        for (StackTraceElement ste : t.getStackTrace()) {
            sb.append("\t").append(ste.toString()).append("\n");
        }
    }
}
