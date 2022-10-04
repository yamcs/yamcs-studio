/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.formula;

import java.io.IOException;
import java.util.logging.Level;

import org.yamcs.studio.autocomplete.AutoCompletePlugin;
import org.yamcs.studio.autocomplete.AutoCompleteType;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.FunctionDescriptor;
import org.yamcs.studio.autocomplete.parser.IContentParser;
import org.yamcs.studio.autocomplete.parser.engine.ExprParser;
import org.yamcs.studio.autocomplete.parser.engine.expr.Expr;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprBinaryOperator;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprConditionalOperator;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprException;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprFunction;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprPV;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprVariable;

/**
 * PV formula content parser.
 */
public class FormulaContentParser implements IContentParser {

    private FunctionDescriptor currentToken;
    private String contentToParse;

    @Override
    public boolean accept(ContentDescriptor desc) {
        var content = desc.getValue();
        var type = desc.getAutoCompleteType();
        if (type.equals(AutoCompleteType.Formula) && content.startsWith("=")) {
            return true;
        }
        return false;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        currentToken = null;
        // remove first '='
        contentToParse = new String(desc.getValue()).substring(1);
        try {
            var e = ExprParser.parse(contentToParse);
            handleExpr(e);
        } catch (IOException | ExprException e) {
            AutoCompletePlugin.getLogger().log(Level.WARNING, e.getMessage());
        }
        return currentToken;
    }

    private void handleExpr(Expr e) {
        if (e == null) {
            return;
        }
        switch (e.type) {
        case Variable: // no variables, only functions
            handleVariable((ExprVariable) e);
            break;

        case PV: // call PV parsers
            handlePV((ExprPV) e);
            break;

        case Function: // complete last argument
            handleFunction((ExprFunction) e);
            break;

        case BinaryOperation: // complete right argument
            handleBinaryOperation((ExprBinaryOperator) e);
            break;

        case ConditionalOperation: // complete values
            handleConditionalOperation((ExprConditionalOperator) e);
            break;

        default:
            break;
        }
    }

    private void handleFunction(ExprFunction f) {
        if (f.isComplete()) {
            return;
        }
        currentToken = new FunctionDescriptor();
        currentToken.setValue(f.toString());
        currentToken.setFunctionName(f.getName());
        currentToken.setContentType(ContentType.FormulaFunction);
        currentToken.setOpenBracket(true);
        if (f.size() == 0) {
            currentToken.setCurrentArgIndex(0);
            return;
        }
        currentToken.setCurrentArgIndex(f.size() - 1);
        var lastArg = f.getArg(f.size() - 1);
        if (lastArg == null) {
            return;
        }
        handleExpr(lastArg);
    }

    private void handleBinaryOperation(ExprBinaryOperator bo) {
        var rhs = bo.getRHS();
        if (rhs == null) {
            return;
        }
        handleExpr(rhs);
    }

    private void handleConditionalOperation(ExprConditionalOperator co) {
        var value = co.getValueIfFalse();
        if (value == null) {
            value = co.getValueIfTrue();
        }
        if (value == null) {
            return;
        }
        handleExpr(value);
    }

    private void handlePV(ExprPV pv) {
        var name = pv.getName();
        if (!name.endsWith("'")) {
            var value = name.substring(1);
            var startIndex = contentToParse.length() - value.length() + 1;
            currentToken = new FunctionDescriptor();
            currentToken.setValue(value);
            currentToken.setStartIndex(startIndex);
            currentToken.setContentType(ContentType.PV);
            currentToken.setReplay(true);
        }
    }

    private void handleVariable(ExprVariable v) {
        // No variables, only functions
        var value = v.getName();
        var startIndex = contentToParse.length() - value.length() + 1;
        currentToken = new FunctionDescriptor();
        currentToken.setValue(value);
        currentToken.setStartIndex(startIndex);
        currentToken.setFunctionName(value);
        currentToken.setContentType(ContentType.FormulaFunction);
        currentToken.setCurrentArgIndex(-1);
        currentToken.setOpenBracket(false);
    }
}
