package org.yamcs.studio.eventlog;

import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.yamcs.protobuf.Yamcs.Event;

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
        String trimmed = expression.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        boolean matches = false;
        try (Scanner scanner = new Scanner(expression)) {
            String pendingLogicalOperation = null;
            while (scanner.hasNext()) {
                scanner.useDelimiter(LOGICAL_OP);
                if (scanner.findInLine(CONDITION) != null) {
                    String property = scanner.match().group(1);
                    String op = scanner.match().group(2);
                    String value = scanner.match().group(3);
                    boolean eq;
                    switch (property) {
                    case "severity":
                        eq = event.hasSeverity() && event.getSeverity().toString().equals(value);
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

                    boolean conditionTrue = ("==".equals(op) && eq) || ("!=".equals(op) && !eq);
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
