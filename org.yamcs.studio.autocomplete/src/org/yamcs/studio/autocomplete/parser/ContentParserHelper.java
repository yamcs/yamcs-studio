/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser;

import java.io.IOException;
import java.util.logging.Level;

import org.yamcs.studio.autocomplete.AutoCompletePlugin;
import org.yamcs.studio.autocomplete.parser.engine.ExprParser;
import org.yamcs.studio.autocomplete.parser.engine.expr.Expr;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprBoolean;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprDouble;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprException;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprFunction;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprInteger;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprString;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprType;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprVariable;

/**
 * Helper for content parsing.
 */
public class ContentParserHelper {

    /**
     * @return {@link FunctionDescriptor} filled from the content.
     */
    public static FunctionDescriptor parseStandardFunction(String content) {
        var token = new FunctionDescriptor();
        if (content == null || content.isEmpty()) {
            return token;
        }
        ExprFunction function = null;
        try {
            var e = ExprParser.parse(content);
            if (e == null) {
                return token;
            } else if (e.type == ExprType.Function) {
                function = (ExprFunction) e;
            } else if (e.type == ExprType.Variable) {
                function = new ExprFunction(((ExprVariable) e).getName(), null);
            }
        } catch (IOException | ExprException e) {
            AutoCompletePlugin.getLogger().log(Level.SEVERE,
                    "Failed to parse function \"" + content + "\": " + e.getMessage());
        }
        if (function == null) {
            return token;
        }
        token.setValue(content);
        token.setFunctionName(function.getName());
        if (function.getArgs() != null) {
            token.setOpenBracket(true);
            token.setCurrentArgIndex(function.size() > 0 ? function.size() - 1 : 0);
            for (Expr e : function.getArgs()) {
                switch (e.type) {
                case Boolean:
                    token.addArgument(((ExprBoolean) e).value);
                    break;
                case Double:
                    token.addArgument(((ExprDouble) e).value);
                    break;
                case Integer:
                    token.addArgument(((ExprInteger) e).value);
                    break;
                case String:
                    token.addArgument(((ExprString) e).str);
                    break;
                default: // ignore other types
                    token.addArgument(new Object());
                    break;
                }
            }
        }
        token.setComplete(function.isComplete());
        return token;
    }
}
