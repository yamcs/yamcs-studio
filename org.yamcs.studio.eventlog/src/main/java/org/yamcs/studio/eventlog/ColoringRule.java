/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.yamcs.protobuf.Event;
import org.yamcs.protobuf.Event.EventSeverity;

public class ColoringRule {

    private Pattern CONDITION = Pattern.compile("\\s*(severity|type|source)\\s*(==|!=)\\s*(\\w+)\\s*");
    private Pattern LOGICAL_OP = Pattern.compile("(&&)|(\\|\\|)");

    public final String expression;
    public final RGB bg;
    public final RGB fg;

    public ColoringRule(String expression, RGB bg, RGB fg) {
        this.expression = expression;
        this.bg = bg;
        this.fg = fg;
    }

    public boolean matches(Event event) {
        var trimmed = expression.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        var matches = false;
        try (var scanner = new Scanner(expression)) {
            String pendingLogicalOperation = null;
            while (scanner.hasNext()) {
                scanner.useDelimiter(LOGICAL_OP);
                if (scanner.findInLine(CONDITION) != null) {
                    var property = scanner.match().group(1);
                    var op = scanner.match().group(2);
                    var value = scanner.match().group(3);
                    boolean eq;
                    switch (property) {
                    case "severity":
                        if (event.hasSeverity()) {
                            var severity = event.getSeverity();
                            if (severity == EventSeverity.WARNING_NEW) {
                                severity = EventSeverity.WARNING;
                            }
                            eq = severity.toString().equals(value);
                        } else {
                            eq = false;
                        }
                        break;
                    case "type":
                        eq = event.hasType() && event.getType().equals(value);
                        break;
                    case "source":
                        eq = event.hasSource() && event.getSource().equals(value);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                    }

                    var conditionTrue = ("==".equals(op) && eq) || ("!=".equals(op) && !eq);
                    if (pendingLogicalOperation == null) {
                        matches = conditionTrue;
                    } else if ("&&".equals(pendingLogicalOperation)) {
                        matches &= conditionTrue;
                    } else if ("||".equals(pendingLogicalOperation)) {
                        matches |= conditionTrue;
                    } else {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    // System.out.println(scanner.next());
                    throw new IllegalStateException("Invalid condition");
                }

                scanner.reset();
            }
        }
        return matches;
    }
}
